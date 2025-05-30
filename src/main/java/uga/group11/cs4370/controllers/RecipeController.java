package uga.group11.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.RecipeService;
import uga.group11.cs4370.services.UserService;

@Controller
@RequestMapping("/recipe")
public class RecipeController {

    private final ChefsService chefsService;
    private final UserService userService;
    private final RecipeService recipeService;

    @Autowired
    public RecipeController(ChefsService chefsService, UserService userService, RecipeService recipeService) {
        this.chefsService = chefsService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("recipe_page");

        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;

    }

    @Value("${upload.directory}")
    private String uploadDir;

    @PostMapping("/createrecipe")
    public String createRecipe(@RequestParam(name = "mealType") String mealType,
            @RequestParam(name = "cuisineType") String cuisineType,
            @RequestParam(name = "titleText") String titleText,
            @RequestParam(name = "directionsText") String directionsText,
            @RequestParam(name = "timeText") String timeText, @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {

        System.out.println("User is creating recipe: " + titleText);

        // Redirect the user if the post creation is a success.
        // return "redirect:/";
        if (titleText == "") {
            String message = URLEncoder.encode("Failed to create the Recipe. Please input information into each box.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }
        if (directionsText == "") {
            String message = URLEncoder.encode("Failed to create the Recipe. Please input information into each box.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }
        if (timeText == "") {
            String message = URLEncoder.encode("Failed to create the Recipe. Please input information into each box.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }
        if (mealType == "") {
            String message = URLEncoder.encode("Failed to create the Recipe. Please input information into each box.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }
        if (cuisineType == "") {
            String message = URLEncoder.encode("Failed to create the Recipe. Please input information into each box.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }

        int numText = Integer.parseInt(timeText);

        if (file.isEmpty()) {
            // redirectAttributes.addFlashAttribute("message", "Please select a file to
            // upload");
            String message = URLEncoder.encode("Please select a file to upload",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
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

        String image_path = "/uploads" + file.getOriginalFilename();

        try {
            boolean recipeSuccess = chefsService.createRecipe(userService.getLoggedInUser().getUserId(), titleText,
                    directionsText, numText, image_path, mealType, cuisineType);
            // boolean recipeSuccess =
            // chefsService.createRecipe(userService.getLoggedInUser().getUserId(),titleText,directionsText,numText);
            if (recipeSuccess) {
                return "redirect:/";
            } else {
                String message = URLEncoder.encode("Failed to create the Recipe. Please try again.",
                        StandardCharsets.UTF_8);
                return "redirect:/recipe?error=" + message;
            }
        } catch (Exception e) {
            // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to create the Recipe. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/recipe?error=" + message;
        }
    }

    // @GetMapping("/{rec_id}/favorite/{isAdd}")
    // public String addOrRemovefavorite(@PathVariable("rec_id") String rec_id,
    //         @PathVariable("isAdd") Boolean isAdd) {
    //     System.out.println("The user is attempting add or remove a favorite:");
    //     System.out.println("\trec_id: " + rec_id);
    //     System.out.println("\tisAdd: " + isAdd);

    //     // Redirect the user if the comment adding is a success.
    //     // return "redirect:/favorite/" + rec_id;
    //     try {

    //         if (userService.getLoggedInUser() == null) {
    //             System.out.println("User not logged in.");
    //             return "redirect:/login";
    //         }

    //         boolean favoriteSuccess = recipeService.favoriteRecipe(isAdd, rec_id, userService.getLoggedInUser());
    //         if (favoriteSuccess) {
    //             return "redirect:/recipe/" + rec_id;
    //         } else {
    //             String message = URLEncoder.encode("Failed to create the favorite. Please try again.",
    //                     StandardCharsets.UTF_8);
    //             return "redirect:/?error=" + message;
    //         }
    //     } catch (Exception e) {
    //         // Redirect the user with an error message if there was an error.
    //         e.printStackTrace();
    //         String message = URLEncoder.encode("Failed to (un)favorite the post. Please try again.",
    //                 StandardCharsets.UTF_8);
    //         return "redirect:/recipe/" + rec_id + "?error=" + message;
    //     }
    // }

    @GetMapping("/{rec_id}/favorite/{isAdd}")
    public String addOrRemovefavorite(@PathVariable("rec_id") String rec_id,
            @PathVariable("isAdd") Boolean isAdd,
            @RequestParam(name = "redirectUrl", required = false, defaultValue = "/") String redirectUrl) {

        System.out.println("The user is attempting add or remove a favorite:");
        System.out.println("\trec_id: " + rec_id);
        System.out.println("\tisAdd: " + isAdd);

        try {
            if (userService.getLoggedInUser() == null) {
                return "redirect:/login";
            }

            boolean favoriteSuccess = recipeService.favoriteRecipe(isAdd, rec_id, userService.getLoggedInUser());
            if (favoriteSuccess) {
                return "redirect:" + redirectUrl;
            } else {
                String message = URLEncoder.encode("Failed to favorite. Try again.", StandardCharsets.UTF_8);
                return "redirect:" + redirectUrl + "?error=" + message;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Error while favoriting.", StandardCharsets.UTF_8);
            return "redirect:" + redirectUrl + "?error=" + message;
        }
    }

}
