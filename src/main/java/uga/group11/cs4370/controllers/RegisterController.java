package uga.group11.cs4370.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uga.group11.cs4370.services.ImageService;
import uga.group11.cs4370.services.UserService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;
    private final ImageService imageService;

    @Autowired
    public RegisterController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("registration_page");

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        mv.addObject("errorMessage", error);

        return mv;
    }

    private boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 9 &&
                password.matches(".*[A-Z].*") && // at least one uppercase
                password.matches(".*[a-z].*") && // at least one lowercase
                password.matches(".*\\d.*") && // at least one digit
                password.matches(".*[^a-zA-Z0-9].*"); // at least one non-alphanumeric
    }

    @Value("${upload.directory}")
    private String uploadDir;

    @PostMapping
    public String register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("passwordRepeat") String passwordRepeat,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        // Passwords should have at least 3 chars.

        // Password restrictions go here:
        // TEMP
        if (!this.isValidPassword(password)) {
            // If the password is too short redirect to the registration page
            // with an error message.
            String message = URLEncoder.encode(
                    "Passwords must contain 1 uppercase letter, 1 lowercase letter, 1 number, 1 non-aplhanumeric character, and be of length 9 or greater.",
                    "UTF-8");
            // entropy is a minimum of 4 + 2*7 + 1.5 + 6 = 25.5
            return "redirect:/register?error=" + message;
        }
        // End password tests
        System.out.println("here2");
        if (!password.equals(passwordRepeat)) {
            // If the password repeat does not match the password redirect to the
            // registration page
            // with an error message.
            String message = URLEncoder.encode("Passwords do not match.", "UTF-8");
            return "redirect:/register?error=" + message;
        }

        // =====================================================================
        if (file.isEmpty()) {
            // redirectAttributes.addFlashAttribute("message", "Please select a file to
            // upload");
            String message = URLEncoder.encode("Please select a file to upload",
                    StandardCharsets.UTF_8);
            return "redirect:/register?error=" + message;
        }

        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadDir + file.getOriginalFilename());
            Files.write(path, bytes);

            redirectAttributes.addFlashAttribute("message",
                    "File uploaded successfully: " + file.getOriginalFilename());
            System.out.println("Uploaded file: " + file.getOriginalFilename());

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            imageService.storeImage(file);
        } catch (SQLException e) {
            String message = URLEncoder
                    .encode("An error occurred in storeImage SQL: " + e.getMessage(), "UTF-8");
            return "redirect:/register?error=" + message;
        }

        int image_id = 0;
        try {
            image_id = imageService.setImage_id(file);
        } catch (SQLException e) {
            String message = URLEncoder
                    .encode("An error occurred in setUserImage SQL: " + e.getMessage(), "UTF-8");
            return "redirect:/register?error=" + message;
        }
        // =====================================================================

        try {
            System.out.println("here");
            boolean registrationSuccess = userService.registerUser(password,
                    username, image_id);
            if (registrationSuccess && !file.isEmpty()) {
                // If the registration worked redirect to the login page.
                return "redirect:/login";
            } else {
                // If the registration fails redirect to registration page with a message.
                String message = URLEncoder
                        .encode("Registration failed. Please try again.", "UTF-8");
                return "redirect:/register?error=" + message;
            }
        } catch (Exception e) {
            // If the registration fails redirect to registration page with a message.
            String message = URLEncoder
                    .encode("An error occurred: " + e.getMessage(), "UTF-8");
            return "redirect:/register?error=" + message;
        }
    }
}
