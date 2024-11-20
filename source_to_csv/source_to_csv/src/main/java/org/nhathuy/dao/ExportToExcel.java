package org.nhathuy.dao;

import org.nhathuy.model.Sport;

import java.io.*;
import java.util.List;

import static org.nhathuy.dao.SportCrawling.getAllSport;

public class ExportToExcel {
    private static final String BASE_URL = "https://vnsport.com.vn/danh-muc/phu-kien-the-thao/";

    public static void main(String[] args) {
        exportToCSV(getAllSport(BASE_URL),"G:\\intellij_project\\DW_2024_T4_Ca4_Nhom9\\source_to_csv\\source_to_csv\\src\\main\\java\\org\\nhathuy\\data.csv");
    }
    public static void exportToCSV(List<Sport> sports, String filePath) {
        // Define CSV headers
        String[] headers = {
                "Id", "Name", "Original_Price", "Discounted_Price",
                "Discount_Percentage", "Colors", "Sizes", "Materials",
                "ReviewScore", "RatingCount", "ViewCount", "Date"
        };

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {

            // Write BOM for Excel UTF-8 compatibility
            writer.write('\ufeff');

            // Write headers
            writer.write(String.join(",", headers));
            writer.newLine();

            // Write data rows
            for (Sport sport : sports) {
                String[] data = {
                        escapeCSV(sport.getId()),
                        escapeCSV(sport.getName()),
                        escapeCSV(sport.getOriginal_price()),
                        escapeCSV(sport.getDiscounted_price()),
                        escapeCSV(sport.getDiscount_percentage()),
                        escapeCSV(sport.getColors()),
                        escapeCSV(sport.getSizes()),
                        escapeCSV(sport.getMaterials()),
                        escapeCSV(sport.getReviewScore()),
                        escapeCSV(sport.getRatingCount()),
                        String.valueOf(sport.getViewCount()),
                        String.valueOf(sport.getDate())
                };
                writer.write(String.join(",", data));
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    // Helper method to escape special characters in CSV
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        value = value.replace("\"", "\"\""); // Escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\""; // Wrap in quotes if contains special chars
        }
        return value;
    }
}
