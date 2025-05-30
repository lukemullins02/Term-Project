package uga.group11.cs4370.models;

public class User {
    private String user_id; // username
    private String username; // password
    private int image_id;
    private String image_path;
    // static final String DEFAULT_PROFILE_IMAGE_PATH =
    // "https://example.com/default_profile_image.png";

    public User(String user_id, String username, int image_id, String image_path) {
        this.user_id = user_id;
        this.username = username;
        this.image_id = image_id;
        this.image_path = image_path;
    }

    public User(String user_id, String username, int image_id) {
        this.user_id = user_id;
        this.username = username;
        this.image_id = image_id;
    }

    public User(String user_id, String username) {
        this.user_id = user_id;
        this.username = username;
    }

    public User() {
        this.user_id = "";
        this.username = "";
        this.image_id = 0;
        this.image_path = null;
    }

    public String getUserId() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public int getUserImage_id() {
        return image_id;
    }

    public String getUserImage_path() {
        return image_path;
    }
}
