package uga.group11.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.group11.cs4370.models.ExpandedRecipe;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.models.User;

@Service
@SessionScope
public class RecipeService {

    private final DataSource dataSource;
    private final UserService userService;

    @Autowired
    public RecipeService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    public String getRating(String rec_id) throws SQLException {
        final String sql = "select round(avg(rating), 1) as average_rating from rating where rec_id = ?;";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rec_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    float averageRating = rs.getFloat("average_rating");
                    return String.format("%.1f", averageRating);
                }

            } catch (SQLException e) {
                return "1";
            }
        }
        return "0";
    }

    public List<Recipe> getRecipe(String rec_id) throws SQLException {

        List<Recipe> recipes = new ArrayList<>();

       final String sql1 = "UPDATE recipe SET view_count = view_count + 1 WHERE rec_id = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setString(1, rec_id);

            int rowsAffected = pstmt.executeUpdate();

            // Optional: check if update actually occurred
            if (rowsAffected > 0) {
                System.out.println("View count updated successfully.");
            } else {
                System.out.println("No rows updated. Check rec_id.");
            }
        }


        final String sql = "select * from recipe where rec_id = ?";

        String rating = this.getRating(rec_id);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rec_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    String directions = rs.getString("directions");
                    String image_path = rs.getString("image_path");
                    int estim_time = rs.getInt("estim_time");
                    String meal_type = rs.getString("meal_type");
                    String cuisine_type = rs.getString("cuisine_type");
                    int view_count = rs.getInt("view_count");


                    // Create a new Recipe object and return it
                     recipes.add( new Recipe(rec_id, title, directions, image_path, estim_time, rating,meal_type,cuisine_type,view_count,false));
                }
            }
        }
        return recipes;
    }

    public boolean rateRecipe(int count, String rec_id, User user) throws SQLException {
        final String sql = """
            INSERT INTO rating (user_id, rec_id, rating)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE rating = VALUES(rating);
        """;
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, rec_id);
            pstmt.setInt(3, count);
    
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    

    public List<ExpandedRecipe> getUserExpRecipes(String userId) throws SQLException {
        List<ExpandedRecipe> recipes = new ArrayList<>();
        final String sql = "select * from recipe where user_id = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String rec_id = rs.getString("rec_id");
                    String title = rs.getString("title");
                    String directions = rs.getString("directions");
                    String image_path = rs.getString("image_path");
                    int estim_time = rs.getInt("estim_time");
                    String rating = this.getRating(rec_id);
                    recipes.add(new ExpandedRecipe(rec_id, title, image_path, estim_time, rating, directions));
                }
            }
        }

        return recipes;
    }

    public List<Recipe> getUserRecipes(String userId) throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        final String sql = "select * from recipe where user_id = ?;";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String rec_id = rs.getString("rec_id");
                    String title = rs.getString("title");
                    String directions = rs.getString("directions");
                    int estim_time = rs.getInt("estim_time");
                    String rating = this.getRating(rec_id);
                    String meal_type = rs.getString("meal_type");
                    String cuisine_type = rs.getString("cuisine_type");
                    String image_path = rs.getString("image_path");
                    int view_count = rs.getInt("view_count");

                    
                    recipes.add(new Recipe(rec_id, title, directions, image_path, estim_time, rating,meal_type,cuisine_type,view_count,false));
                }
            }
        }

        return recipes;
    }
    
    public boolean favoriteRecipe(boolean isAdd, String rec_id, User user) throws SQLException {
        String userId = user.getUserId();
    
        if (isAdd) {
            if (existsFavorite(rec_id, userId)) {
                System.out.println("Already favorited: skipping insert.");
                return false;
            }
    
            final String sql = "INSERT INTO Favorite (rec_id, user_id) VALUES (?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, rec_id);
                stmt.setString(2, userId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } else {
            final String sql = "DELETE FROM Favorite WHERE rec_id = ? AND user_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, rec_id);
                stmt.setString(2, userId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        }
    }

    public boolean existsFavorite(String rec_id, String user_id) {
        final String sql = "SELECT 1 FROM Favorite WHERE rec_id = ? AND user_id = ? LIMIT 1";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, rec_id);
            stmt.setString(2, user_id);
    
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // returns true if any row exists
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Recipe> getFavoriteRecipe(String user_id) throws SQLException {
        final String sql = """
                SELECT r.*, ROUND(AVG(rt.rating), 1) AS avg_rating 
                FROM recipe r
                JOIN favorite f ON r.rec_id = f.rec_id
                LEFT JOIN rating rt ON r.rec_id = rt.rec_id
                WHERE f.user_id = ?
                GROUP BY r.rec_id;
        """;

        
        List<Recipe> favoriteRecipes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user_id);  // Set the logged-in user_id

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Retrieve the post data from the result set
                    String rec_id = rs.getString("rec_id");
                    String title = rs.getString("title");
                    String directions = rs.getString("directions");
                    String image = rs.getString("image_path");
                    int estim_time = rs.getInt("estim_time");
                    double rating = rs.getDouble("avg_rating");

                    // Fetch the user from the UserService
                    //User user = userService.getUserFromRecipeId(rec_id);  // Ensure this method is being called
                    
                    // Create the Recipe object with all necessary details
                    Recipe recipe = new Recipe(rec_id, title, directions, image, estim_time, String.valueOf(rating), true);
                    favoriteRecipes.add(recipe);
                }
            }
        }

        return favoriteRecipes;
    }
    
    boolean recipeFavorited(String rec_id, String user_id) throws SQLException {
        boolean favorited = false;
        final String sql = "select count(*) as row_count from favorite where rec_id = ? and user_id = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rec_id);
            pstmt.setString(2, user_id);
            try(ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                if(Integer.parseInt(rs.getString("row_count")) > 0) favorited = true;           
            }
        }
        return favorited;
    }

    public int getUserRatingForRecipe(String userId, String recId) throws SQLException {
        final String sql = "SELECT rating FROM rating WHERE user_id = ? AND rec_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, recId);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rating");
                }
            }
        }
        return 0; // No rating yet
    }
    
}
