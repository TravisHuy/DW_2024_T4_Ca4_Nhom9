package org.example;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.example.App.writeLog;

public class CSVToDatabase {

    public static void writeToStagingDB(Connection dbConn, String id, String name, String originalPrice,
                                        String discountedPrice, String discountPercentage, String colors,
                                        String sizes, String materials, String reviewScore, String ratingCount,
                                        String viewCount, String date) throws SQLException {

        String query = "INSERT INTO sports (Id, Name, Original_Price, Discounted_Price, Discount_Percentage, " +
                "Colors, Sizes, Materials, ReviewScore, RatingCount, ViewCount, Date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, originalPrice);
            stmt.setString(4, discountedPrice);
            stmt.setString(5, discountPercentage);
            stmt.setString(6, colors);
            stmt.setString(7, sizes);
            stmt.setString(8, materials);
            stmt.setString(9, reviewScore);
            stmt.setString(10, ratingCount);
            stmt.setString(11, viewCount);
            stmt.setString(12, date);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting data into sports table: " + e.getMessage());
            throw e;
        }
    }

    public static void importCSVToDatabase(String csvFile, Connection dbConn) {
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String id = row[0];
                String name = row[1];
                String originalPrice = row[2];
                String discountedPrice = row[3];
                String discountPercentage = row[4];
                String colors = row[5];
                String sizes = row[6];
                String materials = row[7];
                String reviewScore = row[8];
                String ratingCount = row[9];
                String viewCount = row[10];
                String date = row[11];

                // Gọi phương thức để ghi dữ liệu vào cơ sở dữ liệu
                writeToStagingDB(dbConn, id, name, originalPrice, discountedPrice, discountPercentage, colors, sizes,
                        materials, reviewScore, ratingCount, viewCount, date);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Tạo kết nối đến cơ sở dữ liệu
       Connection stagingConn = new GetConnection().getConnection("stagging");

        // Đọc và nhập CSV
        importCSVToDatabase("/home/ngoctaiphan/WorkSpace/Datawarehouse/Project/Data/data.csv", stagingConn);
        writeLog(stagingConn, "etl_process", "Thành công", "ETL process completed successfully");
    }
}
