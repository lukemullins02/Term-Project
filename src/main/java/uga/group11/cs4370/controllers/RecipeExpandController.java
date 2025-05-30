// === RecipeExpandController.java ===
package uga.group11.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.services.UserService;

@Controller
@RequestMapping("/rexpand")
public class RecipeExpandController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final ChefsService chefsService;

    @Autowired
    public RecipeExpandController(UserService userService, RecipeService recipeService, ChefsService chefsService) {
        this.userService = userService;
        this.recipeService = recipeService;
        this.chefsService = chefsService;
    }

    @GetMapping("/{rec_id}")
    public ModelAndView webpage(@PathVariable("rec_id") String rec_id,
            @RequestParam(name = "error", required = false) String error) {

        System.out.println("The user is attempting to view recipe with id: " + rec_id);
        ModelAndView mv = new ModelAndView("recipe_view_page");
        List<Recipe> recipes = new ArrayList<>();
        

        try {
            recipes = recipeService.getRecipe(rec_id);
            mv.addObject("recipes", recipes);
            int userRating = recipeService.getUserRatingForRecipe(userService.getLoggedInUser().getUserId(), rec_id);
            mv.addObject("userRating", userRating);
        } catch (SQLException e) {
            System.out.println("failed to get recipe");
        }

        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    @GetMapping("/{rec_id}/stars/{count}")
    public String changeRating(@PathVariable("rec_id") String rec_id,
                                @PathVariable("count") int count) {
        System.out.println("The user is attempting to rate recipe with id: " + rec_id + "; and rating: " + count);

        try {
            boolean rateSuccess = recipeService.rateRecipe(count, rec_id, userService.getLoggedInUser());
            if (rateSuccess) {
                return "redirect:/rexpand/" + rec_id;
            } else {
                String message = URLEncoder.encode("Failed to rate the recipe. Please try again.", StandardCharsets.UTF_8);
                return "redirect:/?error=" + message;
            }
        } catch (Exception e) {
            String message = URLEncoder.encode("Failed to rate the recipe. Please try again.", StandardCharsets.UTF_8);
            return "redirect:/rexpand/" + rec_id + "?error=" + message;
        }
    }
}
