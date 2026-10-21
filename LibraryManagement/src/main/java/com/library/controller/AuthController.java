package com.library.controller;

import com.library.dto.RegisterDto;
import com.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "registered", required = false) Boolean registered,
                        Model model) {
        model.addAttribute("pageTitle", "Đăng nhập");
        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }
        return "client/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("pageTitle", "Đăng ký Tài khoản Bạn đọc");
        return "client/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Đăng ký Tài khoản Bạn đọc");
            return "client/register";
        }

        try {
            userService.registerReader(registerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Đăng ký Tài khoản Bạn đọc");
            return "client/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Truy cập bị từ chối");
        return "client/access-denied";
    }
}
