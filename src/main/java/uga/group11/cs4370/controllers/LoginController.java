package uga.group11.cs4370.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

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

import uga.group11.cs4370.services.UserService;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("login_page");

        // Log out if the user is already logged in.
        userService.unAuthenticate();

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        mv.addObject("errorMessage", error);

        return mv;
    }

    @PostMapping
    public String login(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        boolean isAuthenticated = false;

        try {
            isAuthenticated = userService.authenticate(username, password);
        } catch (SQLException e) {
            // Redirect back to the login page with an error message if authentication
            // fails.
            String message = URLEncoder.encode("Authentication failed. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/login?error=" + message;
        }

        if (isAuthenticated) {
            // Redirect to home page if authentication is successful.
            return "redirect:/";
        } else {
            // Redirect back to the login page with an error message if authentication
            // fails.
            String message = URLEncoder.encode("Invalid username or password. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/login?error=" + message;
        }
    }
}
