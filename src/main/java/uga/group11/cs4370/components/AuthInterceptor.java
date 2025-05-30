package uga.group11.cs4370.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uga.group11.cs4370.services.UserService;

/**
 * This class intercepts requests that goes into controllers.
 * The intercepted requests are redirected to the login page if the
 * user is not logged in.
 * The intercepter is selectively applied to different URL patterns.
 * See WebConfig.java.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // This service enables user services.
    private UserService userService;

  
    @Autowired
    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (!userService.isAuthenticated()) {
            // Redirect the user to login.
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
  
}
