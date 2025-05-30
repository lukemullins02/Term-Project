--ADMIN SERVICE:
--We don’t use anything in this
--Was used for tests not final product

--CHEFS SERVICE:
--insert user recipe data into recipe table
--http://localhost:8081/recipe
"insert into recipe  insert(user_id, title, directions, estim_time, image_path, meal_type,
cuisine_type, view_count) values (?,?,?,?,?,?,?,?)"

--Retrieve recipe information 
--http://localhost:8081
"select distinct r.title, r.directions, r.estim_time, r.rec_id, r.image_path, r.meal_type,
r.cuisine_type, r.view_count  from user u, recipe r, subscription s where u.user_id = ? and
u.user_id = r.user_id or u.user_id = r.user_id and r.user_id = some (select distinct s.subscribed_id
from user u, subscription s where u.user_id = ? and u.user_id = s.subscriber_id) order by rec_id desc"

--Get all chefs that aren’t the current user and check follow status. Also join to image
--http://localhost:8081/chefs
"SELECT u.user_id, u.username, u.image_id, i.image_path, " +
                           "EXISTS (SELECT 1 FROM subscription s " +
                           "        WHERE s.subscriber_id = ? AND s.subscribed_id = u.user_id) AS isSubbed " +
                           "FROM user u " +
                           "JOIN image i ON u.image_id = i.image_id " +
                           "WHERE u.user_id != ?"

--Updates follow status when clicking subscribe button (Dynamic query)
--http://localhost:8081/chefs
--@GetMapping("{user_id}/sub/{isSubbed}")
--Note this very briefly hit the mapping
/*final String sql = isSubbed
                ? "INSERT INTO subscription (subscriber_id, subscribed_id) VALUES (?, ?)"
                : "DELETE FROM subscription WHERE subscriber_id = ? AND subscribed_id = ?"*/

--IMAGE SERVICE:
--Insert images into image table
--http://localhost:8081/register
--http://localhost:8081/recipe
--http://localhost:8081/profile/2/true
"INSERT INTO image (image_path) VALUES (?)"

--Match user and picture image_ids
--http://localhost:8081/register
"SELECT image_id FROM image WHERE image_path = ?"

--Helper for placing user image_id into next query ‘?’
--http://localhost:8081/profile/2/true
"select image_id from user where user_id = ?"

--Map new image to old fk from prev image in user
--http://localhost:8081/profile/2/true
"UPDATE image SET image_path = ? WHERE image_id = ?"

--RECIPE SERVICE:
--Get a rating on recipe based on all user average ratings
--Helper in getRecipe, getUserExpRecipes, getUserRecipes
"select round(avg(rating), 1) as average_rating from rating where rec_id = ?;"

--Each time page refresh occurs view count is updated
--http://localhost:8081/rexpand/3
"UPDATE recipe SET view_count = view_count + 1 WHERE rec_id = ?;"

--Get a recipe based on id
--http://localhost:8081/rexpand/3
"select * from recipe where rec_id = ?"

--Add a rating
--http://localhost:8081/rexpand/3
--@GetMapping("/{rec_id}/stars/{count}")
--Note this very briefly hit the mapping
        """
            INSERT INTO rating (user_id, rec_id, rating)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE rating = VALUES(rating);
        """
--Get recipes of specific user for profile view
--Helper to getChefsWithSubStatus
--http://localhost:8081/profile/2/true
"select * from recipe where user_id = ?;"

--Favorite a recipe
--http://localhost:8081/
--@GetMapping("/{rec_id}/favorite/{isAdd}")
"INSERT INTO Favorite (rec_id, user_id) VALUES (?, ?)"

--Favorite a recipe
--http://localhost:8081/
--@GetMapping("/{rec_id}/favorite/{isAdd}")
"DELETE FROM Favorite WHERE rec_id = ? AND user_id = ?"

--Determines boolean value for above path
--Help to favoriteRecipe
"SELECT 1 FROM Favorite WHERE rec_id = ? AND user_id = ? LIMIT 1"

--Displays favorite recipes on favorites page
--http://localhost:8081/favorites
       """
                SELECT r.*, ROUND(AVG(rt.rating), 1) AS avg_rating 
                FROM recipe r
                JOIN favorite f ON r.rec_id = f.rec_id
                LEFT JOIN rating rt ON r.rec_id = rt.rec_id
                WHERE f.user_id = ?
                GROUP BY r.rec_id;
        """
--Tells chef if recipe is favorited
--Helper to getCreatedRecipes
"select count(*) as row_count from favorite where rec_id = ? and user_id = ?"

--Helps display the user rating in extended view
--http://localhost:8081/rexpand/3
"SELECT rating FROM rating WHERE user_id = ? AND rec_id = ?"

--SEARCH SERVICE:
--Search Dynamic Query to help search recipes based on multiple fields
--http://localhost:8081/search GET
--http://localhost:8081/searchresults POST
--Example:
"SELECT recipe.*, AVG(rating.rating) AS avg_rating FROM recipe JOIN subscription ON recipe.user_id =
subscription.subscribed_id LEFT JOIN rating ON recipe.rec_id = rating.rec_id WHERE subscription.subscriber_id =
'2' AND recipe.estim_time <= 60 AND recipe.meal_type = 'Lunch' AND recipe.cuisine_type = 'Indian' GROUP BY
recipe.rec_id HAVING AVG(rating.rating) >= 2.0 ORDER BY recipe.view_count DESC LIMIT 10"
--Implementation in Java:
           /*boolean favoritesOnly,
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
        }*/

--USER SERVICE:
--get all user table information of the user with the username passed in
--http://localhost:8081/login
"select * from user where username = ?;"

--insert new user into user table with the passed in user info
--http://localhost:8081/register
"insert into user (password, username, image_id) values (?, ?, ?)"

--Get individual user profile
--http://localhost:8081/profile/1/false
"select * from user u, image i where u.image_id = i.image_id"
                + " and u.user_id = ?"
