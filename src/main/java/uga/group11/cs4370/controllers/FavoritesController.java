package uga.group11.cs4370.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.services.UserService;


@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    private final ChefsService chefsService;
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public FavoritesController(ChefsService chefsService, UserService userService, RecipeService recipeService) {
        this.chefsService = chefsService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("favorites_page");
        List<Recipe> recipes = new ArrayList<>();
        
        try {
            recipes = recipeService.getFavoriteRecipe(userService.getLoggedInUser().getUserId());

        } catch (SQLException e) {
            System.out.println("Failed to retrieve recipes");
        }
        mv.addObject("recipes", recipes);
        mv.addObject("currentPath", "/favorites");
        return mv;
        
    }
}