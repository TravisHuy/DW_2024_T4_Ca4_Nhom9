import org.w3c.dom.Text;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

public class Staging {
    public static void staging() throws SQLException, IOException {
        Connection conn = null;
        PreparedStatement pre_control = null;
        String link = ".\\config\\config.properties";
        // 1. Đọc file config.properties

        try (InputStream input = new FileInputStream(link)){
            Properties prop = new Properties();
            prop.load(input);
            // 2. Kết nối db control
            conn = new GetConnection().getConnection("control");
            try {
                // 3. Tìm các hàng có result Loading, phase STAGING và is_delete 0
                ResultSet re = checResult(conn, pre_control, "Thành công", "source to csv", false );
                // 4. Tìm các tiến trình đang chạy
                if(re.next()){
                    // 4.1 Thông báo
                    System.out.println("Currently, there is another process at work.");
                }else {
                    // 5. Tìm các hàng có result Sucess, phase CSV và is_delete 0
                         re = checResult(conn, pre_control, "Thành công", "source to csv", false   );
                         int id;
                         String filename = null;

            }
        } catch (SQLException e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            // 9. Đóng kết nối db
            pre_control.close();
            conn.close();
        }catch (IOException ex) {
            // 1.1 Thông báo không tìm thấy file
            System.out.println("Unknown file " + link);
            // 1.2 Log file
            new GetConnection().logFile("Unknown file " + link + "\n" + ex.getMessage());
            System.exit(0);
        }
    }
    public static ResultSet checResult(Connection conn, PreparedStatement pre_control, String result, String phase, boolean is_delete) throws SQLException{
        pre_control = conn.prepareStatement("SELECT * FROM log WHERE date(tracking_date_time)=? and result = ? AND phase =? AND is_delete = ?");
        pre_control.setString(1, LocalDateTime.now().toLocalDate().toString());
        pre_control.setString(2,result);
        pre_control.setString(3,phase);
        pre_control.setBoolean(4,is_delete);

        return pre_control.executeQuery();
    }

    // Ghi log vào database
    public static void writeLog(Connection dbConn, String phase, String result, String detail) throws SQLException {
        String query = "INSERT INTO log (tracking_date_time, source, connect_status, destination, phase, result, detail) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            // Set các tham số cho câu lệnh SQL
            stmt.setString(1, LocalDateTime.now().toString());  // Lấy thời gian hiện tại
            stmt.setString(2, "D://DW/DDMMYYYY.csv");          // Đường dẫn nguồn
            stmt.setInt(3, 1);                                 // Trạng thái kết nối (1: thành công)
            stmt.setString(4, "db.staging");                   // Đích đến (database staging)
            stmt.setString(5, phase);                          // Giai đoạn
            stmt.setString(6, result);                         // Kết quả
            stmt.setString(7, detail);                         // Chi tiết

            // Thực thi câu lệnh insert
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting log: " + e.getMessage());
            throw e;  // Ném lại exception nếu có lỗi
        }
    }

    // Ghi dữ liệu vào bảng sports trong database
    public static void writeToStagingDB(Connection dbConn,
                                       String id, String name, String originalPrice, String discountedPrice,
                                       String discountPercentage, String colors, String sizes, String materials,
                                       String reviewScore, String ratingCount, String viewCount, String date) throws SQLException {

        String query = "INSERT INTO sports (Id, Name, Original_Price, Discounted_Price, Discount_Percentage, " +
                "Colors, Sizes, Materials, ReviewScore, RatingCount, ViewCount, Date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConn.prepareStatement(query)) {
            // Set các tham số cho câu lệnh SQL
            stmt.setString(1, id);                             // ID
            stmt.setString(2, name);                           // Name
            stmt.setString(3, originalPrice);                  // Original Price
            stmt.setString(4, discountedPrice);                // Discounted Price
            stmt.setString(5, discountPercentage);             // Discount Percentage
            stmt.setString(6, colors);                         // Colors
            stmt.setString(7, sizes);                          // Sizes
            stmt.setString(8, materials);                      // Materials
            stmt.setString(9, reviewScore);                    // Review Score
            stmt.setString(10, ratingCount);                   // Rating Count
            stmt.setString(11, viewCount);                     // View Count
            stmt.setString(12, date);                          // Date

            // Thực thi câu lệnh insert
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting data into sports table: " + e.getMessage());
            throw e;  // Ném lại exception nếu có lỗi
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        Connection conn = null;
        PreparedStatement pre_control = null;
        String link = ".\\config\\config.properties";

        // 1. Đọc file config.properties
        try (InputStream input = new FileInputStream(link)) {
            Properties prop = new Properties();
            prop.load(input);

            // 2. Kết nối db control
            conn = new GetConnection().getConnection("control");
            writeLog(conn, "connect_db_staging", "Thành công", "Kết nối db control thành công");

            ResultSet re = checResult(conn, pre_control, "Thành công", "source to csv", false);

            if (re.next()) {
                // 4.1 Thông báo
                System.out.println("Kiểm tra tiến trình source to csv thành công");
                writeLog(conn, "check_csv", "Tồn tại", "Đã có file csv hôm nay");

                String filePath = "C:\\DW\\" + LocalDateTime.now().getDayOfMonth() + LocalDateTime.now().getMonthValue() + LocalDateTime.now().getYear() + "-sport.csv";
                File file = new File(filePath);

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    br.readLine(); // Bỏ qua dòng header
                    conn = new GetConnection().getConnection("staging");

                    while ((line = br.readLine()) != null) {
                        // Sử dụng regular expression để tách dữ liệu, bỏ qua dấu phẩy trong chuỗi có dấu nháy kép
                        String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                        // Kiểm tra và định dạng lại cột 'Date' nếu cần thiết
                        String date = data[11].trim(); // Lấy giá trị của cột Date
                        if (!isValidDate(date)) {
                            date = LocalDateTime.now().toString(); // Hoặc dùng giá trị mặc định khác nếu cần
                        }

                        // Ghi dữ liệu vào database
                        writeToStagingDB(conn, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], date);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                conn = new GetConnection().getConnection("control");
                writeLog(conn, "csv to staging", "Thành công", "csv to staging completed");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Kiểm tra nếu ngày tháng có định dạng hợp lệ (yyyy-MM-dd hoặc định dạng bạn muốn)
    private static boolean isValidDate(String date) {
        try {
            LocalDateTime.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
