package uga.group11.cs4370.controllers;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.models.ExpandedRecipe;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.models.User;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.services.UserService;
import uga.group11.cs4370.services.SearchService;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final SearchService searchService;

    @Autowired
    public SearchController(UserService userService, RecipeService recipeService, SearchService searchService) {
        this.searchService = searchService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("search_page");
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);
        return mv;
    }

    @PostMapping("/searchresults")
    public ModelAndView search(
            @RequestParam(name = "favoritesOnly", required = false, defaultValue = "false") boolean favoritesOnly,
            @RequestParam(name = "followingOnly", required = false, defaultValue = "false") boolean followingOnly,
            @RequestParam(name = "lessThanTime", required = false) Integer lessThanTime,
            @RequestParam(name = "overRating", required = false) Float overRating,
            @RequestParam(name = "mealType", required = false) String mealType,
            @RequestParam(name = "cuisineType", required = false) String cuisineType,
            //@RequestParam(name = "orderChef", required = false) String orderChef,
            @RequestParam(name = "orderRecipe", required = false) String orderRecipe,
            @RequestParam(name = "limitResults", required = false) Integer limitResults,
            @RequestParam(name = "error", required = false) String error) {
        
        System.out.println("Favorites Only: " + favoritesOnly);
        System.out.println("Following Only: " + followingOnly);
        System.out.println("Less Than Time: " + lessThanTime);
        System.out.println("Over Rating: " + overRating);
        System.out.println("Meal Type: " + mealType);
        System.out.println("Cuisine Type: " + cuisineType);
        //System.out.println("Order Chef: " + orderChef);
        System.out.println("Order Recipe: " + orderRecipe);
        System.out.println("Limit Results: " + limitResults);

        ModelAndView mv = new ModelAndView("search_results_page");
        String errorMessage = error;

        System.out.println("User is searching");

        try {
            /*if (orderChef.compareTo("") != 0) {
                List<User> users = searchService.searchChefs(favoritesOnly, followingOnly, lessThanTime, overRating,
                        mealType, cuisineType, orderChef, limitResults, userService.getLoggedInUser().getUserId());
                mv.addObject("users", users);
            } else */
            if (orderRecipe.compareTo("") != 0) {
                List<Recipe> recipes = searchService.searchRecipes(favoritesOnly, followingOnly, lessThanTime,
                        overRating, mealType, cuisineType, orderRecipe, limitResults, userService.getLoggedInUser().getUserId());
                mv.addObject("recipes", recipes);
            } else {
                System.out.println("No search criteria provided.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        mv.addObject("errorMessage", errorMessage);
        return mv;
    }

}