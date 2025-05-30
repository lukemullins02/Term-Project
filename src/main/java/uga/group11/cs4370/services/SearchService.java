package uga.group11.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.group11.cs4370.models.ExpandedRecipe;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.models.User;

@Service
@SessionScope
public class SearchService {

    private final DataSource dataSource;
    private final RecipeService recipeService;

    @Autowired
    public SearchService(DataSource dataSource, RecipeService recipeService) {
        this.dataSource = dataSource;
        this.recipeService = recipeService;
    }

    public List<Recipe> searchRecipes(
            boolean favoritesOnly,
            boolean followingOnly,
            Integer lessThanTime,
            Float overRating,
            String mealType,
            String cuisineType,
            String orderRecipe,
            Integer limitResults,
            String user_id) throws SQLException {

        List<Recipe> recipes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT recipe.*, AVG(rating.rating) AS avg_rating FROM recipe ");
        StringBuilder join = new StringBuilder();
        StringBuilder where = new StringBuilder();
        StringBuilder having = new StringBuilder();
        List<Object> params = new ArrayList<>();
        boolean groupByNeeded = false;

        // Handle JOINs
        if (favoritesOnly) {
            join.append("JOIN favorite ON recipe.rec_id = favorite.rec_id ");
            where.append(where.length() > 0 ? "AND " : "WHERE ");
            where.append("favorite.user_id = ? ");
            params.add(user_id);
        }
        if (followingOnly) {
            join.append("JOIN subscription ON recipe.user_id = subscription.subscribed_id ");
            where.append(where.length() > 0 ? "AND " : "WHERE ");
            where.append("subscription.subscriber_id = ? ");
            params.add(user_id);
        }
        

        // Handle WHERE conditions
        if (lessThanTime != null) {
            where.append(where.length() > 0 ? "AND " : "WHERE ");
            where.append("recipe.estim_time <= ? ");
            params.add(lessThanTime);
        }
        if (mealType.compareTo("") != 0) {
            where.append(where.length() > 0 ? "AND " : "WHERE ");
            where.append("recipe.meal_type = ? ");
            params.add(mealType);
        }
        if (cuisineType.compareTo("") != 0) {
            where.append(where.length() > 0 ? "AND " : "WHERE ");
            where.append("recipe.cuisine_type = ? ");
            params.add(cuisineType);
        }
        if (overRating != null && overRating != 0) {
            join.append("LEFT JOIN rating ON recipe.rec_id = rating.rec_id ");
            having.append(having.length() > 0 ? "AND " : "HAVING ");
            having.append("AVG(rating.rating) >= ? ");
            params.add(overRating);
            groupByNeeded = true;
        }
        // Handle ORDER BY
        String orderBy = "ORDER BY ";
        if (orderRecipe.compareTo("") != 0) {
            if (orderRecipe.equals("Highest Rated")) {
                //join.append("LEFT JOIN rating ON recipe.rec_id = rating.rec_id ");
                orderBy += "avg_rating DESC ";
                groupByNeeded = true;
            } else if (orderRecipe.equals("Lowest Rated")) {
                //join.append("LEFT JOIN rating ON recipe.rec_id = rating.rec_id ");
                orderBy += "avg_rating ASC ";
                groupByNeeded = true;
            } else if (orderRecipe.equals("Most Viewed")) {
                orderBy += "recipe.view_count DESC ";
            } else if (orderRecipe.equals("Least Viewed")) {
                orderBy += "recipe.view_count ASC ";
            } else if (orderRecipe.equals("Most Recent")) {
                orderBy += "recipe.rec_id DESC ";
            } else if (orderRecipe.equals("Least Recent")) {
                orderBy += "recipe.rec_id ASC ";
            } else {
                orderBy += "recipe.rec_id DESC "; // Default ordering
            }
        } else {
            orderBy += "recipe.rec_id DESC "; // Default ordering
        }

        // Build final query
        sql.append(join);
        if (where.length() > 0) {
            sql.append(where);
        }
        if (groupByNeeded) {
            sql.append("GROUP BY recipe.rec_id ");
        }
        if (having.length() > 0) {
            sql.append(having);
        }
        sql.append(orderBy);
        if (limitResults != null) {
            sql.append("LIMIT ? ");
            params.add(limitResults);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            // Bind parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) param);
                }
            }

            System.out.println("147 pstmt Statement: " + pstmt.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String rec_id = rs.getString("rec_id");
                    String title = rs.getString("title");
                    String directions = rs.getString("directions");
                    String image = rs.getString("image_path");
                    int estim_time = rs.getInt("estim_time");
                    String meal_type = rs.getString("meal_type");
                    String cuisine_type = rs.getString("cuisine_type");
                    int view_count = rs.getInt("view_count");
                    String avg_rating = groupByNeeded ? String.valueOf(rs.getFloat("avg_rating"))
                            : recipeService.getRating(rec_id);
                    recipes.add(new Recipe(rec_id, title, directions, image, estim_time, avg_rating, meal_type,
                            cuisine_type, view_count, false));
                }
            }
        }

        return recipes;
    }

    public List<User> searchChefs(
            boolean favoritesOnly,
            boolean followingOnly,
            Integer lessThanTime,
            Float overRating,
            String mealType,
            String cuisineType,
            String orderChef,
            Integer limitResults,
            String user_id) throws SQLException {
        
        List<User> chefs = new ArrayList<>();
        String sql = "SELECT user.user_id, user.username, user.image_id, image.image_path, COUNT(DISTINCT recipe.rec_id) AS recipe_count, AVG(rating.rating) AS avg_rating ";
        String join = "LEFT JOIN recipe ON user.user_id = recipe.user_id ";
        join += "LEFT JOIN image ON user.image_id = image.image_id ";
        String where = "";
        String having = "";
        List<Object> params = new ArrayList<>();
        boolean groupByNeeded = true; // Always group by user_id to aggregate recipe_count and avg_rating

        // Handle additional JOINs
        if (favoritesOnly) {
            join += "JOIN favorite ON recipe.rec_id = favorite.rec_id ";
            where += where.length() > 0 ? "AND " : "WHERE ";
            where += "favorite.user_id = ? ";
            params.add(user_id);
        }
        if (followingOnly) {
            join += "JOIN subscription ON user.user_id = subscription.subscribed_id ";
            where += where.length() > 0 ? "AND " : "WHERE ";
            where += "subscription.subscriber_id = ? ";
            params.add(user_id);
        }
        if (overRating != null && overRating != 0) {
            join += "LEFT JOIN rating ON recipe.rec_id = rating.rec_id ";
            having += having.length() > 0 ? "AND " : "HAVING ";
            having += "AVG(rating.rating) > ? ";
            params.add(overRating);
        }

        // Handle WHERE conditions for recipe filters
        if (lessThanTime != null) {
            where += where.length() > 0 ? "AND " : "WHERE ";
            where += "recipe.estim_time <= ? ";
            params.add(lessThanTime);
        }
        if (mealType.compareTo("") != 0) {
            where += where.length() > 0 ? "AND " : "WHERE ";
            where += "recipe.meal_type = ? ";
            params.add(mealType);
        }
        if (cuisineType.compareTo("") != 0) {
            where += where.length() > 0 ? "AND " : "WHERE ";
            where += "recipe.cuisine_type = ? ";
            params.add(cuisineType);
        }

        // Handle ORDER BY
        String orderBy = "ORDER BY ";
        if (orderChef.compareTo("") != 0) {
            if (orderChef.equals("Highest Rated")) {
                join += "LEFT JOINAnimation: [Ideal Response] rating ON recipe.rec_id = rating.rec_id ";
                orderBy += "avg_rating DESC ";
            } else if (orderChef.equals("Lowest Rated")) {
                join += "LEFT JOIN rating ON recipe.rec_id = rating.rec_id ";
                orderBy += "avg_rating ASC ";
            } else if (orderChef.equals("Most Recipes")) {
                orderBy += "recipe_count DESC ";
            } else if (orderChef.equals("Least Recipes")) {
                orderBy += "recipe_count ASC ";
            } else if (orderChef.equals("Most Followers")) {
                join += "LEFT JOIN subscription AS followers ON user.user_id = followers.subscribed_id ";
                orderBy += "COUNT(DISTINCT followers.subscriber_id) DESC ";
                groupByNeeded = true;
            } else if (orderChef.equals("Least Followers")) {
                join += "LEFT JOIN subscription AS followers ON user.user_id = followers.subscribed_id ";
                orderBy += "COUNT(DISTINCT followers.subscriber_id) ASC ";
                groupByNeeded = true;
            } else {
                orderBy += "user.user_id DESC "; // Default ordering
            }
        } else {
            orderBy += "user.user_id DESC "; // Default ordering
        }

        // Build final query
        sql += "FROM user ";
        sql += join;
        if (where.length() > 0) {
            sql += where;
        }
        if (groupByNeeded) {
            sql += "GROUP BY user.user_id, user.username, user.image_id, image.image_path ";
        }
        if (having.length() > 0) {
            sql += having;
        }
        sql += orderBy;
        if (limitResults != null) {
            sql += "LIMIT ? ";
            params.add(limitResults);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Bind parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else if (param instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) param);
                }
            }

            System.out.println("287 pstmt Statement: " + pstmt.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String chef_user_id = rs.getString("user_id");
                    String username = rs.getString("username");
                    int image_id = rs.getInt("image_id");
                    String image_path = rs.getString("image_path");
                    chefs.add(new User(chef_user_id, username, image_id, image_path));
                }
            }
        }

        return chefs;
    }
}