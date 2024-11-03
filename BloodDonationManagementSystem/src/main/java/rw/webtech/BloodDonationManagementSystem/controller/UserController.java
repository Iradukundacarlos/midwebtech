package rw.webtech.BloodDonationManagementSystem.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import rw.webtech.BloodDonationManagementSystem.model.Role;
import rw.webtech.BloodDonationManagementSystem.model.User;
import rw.webtech.BloodDonationManagementSystem.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/home")
    public String home() {
        return "home"; // Home page
    }

    @GetMapping("/about")
    public String showAboutPage() {
        return "about"; // Template name for About Us page
    }

    @GetMapping("/why-donate")
    public String showWhyDonatePage() {
        return "why-donate"; // Template name for Why Donate page
    }

    @GetMapping("/contact")
    public String showContactPage() {
        return "contact"; // Template name for Contact Us page
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Template name for registration
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {

//        if (result.hasErrors()) {
//            return "register"; // If form validation fails, return the registration page
//        }

        // Register user
        userService.registerUser(user);
        model.addAttribute("message", "Registration successful! You can log in now.");
        return "login"; // Redirect to login page
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("error", model.getAttribute("error"));
        return "login"; // Template name for login
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        User user = userService.loginUser(username);

        if (user == null || !user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid username or password");
            return "login"; // Show login page again with error message
        }

        // Set user information in session
        session.setAttribute("loggedInUser", user); // Ensure no space in the attribute name

        // Redirect based on the role
        switch (user.getRole()) {
            case ROLE_ADMIN:
                return "redirect:/admin"; // Redirect to admin dashboard
            case ROLE_MANAGER:
                return "redirect:/manager"; // Redirect to manager page
            case ROLE_USER:
                return "redirect:/user"; // Redirect to user page
            default:
                return "redirect:/login"; // Default redirect if no role matches
        }
    }

    @GetMapping("/admin")
    public String showAdminDashboard(HttpSession session, Model model,
                                     @RequestParam(defaultValue = "0") int pageNo,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(defaultValue = "id") String sortBy) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login"; // Redirect to login if not logged in or not admin
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Page<User> userPage = userService.getAllUsers(pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalUsers", userPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);

        return "admin"; // Return the admin dashboard template
    }

    @GetMapping("/manager")
    public String sellerPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_MANAGER) {
            return "redirect:/login"; // Redirect to login if not logged in or not manager
        }
        return "manager"; // Manager dashboard template
    }

    @GetMapping("/user")
    public String customerPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_USER) {
            return "redirect:/login"; // Redirect to login if not logged in or not customer
        }
        return "user"; // Customer dashboard template
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("Logging out user: " + session.getAttribute("loggedInUser"));

        session.invalidate(); // Invalidate session on logout

        System.out.println("Session invalidated.");

        redirectAttributes.addFlashAttribute("message", "You are logged out.");

        return "redirect:/login"; // Redirect to login after logout
    }

    @GetMapping("/e-showroom")
    public String showEshowroomPage() {
        return "e-showroom"; // E-showroom page
    }
}
