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
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.group11.cs4370.models.Chef;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.models.User;

@Service
//@SessionScope
public class ChefsService {
    private final DataSource dataSource;
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public ChefsService(DataSource dataSource, UserService userService, RecipeService recipeService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    public boolean createRecipe(String userId, String title, String directions, int estim_time, String image_path, String meal_type, String cuisine_type) throws SQLException {
        final String postSql = "insert into recipe (user_id,title,directions,estim_time,image_path,meal_type,cuisine_type,view_count) values (?,?,?,?,?,?,?,?)";


        try (Connection conn = dataSource.getConnection();
                PreparedStatement sqlStmt = conn.prepareStatement(postSql)) {
            sqlStmt.setString(1, userId);
            sqlStmt.setString(2, title);
            sqlStmt.setString(3, directions);
            sqlStmt.setInt(4, estim_time);
            sqlStmt.setString(5, image_path);
            sqlStmt.setString(6, meal_type);
            sqlStmt.setString(7, cuisine_type);
            sqlStmt.setInt(8, 0); // view_count is set to 0 by default
            System.out.println("SQL Statement: " + sqlStmt.toString());
            int rowsAffected = sqlStmt.executeUpdate();
            return rowsAffected > 0;
        }

    }

    public List<Recipe> getCreatedRecipes(String userId) throws SQLException {
        final String sql = "select distinct r.title,r.directions,r.estim_time,r.rec_id,r.image_path,r.meal_type,r.cuisine_type,r.view_count  from user u, recipe r, subscription s where u.user_id = ? and u.user_id = r.user_id or u.user_id = r.user_id and r.user_id = some (select distinct s.subscribed_id from user u, subscription s where u.user_id = ? and u.user_id = s.subscriber_id) order by rec_id desc";  
        System.out.println(userId);
        List<Recipe> recipes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            System.out.println(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String directions = rs.getString("directions");
                    String title = rs.getString("title");
                    int estim_time = rs.getInt("estim_time");
                    String rec_id = rs.getString("rec_id");
                    String image_path = rs.getString("image_path");
                    String rating = recipeService.getRating(rs.getString("rec_id"));
                    String meal_type = rs.getString("meal_type");
                    String cuisine_type = rs.getString("cuisine_type");
                    int view_count = rs.getInt("view_count");
                    

                    recipes.add(new Recipe(rec_id, title, directions, image_path, estim_time, rating, meal_type, cuisine_type, view_count, recipeService.recipeFavorited(rec_id, userId)));
                }
            }
        }

          final String sql2 = "select * from recipe where user_id = ?";
        if(recipes.isEmpty()){
        try(Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql2)){
            pstmt.setString(1, userId);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    String directions = rs.getString("directions");
                    String title = rs.getString("title");
                    int estim_time = rs.getInt("estim_time");
                    String rec_id = rs.getString("rec_id");
                    String image_path = rs.getString("image_path");
                    String rating = recipeService.getRating(rs.getString("rec_id"));
                    String meal_type = rs.getString("meal_type");
                    String cuisine_type = rs.getString("cuisine_type");
                    int view_count = rs.getInt("view_count");

                    recipes.add(new Recipe(rec_id, title, directions, image_path, estim_time, rating, meal_type, cuisine_type, view_count, recipeService.recipeFavorited(rec_id, userId)));
                }
            }
        }
    }

        return recipes;

    }

    public List<Chef> getChefs() throws SQLException {
        final String sql = "select * from user u, image i where u.image_id = i.image_id";
        List<Chef> chefs = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String user_id = rs.getString("user_id");
                    String username = rs.getString("username");
                    int image_id = rs.getInt("image_id");
                    String image_path = rs.getString("image_path");
                    List<Recipe> recipes = recipeService.getUserRecipes(user_id);
                    boolean isSubbed = true;
                    chefs.add(new Chef(user_id, username, image_id, image_path, recipes, isSubbed));
                }
            }

        }

        return chefs;
    }

    public User getChef(String user_id) throws SQLException {
        final String sql = "select * from user u, image i where u.image_id = i.image_id"
                + " and u.user_id = ?";
        User user = new User();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user_id);

            try (ResultSet rs = pstmt.executeQuery()) {

                String username = rs.getString("username");
                int image_id = rs.getInt("image_id");
                String image_path = rs.getString("image_path");

                user = new User(user_id, username, image_id, image_path);
            }

        }

        return user;
    }

    public List<Chef> getChefsWithSubStatus() throws SQLException {
        System.out.println("Yo");
        final String sql = "SELECT u.user_id, u.username, u.image_id, i.image_path, " +
                           "EXISTS (SELECT 1 FROM subscription s " +
                           "        WHERE s.subscriber_id = ? AND s.subscribed_id = u.user_id) AS isSubbed " +
                           "FROM user u " +
                           "JOIN image i ON u.image_id = i.image_id " +
                           "WHERE u.user_id != ?";

  
    
        List<Chef> chefs = new ArrayList<>();
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            String loggedInUserId = userService.getLoggedInUser().getUserId();
            pstmt.setString(1, loggedInUserId);
            pstmt.setString(2, loggedInUserId);
    
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Sup");
                while (rs.next()) {
                    String user_id = rs.getString("user_id");
                    String username = rs.getString("username");
                    int image_id = rs.getInt("image_id");
                    String image_path = rs.getString("image_path");
                    boolean isSubbed = rs.getBoolean("isSubbed");
                    int count = 0;
                    count++;
                    System.out.println("Chef " + count);
                    List<Recipe> recipes = recipeService.getUserRecipes(user_id);
    
                    chefs.add(new Chef(user_id, username, image_id, image_path, recipes, isSubbed));
                }
            }
        }
    
        return chefs;
    }
    

    public void updateFollowStatus(String subscriber_id, String subscribed_id, boolean isSubbed) throws SQLException {
        final String sql = isSubbed
                ? "INSERT INTO subscription (subscriber_id, subscribed_id) VALUES (?, ?)" // subscribe
                : "DELETE FROM subscription WHERE subscriber_id = ? AND subscribed_id = ?"; // unsubscribe

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, subscriber_id);
            stmt.setString(2, subscribed_id);
            stmt.executeUpdate();
        }

    }
    
    public boolean recipeExistsForUser(String userId, String title) throws SQLException {
        final String sql = "SELECT 1 FROM recipe WHERE user_id = ? AND title = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, title);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
