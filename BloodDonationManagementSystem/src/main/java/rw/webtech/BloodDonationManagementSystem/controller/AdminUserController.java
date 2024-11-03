package rw.webtech.BloodDonationManagementSystem.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rw.webtech.BloodDonationManagementSystem.model.Role;
import rw.webtech.BloodDonationManagementSystem.model.User;
import rw.webtech.BloodDonationManagementSystem.service.UserService;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/users")
//    public String showUserManagement(Model model,
//                                      @RequestParam(defaultValue = "0") int pageNo,
//                                      @RequestParam(defaultValue = "5") int pageSize,
//                                      @RequestParam(defaultValue = "id") String sortBy) {
//        // Use the pageable method to get a page of users
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        Page<User> userPage = userService.getAllUsers(pageable);
//
//        model.addAttribute("users", userPage.getContent());
//        model.addAttribute("currentPage", pageNo);
//        model.addAttribute("totalPages", userPage.getTotalPages());
//        model.addAttribute("totalUsers", userPage.getTotalElements());
//        model.addAttribute("sortBy", sortBy);
//        return "user-management"; // Return the user management template
//    }

    @GetMapping("/users")
    public String showUserManagement(Model model,
                                     @RequestParam(defaultValue = "0") int pageNo,
                                     @RequestParam(defaultValue = "5") int pageSize,
                                     @RequestParam(defaultValue = "id") String sortBy) {
        // Use the pageable method to get a page of users with sorting
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Page<User> userPage = userService.getAllUsers(pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalUsers", userPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        return "user-management"; // Return the user management template
    }


    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User()); // Creates a new User object for form binding
        return "add-user"; // Directs to the add-user.html template in templates directory
    }

    @PostMapping("/users/add")
    public String addUser (@ModelAttribute User user) {
        userService.registerUser (user); // Save the user using your service
        return "redirect:/admin/users"; // Redirect to the user management page after saving
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id); // Fetch the user by ID
        model.addAttribute("user", user); // Add the user to the model
        return "edit-user"; // Return the edit user template
    }

    @PostMapping("/users/update")
    public String updateUser (@ModelAttribute User user) {
        userService.updateUser (user); // Call the service to update the user
        return "redirect:/admin/users"; // Redirect to the user management page after updating
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser (@PathVariable Long id) {
        userService.deleteUser (id); // Call the service to delete the user
        return "redirect:/admin/users"; // Redirect to the user management page after deletion
    }

    @GetMapping("/search")
    public String showSearchForm() {
        return "search-user"; // Return the search user template
    }

    @GetMapping("/search/results")
    public String searchUsers(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String email,
                              Model model) {
        List<User> users = userService.searchUsers(username, email); // Call the service to search for users
        model.addAttribute("users", users); // Add the list of users to the model
        return "user-list"; // Return the template that displays the list of users
    }

    @GetMapping("/download/users")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadUsers() throws IOException {
        List<User> users = userService.getAllUsers(); // Fetch all users from the service

        // Create CSV content
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        // Write CSV header
        writer.println("ID,Username,Email"); // Adjust according to your User fields

        // Write user data
        for (User  user : users) {
            writer.printf("%d,%s,%s%n", user.getId(), user.getUsername(), user.getEmail()); // Adjust according to your User fields
        }
        writer.flush();
        writer.close();

        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        // Set the content type and attachment header
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment ;filename=users.csv");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(resource.contentLength())
                .headers(headers)
                .body(resource);
    }



    // Handle GET requests for the user upload endpoint
    @GetMapping("/upload/users")
    public String showUserUploadForm(Model model) {
        model.addAttribute("userMessage", "Please use the form to upload user data.");
        return "upload"; // Display the upload page with a message
    }

    // Handle POST requests for user upload
    @PostMapping("/upload/users")
    public String uploadUsers(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("userMessage", "Please select a file to upload.");
            return "upload";
        }

        try {
            List<User> userList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            reader.readLine(); // Skip header line

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                User user = new User();
                user.setUsername(data[0]);
                user.setFirstName(data[1]);
                user.setLastName(data[2]);
                user.setEmail(data[3]);
                user.setPhoneNumber(data[4]);
//                user.setProfilePicture(data[5]);
                user.setRole(Role.valueOf(data[5]));
                userList.add(user);
            }

            userService.saveAll(userList);
            model.addAttribute("userMessage", "User file uploaded successfully!");
            return "redirect:/admin";
        } catch (IOException | IllegalArgumentException e) {
            model.addAttribute("userMessage", "Failed to upload user file: " + e.getMessage());
        }

        return "upload";
    }

    @GetMapping("/notifications")
    public String showNotificationPage() {
        return "notification"; // Return the notification user template
    }


    @GetMapping("/admin/user-role-stats")
    @ResponseBody
    public Map<String, Integer> getUserRoleStatistics() {
        List<User> users = userService.getAllUsers(); // Fetch all users from the service
        Map<String, Integer> roleStats = new HashMap<>();

        // Count users per role
        for (User  user : users) {
            String role = user.getRole().name(); // Assuming getRole() returns a Role enum
            roleStats.put(role, roleStats.getOrDefault(role, 0) + 1);
        }

        return roleStats; // Return the statistics as a JSON response
    }




}