package org.nhathuy.controller;

import org.nhathuy.model.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogController {
    public static int insertLog(Connection connection, Log log) {
        int generatedId = -1;
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            String sql = "INSERT INTO `log` ( tracking_date_time, source, connect_status, destination, phase, result, detail, is_delete) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1,
                    log.getTrackingDateTime() != null ? log.getTrackingDateTime().format(formatterDateTime) : null);
            statement.setString(2, log.getSource());
            statement.setInt(3, log.getConnectStatus());
            statement.setString(4, log.getDestination());
            statement.setString(5, log.getPhase());
            statement.setString(6, log.getResult());
            statement.setString(7, log.getDetail());
            statement.setBoolean(8, log.isDelete());
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return generatedId;
    }

    public static void writeLogToCSV(Log log, String csvFilePath, String ddmmyyyy, String area) {
        List<String[]> data = new ArrayList<String[]>();
        String[] title = { "id", "tracking_date_time", "source", "connect_status", "destination", "phase", "result",
                "detail", "is_delete" };
        data.add(title);
        String path = csvFilePath + "\\" + ddmmyyyy + "_" + area + ".csv";

        try (FileWriter fileWriter = new FileWriter(path)) {
            for (int i = 0; i < title.length; i++) {
                fileWriter.append(title[i]);
                if (i < title.length - 1) {
                    fileWriter.append(",");
                }
            }
            fileWriter.append("\n");
            // Ghi dòng log vào file CSV
            fileWriter.append(String.valueOf(log.getId())).append(",");
            fileWriter.append(log.getTrackingDateTime() != null ? log.getTrackingDateTime().toString() : null)
                    .append(",");
            fileWriter.append(log.getSource()).append(",");
            fileWriter.append(String.valueOf(log.getConnectStatus())).append(",");
            fileWriter.append(log.getDestination()).append(",");
            fileWriter.append(log.getPhase()).append(",");
            fileWriter.append(log.getResult()).append(",");
            fileWriter.append(log.getDetail()).append(",");
            fileWriter.append(String.valueOf(log.isDelete())).append("\n");
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    fileWriter.append(row[i]);
                    if (i < row.length - 1) {
                        fileWriter.append(",");
                    }
                }
                fileWriter.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
