package uga.group11.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

@Service
@SessionScope
public class ImageService {

    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public ImageService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    public boolean storeImage(MultipartFile file) throws SQLException {
        final String sql = "INSERT INTO image (image_path) VALUES (?)";

        String imagePath = "/uploads" + file.getOriginalFilename();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, imagePath);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public int setImage_id(MultipartFile file) throws SQLException {
        final String sql = "SELECT image_id FROM image WHERE image_path = ?";

        String imagePath = "/uploads" + file.getOriginalFilename();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, imagePath);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("image_id");
                } else {
                    throw new SQLException("No image found with path: " + imagePath);
                }
            }
        }
    }

    public String getUserImage() throws SQLException {
        final String sql = "SELECT image_path FROM image i, user u " +
                "WHERE i.image_id = u.image_id " +
                "AND u.image_id = (SELECT image_id FROM user WHERE user_id = ?)";
        
        String user_id = userService.getLoggedInUser().getUserId();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image_path");
                } else {
                    throw new SQLException("No image found");
                }
            }
        }
    }

    public boolean updateImage(int image_id, MultipartFile file) throws SQLException {
        final String sql = "UPDATE image SET image_path = ? WHERE image_id = ?";
    
        String image_path = "/uploads" + file.getOriginalFilename();
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, image_path);
            stmt.setInt(2, image_id);
    
            int rowsAffected = stmt.executeUpdate();
            System.out.println("updateImage");
            return rowsAffected > 0;
        }
    }
    
    public int getUserImage_id(String user_id) throws SQLException {
        final String sql = "select image_id from user where user_id = ?";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, user_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("getUserImage_id");
                    return rs.getInt("image_id");
                } else {
                    throw new SQLException("No image found");
                }
            }
        }
    }
}