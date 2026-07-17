package com.example.bankDemo.controller;

import com.example.bankDemo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class WebController {

    private final AccountService accountService;
    // Authentication pages
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        model.addAttribute("welcomeMessage", "Please enter your credentials to access the banking system");

        if (error != null) {
            model.addAttribute("error", "Invalid username or password. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        model.addAttribute("serverTime", LocalDateTime.now());

        return "login"; // -> templates/login.html
    }

    // Main dashboard - overview of everything
    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("pageTitle", "Banking Dashboard");
        model.addAttribute("welcomeMessage", "Welcome to your banking dashboard");
        model.addAttribute("currentTime", LocalDateTime.now());

        return "dashboard"; // -> templates/dashboard.html
    }

    // User profile page - detailed profile management
    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("pageTitle", "User ProfileOld");
        model.addAttribute("welcomeMessage", "Manage your profile information");
        model.addAttribute("currentTime", LocalDateTime.now());

        return "profile"; // -> templates/profile.html
    }

//    // For future: Account details (when you get to this stage)
//    @GetMapping("/account/{accountId}")
//    public String accountDetailsPage(@PathVariable String accountId, Model model) {
//        model.addAttribute("pageTitle", "Account Details");
//        model.addAttribute("accountId", accountId);
//        model.addAttribute("currentTime", LocalDateTime.now());
//
//        return "account-details"; // -> templates/account-details.html
//    }

    // Root redirect
    @GetMapping("/")
    public String homePage(Authentication authentication) {
        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
    @GetMapping("/account/{accountNumber}/details")
    public String accountDetailsPageWithAccountNumber(@PathVariable String accountNumber, Model model) {
        // You will need to call your service layer here to find the ID
        Long accountId = accountService.findAccountIdByAccountNumber(accountNumber);

        if (accountId == null) {
            // Handle case where account number is not found
            // Redirect to an error page or a "not found" page
            return "redirect:/error";
        }

        model.addAttribute("pageTitle", "Account Details");
        model.addAttribute("accountId", accountId);
        model.addAttribute("welcomeMessage", "View your account details and transaction history");

        return "account-details"; // -> templates/account-details.html
    }
    @GetMapping("/statement")
    public String statementsPage(@RequestParam(required = false) Long customerId, @RequestParam("year") int year, @RequestParam("month") int month,  Model model) {
        model.addAttribute("pageTitle", "Statements");
        model.addAttribute("welcomeMessage", "View your statements and transaction history");
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        if (customerId != null) {
            model.addAttribute("customerId", customerId);
        }
        model.addAttribute("currentTime", LocalDateTime.now());

        return "statement";
    }


}