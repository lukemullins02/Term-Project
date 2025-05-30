package uga.group11.cs4370.models;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private final String recipe_id;
    // private final String chef_id;
    private final String title;
    private final String directions;
    private final String image_path;
    private final int estim_time;
    private final String rating;
    private String meal_type;
    private String cuisine_type;
    private int view_count;
    private boolean is_favorite;

    public Recipe(String recipe_id, String title, String directions, String image_path, int estim_time, String rating, String meal_type, String cuisine_type, int view_count, boolean is_favorite) {
        this.recipe_id = recipe_id;
        // this.chef_id = chef_id;
        this.title = title;
        this.directions = directions;
        this.image_path = image_path;
        this.estim_time = estim_time;
        this.rating = rating;
        this.meal_type = meal_type;
        this.cuisine_type = cuisine_type;
        this.view_count = view_count;
        this.is_favorite = is_favorite;
    }

    public Recipe(String recipe_id, String title, String directions, String image_path, int estim_time, String rating) {

        this.recipe_id = recipe_id;
        // this.chef_id = chef_id;
        this.title = title;
        this.directions = directions;
        this.image_path = image_path;
        this.estim_time = estim_time;
        this.rating = rating;
    }

    public Recipe(String recipe_id, String title, String directions, String image_path, int estim_time, String rating, boolean is_favorite) {

        this.recipe_id = recipe_id;
        // this.chef_id = chef_id;
        this.title = title;
        this.directions = directions;
        this.image_path = image_path;
        this.estim_time = estim_time;
        this.rating = rating;
        this.is_favorite = is_favorite;
    }

    public String getRecipeId() {
        return recipe_id;
    }

    /*
     * public String getChefId() {
     * return chef_id;
     * }
     */
    public String getTitle() {
        return title;
    }

    
    public String getDirections() {
       return directions;
    }
     
    public String getImagePath() {
        return image_path;
    }

    public int getEstimTime() {
        return estim_time;
    }

    public String getRating() {
        return rating;
    }

    public String getMealType() {
        return meal_type;
    }

    public String getCuisineType() {
        return cuisine_type;
    }

    public int getViewCount() {
        return view_count;
    }

    public boolean isFavorite() {
        return is_favorite;
    }

    public List<Boolean> getStars() {
        int roundedStars = 0;
        System.out.println("getStars is called");
        System.out.println("Rating is: " + this.getRating());
        try {
            System.out.println("This rating is: " + this.rating);
            roundedStars = (int) Math.round(Double.parseDouble(this.rating));
            
        } catch (NumberFormatException e) {
            System.out.println("getStars is reached NumberForamatException");
            // default to 0 if invalid
        }
        List<Boolean> starsList = new ArrayList<>();
        for (int i = 0; i < roundedStars; i++) {
            starsList.add(Boolean.TRUE);
            System.out.println(starsList);
        }
        return starsList;
    }
    
    public List<Integer> getStarCounts() {
        List<Integer> counts = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            counts.add(i);
        }
        return counts;
    }
    

}
