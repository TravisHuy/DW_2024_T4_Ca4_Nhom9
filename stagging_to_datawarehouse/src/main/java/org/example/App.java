package org.example;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;


public class App {
    public static void main(String[] args) {
        Connection controlConn = null;
        Connection stagingConn = null;
        Connection dwConn = null;

        try {
            // 1. Kết nối đến control, staging, và data warehouse
            controlConn = new GetConnection().getConnection("control");
            stagingConn = new GetConnection().getConnection("staging");
            dwConn = new GetConnection().getConnection("dw");

            writeLog(controlConn, "connect_db", "Thành công", "Kết nối db control, staging và data warehouse thành công");

            // 2. Lấy dữ liệu từ staging và biến đổi
            String selectStagingData = "SELECT * FROM sports";
            Statement stmt = stagingConn.createStatement();
            ResultSet rs = stmt.executeQuery(selectStagingData);
            List<TransformData.Product> products = TransformData.transformStagingData(rs);

            // 3. Nạp dữ liệu vào Data Warehouse
            LoadToDW.loadProductToDW(dwConn, products);
            LoadToDW.loadDateToDW(dwConn, products);
            LoadToDW.loadFactSalesToDW(dwConn, products);

            writeLog(controlConn, "etl_process", "Thành công", "ETL process completed successfully");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            writeLog(controlConn, "etl_process", "Thất bại", "Lỗi: " + e.getMessage());
        } finally {
            closeConnection(controlConn);
            closeConnection(stagingConn);
            closeConnection(dwConn);
        }
    }

    // Ghi log vào database control
    public static void writeLog(String phase, String result, String detail) throws IOException {
        Connection dbConn = new GetConnection().getConnection("control");
        String query = "INSERT INTO log (tracking_date_time, phase, result, detail) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            stmt.setString(1, LocalDateTime.now().toString());
            stmt.setString(2, phase);
            stmt.setString(3, result);
            stmt.setString(4, detail);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }

    // Đóng kết nối
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
