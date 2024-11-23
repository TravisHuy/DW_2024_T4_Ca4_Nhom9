package org.nhathuy.controller;

import org.nhathuy.model.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfigController {
    public static Config getConfig(Connection connection, String description) throws SQLException {
        Config res = null;
        String sql = "SELECT * FROM `config` WHERE phase=? AND status=? AND description=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, "source to csv");
            statement.setInt(2, 1);
            statement.setString(3, description);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                res = new Config();
                res.setId(rs.getInt(1));
                res.setPhase(rs.getString(2));
                res.setSource(rs.getString(3));
                res.setSourceName(rs.getString(4));
                res.setArea(rs.getString(5));
                res.setPathToSave(rs.getString(6));
                res.setFileNameFormat(rs.getString(7));
                res.setFileType(rs.getString(8));
                res.setTimeGetData(rs.getString(9) != null
                        ? LocalDateTime.parse(rs.getString(9), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null);
                res.setInterval(rs.getInt(10));
                res.setCreateDate(
                        LocalDateTime.parse(rs.getString(11), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                res.setUpdateDate(rs.getString(12) != null
                        ? LocalDateTime.parse(rs.getString(12), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null);
                res.setDescription(rs.getString(13));
                res.setStatus(rs.getInt(14));
                break;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
}
