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
        String link = ".\\config\\config.properties";
        Connection result = null;
        // 2. ket noi db control
        if (location.equalsIgnoreCase("control")) {
        	 
            try (InputStream input = new FileInputStream(link)) {
                Properties prop = new Properties();
                prop.load(input);
                // Lấy giá trị thuộc tính cấu hình cho cơ sở dữ liệu log
                url = prop.getProperty("log.db.url");
                username = prop.getProperty("log.db.username");
                password = prop.getProperty("log.db.password");
                
            } catch (IOException ex) {
                //  8.3.1, 2.1 Thông báo không tìm thấy file
                System.out.println("Unknown file " + link);
                // Log file
                logFile("Unknown file " + link + "\n" + ex.getMessage());
                System.exit(0);
            }
        }else if (location.equalsIgnoreCase("staging")) {
                try (InputStream input = new FileInputStream(link)) {
                    Properties prop = new Properties();
                    prop.load(input);
                    // Lấy giá trị thuộc tính cấu hình cho nguồn dữ liệu
                    url = prop.getProperty("source.db.url");
                    username = prop.getProperty("source.db.username");
                   password = prop.getProperty("source.db.password");
                } catch (IOException ex) {
                    //  8.3.1, 3.1 Thông báo không tìm thấy file
                    System.out.println("Unknown file " + link);
                    // Log file
                    logFile("Unknown file " + link + "\n" + ex.getMessage());
                    System.exit(0);
                }
            }
        try {
            // đăng kí driver
            Class.forName("com.mysql.jdbc.Driver");
            try {
                // kết nối
                result = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                // 8.3.1, 3.1 thông báo lỗi kết nối
                e.printStackTrace();
                System.out.println("Error connect " + location);
//                updateError(location, "Error connect " + location + "\n" + e.getLocalizedMessage());
            }
        } catch (ClassNotFoundException e) {
            //  8.3.1, 3.1 thông báo không đăng ký được driver
            e.printStackTrace();
            System.out.println("Driver not connect");
            logFile("driver not connect" + "\n" + e.getMessage());
            System.exit(0);
        }
        return result;
    }
}
