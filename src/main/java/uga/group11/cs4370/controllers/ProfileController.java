package uga.group11.cs4370.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uga.group11.cs4370.services.UserService;
import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.ImageService;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.models.ExpandedRecipe;
import uga.group11.cs4370.models.User;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final RecipeService recipeService;
    private final ChefsService chefsService;
    private final ImageService imageService;

    @Autowired
    public ProfileController(UserService userService, RecipeService recipeService,
            ChefsService chefsService, ImageService imageService) {
        this.userService = userService;
        this.recipeService = recipeService;
        this.chefsService = chefsService;
        this.imageService = imageService;
    }

    // @GetMapping /*("/{isCurrentUser}")*/
    // public ModelAndView profileOfLoggedInUser(/*@PathVariable("isCurrentUser")
    // boolean isCurrentUser,*/
    // @RequestParam(name = "error", required = false) String error) throws
    // SQLException {
    // ModelAndView mv = new ModelAndView("profile_page");
    // String user_id = userService.getLoggedInUser().getUserId();

    // //mv.addObject("isCurrentUser", isCurrentUser);

    // User user = userService.getUser(user_id);
    // mv.addObject("user", user);

    // List<ExpandedRecipe> recipes = recipeService.getUserExpRecipes(user_id);
    // mv.addObject("recipes", recipes);

    // if (recipes.isEmpty()) {
    // mv.addObject("isNoContent", true);
    // }

    // String errorMessage = error;
    // mv.addObject("errorMessage", errorMessage);
    // return mv;
    // }

    @GetMapping("/{user_id}/{isCurrentUser}")
    public ModelAndView profileOfSpecificUser(@PathVariable("user_id") String user_id,
            @PathVariable("isCurrentUser") boolean isCurrentUser) throws SQLException {
        ModelAndView mv = new ModelAndView("profile_page");
        System.out.println("User is attempting to view profile: " + user_id);

        mv.addObject("isCurrentUser", isCurrentUser);

        User user = userService.getUser(user_id);
        mv.addObject("user", user);

        List<ExpandedRecipe> recipes = recipeService.getUserExpRecipes(user_id);
        mv.addObject("recipes", recipes);

        if (recipes.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        return mv;
    }

    @Value("${upload.directory}")
    private String uploadDir;

    @PostMapping /*("/{user_id}/{isCurrentUser}")*/
    public String updateUserImage(/*@PathVariable("user_id") String user_id,
            @PathVariable("isCurrentUser") boolean isCurrentUser,*/ @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
                String user_id = userService.getLoggedInUser().getUserId();
                System.out.println( user_id + "updateUserImage");
        if (file.isEmpty()) {
            String message = URLEncoder.encode("Please select a file to upload",
                    StandardCharsets.UTF_8);
            return "redirect:/profile/" + user_id + "/true?error=" + message;
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
            int image_id = imageService.getUserImage_id(user_id);
            imageService.updateImage(image_id, file);
        } catch (SQLException e) {
            String message = URLEncoder
                    .encode("An error occurred in updateImage SQL: " + e.getMessage(), "UTF-8");
            return "redirect:/profile/" + user_id + "/true?error=" + message;
        }

        return "redirect:/profile/" + user_id + "/true";
    }
}
