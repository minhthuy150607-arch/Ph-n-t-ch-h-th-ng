package com.library.controller.admin;

import com.library.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rules")
@RequiredArgsConstructor
public class RuleAdminController {

    private final RuleService ruleService;

    @GetMapping
    public String listRules(Model model) {
        model.addAttribute("rules", ruleService.getAllRules());
        model.addAttribute("activeMenu", "rules");
        model.addAttribute("pageTitle", "Cấu hình Quy định Thư viện");
        return "admin/rules/list";
    }

    @PostMapping("/update")
    public String updateRule(@RequestParam Long id,
                             @RequestParam String ruleValue,
                             @RequestParam(required = false) String description,
                             RedirectAttributes redirectAttributes) {
        try {
            ruleService.updateRule(id, ruleValue, description);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quy định thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/rules";
    }
}
