package org.nhathuy.run;

import org.nhathuy.controller.ConfigController;
import org.nhathuy.controller.LogController;
import org.nhathuy.dao.SportCrawling;
import org.nhathuy.dao.ExportToExcel;
import org.nhathuy.db.DBConnect;
import org.nhathuy.db.DBProperties;
import org.nhathuy.model.Config;
import org.nhathuy.model.Log;
import org.nhathuy.model.Sport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class AutoSportCrawler {
    // URL nguồn dữ liệu cần crawl
    private static final String BASE_URL = "https://vnsport.com.vn/danh-muc/phu-kien-the-thao/";

    /**
     * Format ngày tháng theo định dạng ddMMyyyy
     * VD: 23112024 cho ngày 23/11/2024
     */
    public static String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
    }

    /**
     * Khởi động quá trình crawl dữ liệu tự động hàng ngày
     * Hệ thống sẽ chạy vào 1h sáng mỗi ngày
     */
    public static void startDailyCrawling() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    executeDataCollection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // Thiết lập thời gian chạy (11h sáng mỗi ngày)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstRun = now.withHour(11).withMinute(0).withSecond(0);
        if (now.compareTo(firstRun) > 0) {
            firstRun = firstRun.plusDays(1);
        }

        // Tính toán delay và chu kỳ chạy
        long delay = java.time.Duration.between(now, firstRun).toMillis();
        long period = 24 * 60 * 60 * 1000; // 24 giờ tính bằng milliseconds

        timer.scheduleAtFixedRate(task, delay, period);
    }

    /**
     * Thực hiện quá trình thu thập dữ liệu
     * Bao gồm các bước:
     * 1. Kết nối database
     * 2. Lấy cấu hình
     * 3. Thu thập dữ liệu
     * 4. Xuất file CSV
     * 5. Ghi log
     */
    public static void executeDataCollection() throws SQLException, IOException {
        System.out.println("1. Bắt đầu quá trình thu thập dữ liệu");
        Log log = new Log();

        // 2. Lấy thông tin cấu hình database (Properties) từ file dbControl.properties
        System.out.println("2. Đang đọc file properties...");
        Properties prop = new Properties();
        prop.load(DBProperties.class.getClassLoader().getResourceAsStream("dbControl.properties"));
        DBProperties.setProperties(prop);

        // 3. Kết nối DB Control
        Connection connection = null;
        int retryCount = 0;

        while (retryCount < 3) {
            System.out.println("3. Đang thử kết nối database lần " + (retryCount + 1));
            connection = DBConnect.getInstance().get();
            if (connection != null) {
                // 4. Lấy config(phase="source to csv", description="GET_DATA_AUTO", status=1) trong config table
                System.out.println("4. Kết nối database thành công");
                Config config = ConfigController.getConfig(connection, Config.AUTO);

                // 5. Ghi log bắt đầu lấy dữ liệu
                System.out.println("6. Đang ghi log bắt đầu...");
                log.setTrackingDateTime(LocalDateTime.now());
                log.setSource(BASE_URL);
                log.setConnectStatus(1);
                log.setDestination(config.getPathToSave());
                log.setPhase("source to csv");
                log.setResult("Bắt đầu");
                log.setDetail("Bắt đầu thu thập dữ liệu từ " + config.getSource());
                log.setDelete(false);
                LogController.insertLog(connection, log);

                // Thu thập dữ liệu từ website
                // Crawl dữ liệu
                System.out.println("7. Bắt đầu crawl dữ liệu từ " + BASE_URL);
                //6. Lấy dữ liệu
                List<Sport> sports = SportCrawling.getAllSport(BASE_URL);
                //4.7 Kiểm tra xem List có rỗng hay không
                if (sports != null && !sports.isEmpty()) {
                    // 4.7.1 Ghi log lấy dữ liệu thành công
                    log.setTrackingDateTime(LocalDateTime.now());
                    log.setResult("Thành công");
                    log.setDetail("Lấy dữ liệu ngày " + sports.get(0).getDate() + " phụ kiện thể thao thành công");
                    LogController.insertLog(connection, log);

                    // 5. Load dữ liệu vào file CSV
                    String csvPath = config.getPathToSave() + "/sports_" + formatDate(LocalDateTime.now()) + ".csv";
                    ExportToExcel.exportToCSV(sports, csvPath);

                    // 6. Ghi log lấy load file csv thành công
                    log.setTrackingDateTime(LocalDateTime.now());
                    log.setResult("Thành công");
                    log.setDetail("Load file CSV ngày: " + sports.get(0).getDate() + " thành công");
                    LogController.insertLog(connection, log);
                } else {
                    // 4.7.2 Ghi log lấy dữ liệu thất bại
                    log.setTrackingDateTime(LocalDateTime.now());
                    log.setConnectStatus(0);
                    log.setResult("Thất bại");
                    log.setDetail("Không thu thập được dữ liệu từ nguồn");
                    LogController.insertLog(connection, log);
                }
                break;
            }

            // Ghi log khi kết nối database thất bại
            log.setTrackingDateTime(LocalDateTime.now());
            log.setConnectStatus(0);
            log.setResult("Kết nối database thất bại");
            log.setDetail("Không thể kết nối database, lần thử " + (retryCount + 1) + " / 3");
            log.setDelete(false);

            // Lưu log lỗi vào thư mục riêng
            String errorPath = "D://ErrorsDW";
            LogController.writeLogToCSV(log, errorPath, formatDate(log.getTrackingDateTime()), "SPORTS");

            retryCount++;
            System.out.println("Kết nối database thất bại. Đang thử lại... Lần " + retryCount + " / 3");

            // Đợi 5 giây trước khi thử lại
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Ghi log khi không thể kết nối sau 3 lần thử
        if (connection == null) {
            log.setTrackingDateTime(LocalDateTime.now());
            log.setConnectStatus(0);
            log.setResult("Kết nối database thất bại");
            log.setDetail("Không thể kết nối database sau 3 lần thử");
            log.setDelete(false);
            LogController.writeLogToCSV(log, "D://ErrorsDW", formatDate(log.getTrackingDateTime()), "SPORTS");
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Bắt đầu chạy chương trình...");
//            executeDataCollection(); // Chạy ngay lập tức
             startDailyCrawling(); // Có thể giữ lại nếu vẫn muốn schedule chạy hàng ngày
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}