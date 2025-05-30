package uga.group11.cs4370.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import uga.group11.cs4370.models.Chef;
import uga.group11.cs4370.services.ChefsService;
import uga.group11.cs4370.services.UserService;

@Controller
@RequestMapping("/chefs")
public class ChefsController {
    private final ChefsService chefsService;
    private final UserService userService;

    @Autowired
    public ChefsController(ChefsService chefsService, UserService userService) {
        this.chefsService = chefsService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("chefs_page");

        try {
            List<Chef> subscribableChefs = chefsService.getChefsWithSubStatus();
            mv.addObject("chefs", subscribableChefs);
        } catch (SQLException e) {
            System.out.println("Failed followable chefs");
        }

        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    @GetMapping("{user_id}/sub/{isSubbed}")
    public String followUnfollowUser(@PathVariable("user_id") String user_id,
            @PathVariable("isSubbed") Boolean isSubbed) {
        // System.out.println("User is attempting to sub/unsub a chef:");
        // System.out.println("\tuser_id: " + user_id);
        // System.out.println("\tisSubbed: " + isSubbed);

        try {
            String loggedInUserId = userService.getLoggedInUser().getUserId();
            chefsService.updateFollowStatus(loggedInUserId, user_id, isSubbed);
        } catch (SQLException e) {
            return "redirect:/chefs?error=Could not update sub status.";
        }

        return "redirect:/chefs";
    }
}