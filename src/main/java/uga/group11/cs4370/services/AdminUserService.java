package uga.group11.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.group11.cs4370.models.Chef;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.models.User;

@Service
public class AdminUserService {

    private final DataSource dataSource;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminUserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean registerUser(String rawPassword, String username, int image_id) throws SQLException {
        final String sql = "INSERT INTO user (password, username, image_id) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, encoder.encode(rawPassword));
            stmt.setString(2, username);
            stmt.setInt(3, image_id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String getUserIdByUsername(String username) throws SQLException {
        final String sql = "SELECT user_id FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("user_id");
            }
        }
        return null;
    }
}
