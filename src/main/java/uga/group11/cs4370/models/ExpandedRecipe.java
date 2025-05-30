package uga.group11.cs4370.models;

public class ExpandedRecipe extends Recipe {
    private final String directions;

    public ExpandedRecipe(String recipe_id, String title, String imagePath, int estim_time, String rating,
            String directions) {
        super(recipe_id, title, directions, imagePath, estim_time, rating);
        this.directions = directions;
    }

    public String getDirections() {
        return directions;
    }
}
