package com.railwayticketsystem.railwayticketsystem.controller;

import com.railwayticketsystem.railwayticketsystem.dto.RegisterRequest;
import com.railwayticketsystem.railwayticketsystem.dto.OtpRequest;
import com.railwayticketsystem.railwayticketsystem.service.UserService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final HttpSession session;

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String initiateRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest req,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.initiateRegistration(req);
            session.setAttribute("tempRegisterRequest", req);
            return "redirect:/otp-verify";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/otp-verify")
    public String showOtpPage(Model model) {
        model.addAttribute("otpRequest", new OtpRequest());
        return "otp-verify";
    }

    @PostMapping("/otp-verify")
    public String verifyOtp(@ModelAttribute("otpRequest") OtpRequest otpReq,
                            RedirectAttributes redirectAttributes) {
        RegisterRequest req = (RegisterRequest) session.getAttribute("tempRegisterRequest");
        if (req == null) {
            return "redirect:/register";
        }
        try {
            userService.completeRegistration(req, otpReq.getOtp());
            session.removeAttribute("tempRegisterRequest");
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/otp-verify";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}