package com.library.controller.admin;

import com.library.model.User;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AccountAdminController {

    private final UserService userService;

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", userService.getAllAccounts());
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("pageTitle", "Quản lý Tài khoản Hệ thống");
        return "admin/accounts/list";
    }

    @GetMapping("/new")
    public String newAccountForm(Model model) {
        model.addAttribute("account", new User());
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("pageTitle", "Thêm Tài khoản Mới");
        return "admin/accounts/form";
    }

    @PostMapping("/new")
    public String saveAccount(@ModelAttribute User account,
                              @RequestParam(value = "rawPassword", required = false) String rawPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.saveAccount(account, rawPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/edit/{id}")
    public String editAccountForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.getUserById(id)
                .map(user -> {
                    model.addAttribute("account", user);
                    model.addAttribute("activeMenu", "accounts");
                    model.addAttribute("pageTitle", "Sửa tài khoản: " + user.getUsername());
                    return "admin/accounts/form";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
                    return "redirect:/admin/accounts";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateAccount(@PathVariable Long id,
                                @ModelAttribute User account,
                                @RequestParam(value = "rawPassword", required = false) String rawPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            account.setId(id);
            userService.saveAccount(account, rawPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/reset-password/{id}")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam(defaultValue = "123456") String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu về '" + newPassword + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
