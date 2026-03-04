package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.entity.User;
import com.railwayticketsystem.railwayticketsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", currentUser);
        model.addAttribute("registerRequest", new RegisterRequest());
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @ModelAttribute RegisterRequest req,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(currentUser, req);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/profile";
        }
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(@AuthenticationPrincipal User currentUser) {
        userService.deleteProfile(currentUser.getId());
        return "redirect:/logout";
    }
}