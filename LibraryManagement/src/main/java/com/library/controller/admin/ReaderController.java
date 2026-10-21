package com.library.controller.admin;

import com.library.model.User;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/readers")
@RequiredArgsConstructor
public class ReaderController {

    private final UserService userService;

    @GetMapping
    public String listReaders(Model model) {
        model.addAttribute("readers", userService.getAllReaders());
        model.addAttribute("activeMenu", "readers");
        model.addAttribute("pageTitle", "Quản lý Bạn đọc");
        return "admin/readers/list";
    }

    @GetMapping("/new")
    public String newReaderForm(Model model) {
        model.addAttribute("reader", new User());
        model.addAttribute("activeMenu", "readers");
        model.addAttribute("pageTitle", "Thêm Bạn đọc mới");
        return "admin/readers/form";
    }

    @PostMapping("/new")
    public String saveReader(@ModelAttribute User reader,
                             @RequestParam(value = "rawPassword", required = false) String rawPassword,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.saveReader(reader, rawPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bạn đọc thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/readers";
    }

    @GetMapping("/edit/{id}")
    public String editReaderForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.getUserById(id)
                .map(reader -> {
                    model.addAttribute("reader", reader);
                    model.addAttribute("activeMenu", "readers");
                    model.addAttribute("pageTitle", "Sửa Bạn đọc: " + reader.getFullName());
                    return "admin/readers/form";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bạn đọc!");
                    return "redirect:/admin/readers";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateReader(@PathVariable Long id,
                               @ModelAttribute User reader,
                               @RequestParam(value = "rawPassword", required = false) String rawPassword,
                               RedirectAttributes redirectAttributes) {
        reader.setId(id);
        userService.saveReader(reader, rawPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/admin/readers";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/readers";
    }
}
