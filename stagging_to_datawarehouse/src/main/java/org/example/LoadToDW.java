package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class LoadToDW {
    public static void createProductTable(Connection dwConn) throws SQLException {
        String createProductTableQuery = "CREATE TABLE IF NOT EXISTS dim_product (" +
                "product_id VARCHAR(255) PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "original_price DECIMAL(10, 2), " +
                "discounted_price DECIMAL(10, 2), " +
                "discount_percentage DECIMAL(5, 2), " +
                "colors VARCHAR(255), " +
                "sizes VARCHAR(255), " +
                "materials VARCHAR(255)" +
                ")";

        try (Statement stmt = dwConn.createStatement()) {
            stmt.execute(createProductTableQuery);
            System.out.println("Bảng dim_product đã được tạo hoặc đã tồn tại.");
        }
    }

    // Kiểm tra và tạo bảng dim_date nếu chưa có
    public static void createDateTable(Connection dwConn) throws SQLException {
        String createDateTableQuery = "CREATE TABLE IF NOT EXISTS dim_date (" +
                "date_id DATE PRIMARY KEY, " +
                "year INT, " +
                "month INT, " +
                "day INT" +
                ")";

        try (Statement stmt = dwConn.createStatement()) {
            stmt.execute(createDateTableQuery);
            System.out.println("Bảng dim_date đã được tạo hoặc đã tồn tại.");
        }
    }

    // Kiểm tra và tạo bảng fact_sales nếu chưa có
    public static void createFactSalesTable(Connection dwConn) throws SQLException {
        String createFactSalesTableQuery = "CREATE TABLE IF NOT EXISTS fact_sales (" +
                "product_id VARCHAR(255), " +
                "date_id DATE, " +
                "review_score DECIMAL(3, 2), " +
                "rating_count INT, " +
                "view_count INT, " +
                "PRIMARY KEY (product_id, date_id), " +
                "FOREIGN KEY (product_id) REFERENCES dim_product(product_id), " +
                "FOREIGN KEY (date_id) REFERENCES dim_date(date_id)" +
                ")";

        try (Statement stmt = dwConn.createStatement()) {
            stmt.execute(createFactSalesTableQuery);
            System.out.println("Bảng fact_sales đã được tạo hoặc đã tồn tại.");
        }
    }
    // Chèn dữ liệu vào bảng dim_product trong DW
    public static void loadProductToDW(Connection dwConn, List<TransformData.Product> products) throws SQLException {
       createDateTable(dwConn);
        String insertProductQuery = "INSERT INTO dim_product (product_id, name, original_price, discounted_price, " +
                "discount_percentage, colors, sizes, materials) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (product_id) DO NOTHING";

        try (PreparedStatement psProduct = dwConn.prepareStatement(insertProductQuery)) {
            for (TransformData.Product product : products) {
                psProduct.setString(1, product.productId);
                psProduct.setString(2, product.name);
                psProduct.setDouble(3, product.originalPrice);
                psProduct.setDouble(4, product.discountedPrice);
                psProduct.setDouble(5, product.discountPercentage);
                psProduct.setString(6, product.colors);
                psProduct.setString(7, product.sizes);
                psProduct.setString(8, product.materials);
                psProduct.executeUpdate();
            }
        }
    }

    // Chèn dữ liệu vào bảng dim_date trong DW
    public static void loadDateToDW(Connection dwConn, List<TransformData.Product> products) throws SQLException {
        createDateTable(dwConn);
        String insertDateQuery = "INSERT INTO dim_date (date_id, year, month, day) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (date_id) DO NOTHING";

        try (PreparedStatement psDate = dwConn.prepareStatement(insertDateQuery)) {
            for (TransformData.Product product : products) {
                LocalDate date = product.date;
                psDate.setDate(1, Date.valueOf(date));
                psDate.setInt(2, date.getYear());
                psDate.setInt(3, date.getMonthValue());
                psDate.setInt(4, date.getDayOfMonth());
                psDate.executeUpdate();
            }
        }
    }

    // Chèn dữ liệu vào bảng fact_sales trong DW
    public static void loadFactSalesToDW(Connection dwConn, List<TransformData.Product> products) throws SQLException {
        createFactSalesTable(dwConn);
        String insertFactSalesQuery = "INSERT INTO fact_sales (product_id, date_id, review_score, rating_count, view_count) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement psFactSales = dwConn.prepareStatement(insertFactSalesQuery)) {
            for (TransformData.Product product : products) {
                psFactSales.setString(1, product.productId);
                psFactSales.setDate(2, Date.valueOf(product.date));
                psFactSales.setDouble(3, product.reviewScore);
                psFactSales.setInt(4, product.ratingCount);
                psFactSales.setInt(5, product.viewCount);
                psFactSales.executeUpdate();
            }
        }
    }
}
