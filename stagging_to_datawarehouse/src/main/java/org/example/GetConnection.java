package org.example;


import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class GetConnection {
    String sourceDbUrl = null;
    String sourceDbUsername = null;
    String sourceDbPassword = null;
    String logDbUrl = null;
    String logDbUsername = null;
    String logDbPassword = null;

    String url = null;
    String username = null;
    String password = null;
    boolean checkE = false;

    public boolean getCheckE() {
        return checkE;
    }

    public void setCheckE(boolean check) {
        checkE = check;
    }

    public void logFile(String message) throws IOException {
        FileWriter fw = new FileWriter("D:\\DW\\logs.txt", true);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(message + "\t");
        pw.println("HH:mm:ss DD/MM/yyyy - "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss DD/MM/yyyy")));
        pw.println("-----");
        pw.close();
    }

    public Connection getConnection(String location) throws IOException {
        String dbProperties = "/home/ngoctaiphan/WorkSpace/Datawarehouse/Project/DW_2024_T4_Ca4_Nhom9/source_to_csv/stagging_to_datawarehouse/src/db.properties";
        Connection connection = null;

        // 2. Kết nối DB control
        if (location.equalsIgnoreCase("control")) {
            try (InputStream input = new FileInputStream(dbProperties)) {
                Properties prop = new Properties();
                prop.load(input);
                // Lấy giá trị cấu hình cho cơ sở dữ liệu log
                url = prop.getProperty("log.db.url");
                username = prop.getProperty("log.db.username");
                password = prop.getProperty("log.db.password");
            } catch (IOException ex) {
//                System.out.println("Unknown file " + dbProperties);
//                logFile("Unknown file " + dbProperties + "\n" + ex.getMessage());
//                System.exit(0);

                System.out.println(ex.getMessage());
            }
        }
        // 3. Kết nối DB staging
        else if (location.equalsIgnoreCase("staging")) {
            try (InputStream input = new FileInputStream(dbProperties)) {
                Properties prop = new Properties();
                prop.load(input);
                // Lấy giá trị cấu hình cho nguồn dữ liệu
                url = prop.getProperty("source.db.url");
                username = prop.getProperty("source.db.username");
                password = prop.getProperty("source.db.password");
            } catch (IOException ex) {
                System.out.println("Unknown file " + dbProperties);
                logFile("Unknown file " + dbProperties + "\n" + ex.getMessage());
                System.exit(0);
            }
        }
        // 4. Kết nối DB data warehouse (DW)
        else if (location.equalsIgnoreCase("dw")) {
            try (InputStream input = new FileInputStream(dbProperties)) {
                Properties prop = new Properties();
                prop.load(input);
                // Lấy giá trị cấu hình cho data warehouse
                url = prop.getProperty("dw.db.url");
                username = prop.getProperty("dw.db.username");
                password = prop.getProperty("dw.db.password");
            } catch (IOException ex) {
                System.out.println("Unknown file " + dbProperties);
                logFile("Unknown file " + dbProperties + "\n" + ex.getMessage());
                System.exit(0);
            }
        }

        try {
            // Đăng ký driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            try {
                // Kết nối
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Kết nối thành công đến: " + location);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error connect " + location);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Driver not connect");
            logFile("Driver not connect" + "\n" + e.getMessage());
            System.exit(0);
        }

        return connection;
    }

    public static void main(String[] args) throws IOException {
        new GetConnection().getConnection("control");
    }
}
