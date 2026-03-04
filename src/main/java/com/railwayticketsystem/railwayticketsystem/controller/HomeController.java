package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.entity.Booking;
import com.railwayticketsystem.railwayticketsystem.entity.Role;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";           // landing page
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);

        if (currentUser.getRole() == Role.ADMIN) {
            return "admin/dashboard";
        }
        return "user/dashboard";   // user dashboard
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout";
    }

}