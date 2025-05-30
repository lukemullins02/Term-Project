package uga.group11.cs4370.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import uga.group11.cs4370.services.AdminUserService;
import uga.group11.cs4370.services.ChefsService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import java.sql.Connection;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DummyUserSeeder implements CommandLineRunner {

    private final AdminUserService adminUserService;
    private final ChefsService chefsService;
    private final DataSource dataSource;

    @Autowired
    public DummyUserSeeder(AdminUserService adminUserService, ChefsService chefsService, DataSource dataSource) {
        this.adminUserService = adminUserService;
        this.chefsService = chefsService;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Asdf1234!";
        //String hashedPassword = encoder.encode(rawPassword);

        String[] names = {
            "Alice Johnson", "Bob Smith", "Charlie Nguyen", "Diana Perez", "Ethan Lee",
            "Fiona Davis", "George Patel", "Hannah Wright", "Ian Chen", "Julia Adams",
            "Kevin Torres", "Lily Robinson", "Michael Garcia", "Nina Young", "Oscar Martin",
            "Paula Scott", "Quentin Bell", "Rachel Kim", "Samuel Murphy", "Tina Rogers",
            "Umar Lopez", "Valerie Brooks", "William Sanders", "Xenia Foster", "Yusuf Rivera",
            "Zara Ward", "Amber Bennett", "Brian Price", "Carmen James", "Derek Ross",
            "Elena Cox", "Frank Gray", "Grace Barnes", "Henry Powell", "Isla Russell",
            "Jack Butler", "Kara Simmons", "Liam Watson", "Maya Patterson", "Noah Jenkins",
            "Olivia Long", "Peter Ford", "Queen Vasquez", "Ryan Peterson", "Sophie Hamilton",
            "Trent Stephens", "Uma Dean", "Vince Curtis", "Wendy Stone", "Xander Doyle"
        };

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement imageStmt = conn.prepareStatement("""
                INSERT INTO image (image_id, image_path)
                VALUES (1, '/images/AchefImage.jpeg')
                ON DUPLICATE KEY UPDATE image_path = VALUES(image_path)
            """);
            imageStmt.executeUpdate();
            System.out.println("🖼️ Ensured default image exists with image_id = 1");
        } catch (SQLException e) {
            System.err.println("❌ Failed to insert default image: " + e.getMessage());
        }
        

        for (int i = 0; i < names.length; i++) {
            String fullName = names[i];
            String username = fullName.toLowerCase().replace(" ", ".");

            try {
                String userId;

                if (adminUserService.usernameExists(username)) {
                    System.out.println("⚠️ Skipping existing user: " + username);
                    userId = adminUserService.getUserIdByUsername(username);
                } else {
                    int imageId = 1; // default image
                    boolean success = adminUserService.registerUser(rawPassword, username, imageId);
                    if (!success) {
                        System.out.println("❌ Failed to create user: " + username);
                        continue;
                    }

                    userId = adminUserService.getUserIdByUsername(username);
                    System.out.println("✅ Created user: " + username);
                }

                // Only for first 5 users
                if (i < 5 && userId != null) {
                    createSampleRecipesForUser(userId, i);
                }

            } catch (Exception e) {
                System.out.println("❗ Error processing user " + username + ": " + e.getMessage());
            }
        }
        seedRatings();
    }

    private void createSampleRecipesForUser(String userId, int userIndex) {
        String[] titles = {
            "Spaghetti Carbonara", "Chicken Tikka", "Miso Ramen", "Beef Burrito",
            "Paneer Butter Masala", "Shrimp Pad Thai", "Caesar Salad", "Chili Con Carne",
            "Tuna Casserole", "Pho Bo", "Kimchi Fried Rice", "Stuffed Peppers",
            "Mac and Cheese", "Lamb Gyro", "Pumpkin Soup", "Eggplant Parmesan",
            "Chicken Alfredo", "Vegan Tacos", "Beet Hummus Bowl", "BBQ Pulled Jackfruit"
        };

        String[] directions = {
            "Boil pasta, mix with egg and pancetta, then serve hot.",
            "Marinate chicken in yogurt and spices, grill until done.",
            "Simmer broth, add noodles and toppings. Serve warm.",
            "Wrap beef, beans, rice, and cheese in tortilla. Bake.",
            "Cook paneer in creamy tomato sauce, serve with naan.",
            "Stir-fry shrimp with noodles and pad thai sauce.",
            "Chop romaine, toss with dressing and croutons.",
            "Brown beef and beans, simmer with chili seasoning.",
            "Mix tuna with pasta, bake with cheese topping.",
            "Simmer beef bones, add noodles and herbs.",
            "Fry rice with kimchi and a fried egg on top.",
            "Stuff peppers with rice, beef, and cheese. Bake.",
            "Boil macaroni, stir in cheese sauce. Serve warm.",
            "Grill lamb, serve in pita with tzatziki sauce.",
            "Simmer pumpkin with spices, blend until smooth.",
            "Layer and bake eggplant with sauce and cheese.",
            "Cook pasta, add Alfredo sauce and grilled chicken.",
            "Fill tortillas with veggies and beans. Grill lightly.",
            "Top hummus with beets and chickpeas. Serve cold.",
            "Slow cook jackfruit in BBQ sauce, shred and serve."
        };

        String[] cuisineTypes = {
            "Mediterranean", "Indian", "Mexican", "Italian", "Thai", "Chinese"
        };

        String[] mealTypes = {
            "Breakfast", "Brunch", "Lunch", "Dinner", "Snack", "Dessert"
        };

        int startIndex = userIndex * 4;

        for (int i = 0; i < 4; i++) {
            int idx = startIndex + i;
            String title = titles[idx];
            String direction = directions[idx];
            String imagePath = "/images/" + title.replace(" ", "_") + ".jpg";
            int[] validTimes = {15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95};
            int estimTime = validTimes[(idx + 3) % validTimes.length];
            String mealType = mealTypes[idx % mealTypes.length];
            String cuisineType = cuisineTypes[idx % cuisineTypes.length];

            try {
                if (chefsService.recipeExistsForUser(userId, title)) {
                    System.out.println("⏩ Skipping duplicate recipe for user " + userId + ": " + title);
                    continue;
                }

                boolean success = chefsService.createRecipe(
                    userId, title, direction, estimTime, imagePath, mealType, cuisineType
                );
                if (success) {
                    System.out.println("🍽️ Created recipe: " + title + " (" + cuisineType + ", " + mealType + ", " + estimTime + " min)");
                }
            } catch (Exception e) {
                System.out.println("❌ Error creating recipe for user " + userId + ": " + e.getMessage());
            }
        }
    }

    private void seedRatings() {
        Random random = new Random();
    
        try (Connection conn = dataSource.getConnection()) {
            // Fetch all user IDs
            List<String> userIds = new ArrayList<>();
            try (PreparedStatement userStmt = conn.prepareStatement("SELECT user_id FROM user");
                 ResultSet userRs = userStmt.executeQuery()) {
                while (userRs.next()) {
                    userIds.add(userRs.getString("user_id"));
                }
            }
    
            // Fetch all recipe IDs
            List<String> recipeIds = new ArrayList<>();
            try (PreparedStatement recipeStmt = conn.prepareStatement("SELECT rec_id FROM recipe");
                 ResultSet recipeRs = recipeStmt.executeQuery()) {
                while (recipeRs.next()) {
                    recipeIds.add(recipeRs.getString("rec_id"));
                }
            }
    
            // Insert ratings with bias
            PreparedStatement insertStmt = conn.prepareStatement("""
                INSERT INTO rating (user_id, rec_id, rating)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE rating = VALUES(rating)
            """);
    
            for (int i = 0; i < recipeIds.size(); i++) {
                String recipeId = recipeIds.get(i);
    
                for (String userId : userIds) {
                    int ratingValue;
    
                    // Cluster-based bias
                    if (i < 5) {
                        ratingValue = 4 + random.nextInt(2); // 4–5: High-rated group
                    } else if (i < 10) {
                        ratingValue = 1 + random.nextInt(2); // 1–2: Low-rated group
                    } else if (i < 15) {
                        ratingValue = 2 + random.nextInt(2); // 2–3: Medium-rated group
                    } else {
                        ratingValue = 1 + random.nextInt(5); // 1–5: Random/chaotic group
                    }
    
                    insertStmt.setString(1, userId);
                    insertStmt.setString(2, recipeId);
                    insertStmt.setInt(3, ratingValue);
                    insertStmt.addBatch();
                }
            }
    
            insertStmt.executeBatch();
            System.out.println("⭐ Seeded 1000+ biased ratings.");
        } catch (SQLException e) {
            System.err.println("❌ Error seeding ratings: " + e.getMessage());
        }
    }
    

}

