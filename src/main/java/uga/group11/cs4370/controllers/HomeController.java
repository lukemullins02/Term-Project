package uga.group11.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.UserService;
import uga.group11.cs4370.models.Recipe;

@Controller
@RequestMapping
public class HomeController {

    private final ChefsService chefsService;
    private final UserService userService;

    @Autowired
    public HomeController(ChefsService chefsService, UserService userService) {
        this.chefsService = chefsService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("home_page");

        List<Recipe> recipes = new ArrayList<>();
        String user_id = userService.getLoggedInUser().getUserId();

        try {
            recipes = chefsService.getCreatedRecipes(user_id);
        } catch (SQLException e) {
            System.out.println("Failed to retrieve recipes");
        }

        mv.addObject("isCurrentUser", true);
        mv.addObject("user_id", user_id);
        mv.addObject("recipes", recipes);
        mv.addObject("currentPath", "/");
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;

    }
}
