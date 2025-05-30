-- Create the database.
create database if not exists cs4370p4;

-- Use the created database.
use cs4370p4;

-- Drop tables if they already exist, in reverse dependency order
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS subscription;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS image;

-- 1. image table
CREATE TABLE image (
    image_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    image_path VARCHAR(255) NOT NULL
);

-- 2. user table
CREATE TABLE user (
    user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    image_id INT,
    FOREIGN KEY (image_id) REFERENCES image(image_id)
);

-- 3. recipe table
CREATE TABLE recipe (
    rec_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    title VARCHAR(255) NOT NULL,
    directions TEXT,
    image_path VARCHAR(255),
    estim_time INT,
    meal_type VARCHAR(50),
    cuisine_type VARCHAR(50),
    view_count INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

-- 4. rating table
create table Rating (
    user_id INT,
    rec_id INT,
    rating INT,
    PRIMARY KEY (user_id, rec_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (rec_id) REFERENCES recipe(rec_id)
);

-- 5. subscription table
CREATE TABLE subscription (
    subscriber_id INT NOT NULL,
    subscribed_id INT NOT NULL,
    PRIMARY KEY (subscriber_id, subscribed_id),
    FOREIGN KEY (subscriber_id) REFERENCES user(user_id),
    FOREIGN KEY (subscribed_id) REFERENCES user(user_id)
);

-- 6. favorite table
CREATE TABLE favorite (
    user_id INT NOT NULL,
    rec_id INT NOT NULL,
    PRIMARY KEY (user_id, rec_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (rec_id) REFERENCES recipe(rec_id)
);
