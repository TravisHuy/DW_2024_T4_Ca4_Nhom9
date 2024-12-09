package org.nhathuy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DataTransfer {

	public static void main(String[] args) {
		// Cấu hình logging
		Logger logger = Logger.getLogger(DataTransfer.class.getName());
		logger.setLevel(Level.INFO);

		// Thêm custom handler để in log
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				System.out.println(record.getLevel() + ": " + record.getMessage());
			}

			@Override
			public void flush() {}

			@Override
			public void close() throws SecurityException {}
		});

		Connection sourceConn = null;
		Connection destConn = null;
		Connection logConn = null;

		try {
			// Đọc file cấu hình
			Properties props = new Properties();
			try (InputStream input = DataTransfer.class.getClassLoader().getResourceAsStream("config.properties")) {
				if (input == null) {
					throw new FileNotFoundException("config.properties not found in resources directory");
				}
				props.load(input);
			}

			// Nạp driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Mở kết nối
			sourceConn = DriverManager.getConnection(
					props.getProperty("source.db.url"),
					props.getProperty("source.db.username"),
					props.getProperty("source.db.password")
			);

			destConn = DriverManager.getConnection(
					props.getProperty("dest.db.url"),
					props.getProperty("dest.db.username"),
					props.getProperty("dest.db.password")
			);

			logConn = DriverManager.getConnection(
					props.getProperty("log.db.url"),
					props.getProperty("log.db.username"),
					props.getProperty("log.db.password")
			);

			// Tắt auto commit để quản lý transaction
			sourceConn.setAutoCommit(false);
			destConn.setAutoCommit(false);

			// Ghi log bắt đầu quá trình
			writeLog(logConn, "data_transfer", "Bắt đầu", "Bắt đầu chuyển dữ liệu từ staging sang data warehouse");

			// Thực hiện chuyển dữ liệu
			transferData(sourceConn, destConn);

			// Commit transaction
			sourceConn.commit();
			destConn.commit();

			// Ghi log thành công
			writeLog(logConn, "data_transfer", "Thành công", "Chuyển dữ liệu từ staging sang data warehouse thành công");

			logger.info("Data transfer completed successfully!");

		} catch (ClassNotFoundException e) {
			logger.severe("JDBC Driver not found: " + e.getMessage());

			// Ghi log lỗi
			try {
				writeLog(logConn, "data_transfer", "Lỗi", "Không tìm thấy JDBC Driver: " + e.getMessage());
			} catch (SQLException logEx) {
				logger.severe("Error writing log: " + logEx.getMessage());
			}

		} catch (SQLException e) {
			logger.severe("Database error: " + e.getMessage());

			// Rollback nếu có lỗi
			try {
				if (sourceConn != null) sourceConn.rollback();
				if (destConn != null) destConn.rollback();

				// Ghi log lỗi
				writeLog(logConn, "data_transfer", "Lỗi", "Lỗi chuyển dữ liệu: " + e.getMessage());
			} catch (SQLException rollbackEx) {
				logger.severe("Error during rollback: " + rollbackEx.getMessage());
			}

		} catch (IOException e) {
			logger.severe("Configuration file error: " + e.getMessage());

			// Ghi log lỗi
			try {
				writeLog(logConn, "data_transfer", "Lỗi", "Lỗi đọc file cấu hình: " + e.getMessage());
			} catch (SQLException logEx) {
				logger.severe("Error writing log: " + logEx.getMessage());
			}

		} finally {
			// Đóng kết nối
			try {
				if (sourceConn != null) sourceConn.close();
				if (destConn != null) destConn.close();
				if (logConn != null) logConn.close();
			} catch (SQLException e) {
				logger.severe("Error closing connections: " + e.getMessage());
			}
		}
	}
	private static void writeLog(Connection logConn, String phase, String result, String detail) throws SQLException {
		String query = "INSERT INTO log (tracking_date_time, source, connect_status, destination, phase, result, detail) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = logConn.prepareStatement(query)) {
			stmt.setString(1, LocalDateTime.now().toString());
			stmt.setString(2, "Staging Database");
			stmt.setInt(3, 1);
			stmt.setString(4, "Data Warehouse");
			stmt.setString(5, phase);
			stmt.setString(6, result);
			stmt.setString(7, detail);

			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Error inserting log: " + e.getMessage());
			throw e;
		}
	}
	private static void transferSportData(Connection sourceConn, Connection destConn) throws SQLException {
		// 1. Lấy dữ liệu từ bảng sports trong staging
		Statement sourceStmt = sourceConn.createStatement();
		ResultSet resultSet = sourceStmt.executeQuery("SELECT * FROM sports");

		// 2. Chuẩn bị các câu lệnh insert cho các bảng dimension và fact
		PreparedStatement dimProductStmt = destConn.prepareStatement(
				"INSERT INTO dim_product (name, colors, sizes, materials) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);

		PreparedStatement dimDateStmt = destConn.prepareStatement(
				"INSERT INTO dim_date (full_date, day, month, year) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);

		PreparedStatement factSportStmt = destConn.prepareStatement(
				"INSERT INTO fact_sport (product_id, date_id, original_price, discounted_price, " +
						"discount_percentage, review_score, rating_count, view_count) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
		);
		//3. Lặp qua từng dòng dữ liệu trong ResultSet
		while (resultSet.next()) {
			// 4. Xử lý dimension Product
			// 5. Kiểm tra dữ liệu đã có trong dim_product không
			int productId = insertOrGetProductDimension(destConn, dimProductStmt,
					resultSet.getString("Name"),
					resultSet.getString("Colors"),
					resultSet.getString("Sizes"),
					resultSet.getString("Materials")
			);

			//  6: Nếu chưa có, thêm mới vào dim_product
			// (được thực hiện bên trong hàm insertOrGetProductDimension)

			//  7: Nếu đã có, lấy ID của sản phẩm (được thực hiện bên trong hàm insertOrGetProductDimension)

			//  8: Xử lý dimension Date
			//  9: Kiểm tra dữ liệu đã có trong dim_date chưa
			String dateStr = resultSet.getString("Date");
			int dateId = insertOrGetDateDimension(destConn, dimDateStmt, dateStr);

			//  10: Nếu chưa có, thêm mới vào dim_date
			// (được thực hiện bên trong hàm insertOrGetDateDimension)

			//  11: Nếu đã có, lấy ID của ngày (được thực hiện bên trong hàm insertOrGetDateDimension)

			//  12: Chuẩn bị dữ liệu cho bảng fact_sport
			factSportStmt.setInt(1, productId);
			factSportStmt.setInt(2, dateId);
			factSportStmt.setString(3, resultSet.getString("Original_Price"));
			factSportStmt.setString(4, resultSet.getString("Discounted_Price"));
			factSportStmt.setString(5, resultSet.getString("Discount_Percentage"));
			factSportStmt.setString(6, resultSet.getString("ReviewScore"));
			factSportStmt.setInt(7, Integer.parseInt(resultSet.getString("RatingCount")));
			factSportStmt.setInt(8, Integer.parseInt(resultSet.getString("ViewCount")));

			// 13: Chèn dữ liệu vào fact table
			factSportStmt.executeUpdate();
		}

		// 14: Kết thúc xử lý cho từng dòng dữ liệu và đóng vòng lặp
		sourceStmt.close();
		dimProductStmt.close();
		dimDateStmt.close();
		factSportStmt.close();
	}

	//6.
	private static int insertOrGetProductDimension(Connection destConn,
												   PreparedStatement dimProductStmt,
												   String name,
												   String colors,
												   String sizes,
												   String materials) throws SQLException {
		// 10. Kiểm tra xem sản phẩm đã tồn tại chưa
		PreparedStatement checkStmt = destConn.prepareStatement(
				"SELECT id FROM dim_product WHERE name = ? AND colors = ? AND sizes = ? AND materials = ?"
		);
		checkStmt.setString(1, name);
		checkStmt.setString(2, colors);
		checkStmt.setString(3, sizes);
		checkStmt.setString(4, materials);

		ResultSet existingProduct = checkStmt.executeQuery();
		if (existingProduct.next()) {
			return existingProduct.getInt("id");
		}

		// 11. Nếu chưa tồn tại, thêm mới
		dimProductStmt.setString(1, name);
		dimProductStmt.setString(2, colors);
		dimProductStmt.setString(3, sizes);
		dimProductStmt.setString(4, materials);
		dimProductStmt.executeUpdate();

		// 12. Lấy ID vừa được sinh ra
		ResultSet generatedKeys = dimProductStmt.getGeneratedKeys();
		if (generatedKeys.next()) {
			return generatedKeys.getInt(1);
		}

		throw new SQLException("Creating product failed, no ID obtained.");
	}

	//10.
	private static int insertOrGetDateDimension(Connection destConn,
												PreparedStatement dimDateStmt,
												String dateStr) throws SQLException {
		// Chuyển đổi dateStr sang các thành phần ngày tháng
		LocalDate date = LocalDate.parse(dateStr);

		// Kiểm tra xem ngày đã tồn tại chưa
		PreparedStatement checkStmt = destConn.prepareStatement(
				"SELECT id FROM dim_date WHERE full_date = ?"
		);
		checkStmt.setString(1, dateStr);

		ResultSet existingDate = checkStmt.executeQuery();
		if (existingDate.next()) {
			return existingDate.getInt("id");
		}

		// Nếu chưa tồn tại, thêm mới
		dimDateStmt.setString(1, dateStr);
		dimDateStmt.setInt(2, date.getDayOfMonth());
		dimDateStmt.setInt(3, date.getMonthValue());
		dimDateStmt.setInt(4, date.getYear());
		dimDateStmt.executeUpdate();

		// Lấy ID vừa được sinh ra
		ResultSet generatedKeys = dimDateStmt.getGeneratedKeys();
		if (generatedKeys.next()) {
			return generatedKeys.getInt(1);
		}

		throw new SQLException("Creating date dimension failed, no ID obtained.");
	}

	// Trong phương thức transferData, thêm:
	private static void transferData(Connection sourceConn, Connection destConn) throws SQLException {
		// Các phương thức chuyển dữ liệu khác...

		// Chuyển dữ liệu Sport
		transferSportData(sourceConn, destConn);
	}
}