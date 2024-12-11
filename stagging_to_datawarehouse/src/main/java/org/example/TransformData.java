package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransformData {

    // Biến đổi và làm sạch dữ liệu từ Staging trước khi tải vào DW
    public static List<Product> transformStagingData(ResultSet rs) throws SQLException {
        List<Product> products = new ArrayList<>();

        while (rs.next()) {
            // Làm sạch và chuẩn hóa dữ liệu
            String productId = rs.getString("Id").trim();
            String name = rs.getString("Name").trim();
            double originalPrice = Double.parseDouble(rs.getString("Original_Price").replaceAll("[^\\d.]", ""));
            double discountedPrice = Double.parseDouble(rs.getString("Discounted_Price").replaceAll("[^\\d.]", ""));
            double discountPercentage = Double.parseDouble(rs.getString("Discount_Percentage").replace("%", ""));
            String colors = rs.getString("Colors").trim();
            String sizes = rs.getString("Sizes").trim();
            String materials = rs.getString("Materials").trim();
            double reviewScore = Double.parseDouble(rs.getString("ReviewScore"));
            int ratingCount = Integer.parseInt(rs.getString("RatingCount"));
            int viewCount = Integer.parseInt(rs.getString("ViewCount"));
            LocalDate date = LocalDate.parse(rs.getString("Date").trim());

            // Tạo đối tượng Product từ dữ liệu đã làm sạch
            Product product = new Product(productId, name, originalPrice, discountedPrice, discountPercentage,
                    colors, sizes, materials, reviewScore, ratingCount, viewCount, date);
            products.add(product);
        }

        return products;
    }

    // Lớp đại diện cho sản phẩm
    public static class Product {
        String productId;
        String name;
        double originalPrice;
        double discountedPrice;
        double discountPercentage;
        String colors;
        String sizes;
        String materials;
        double reviewScore;
        int ratingCount;
        int viewCount;
        LocalDate date;

        public Product(String productId, String name, double originalPrice, double discountedPrice, double discountPercentage,
                       String colors, String sizes, String materials, double reviewScore, int ratingCount, int viewCount, LocalDate date) {
            this.productId = productId;
            this.name = name;
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
            this.discountPercentage = discountPercentage;
            this.colors = colors;
            this.sizes = sizes;
            this.materials = materials;
            this.reviewScore = reviewScore;
            this.ratingCount = ratingCount;
            this.viewCount = viewCount;
            this.date = date;
        }
    }
}
