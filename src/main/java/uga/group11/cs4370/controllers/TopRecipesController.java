/*package uga.group11.cs4370.controllers;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.models.ExpandedRecipe;
import uga.group11.cs4370.models.Recipe;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.services.UserService;

@Controller
@RequestMapping("/toprecipes")
public class TopRecipesController {
    
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public TopRecipesController(UserService userService, RecipeService recipeService) {
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ModelAndView showTopRecipes() throws SQLException {
        //goal is to return a list of the top 5 recipes in terms of rating
        ModelAndView mv = new ModelAndView("top_recipes_page");

        List<ExpandedRecipe> recipes = recipeService.getTopRecipes();

        mv.addObject("recipes", recipes);

        if (recipes.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        return mv;
    }

}
 */