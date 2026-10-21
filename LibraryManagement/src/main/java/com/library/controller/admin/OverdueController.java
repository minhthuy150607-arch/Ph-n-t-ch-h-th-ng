package com.library.controller.admin;

import com.library.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/overdue")
@RequiredArgsConstructor
public class OverdueController {

    private final LoanService loanService;

    @GetMapping
    public String listOverdue(Model model) {
        var overdueList = loanService.getOverdueLoanDetails();
        int finePerDay = loanService.getRuleIntValue("OVERDUE_FINE_PER_DAY", 2000);

        model.addAttribute("overdueList", overdueList);
        model.addAttribute("finePerDay", finePerDay);
        model.addAttribute("activeMenu", "overdue");
        model.addAttribute("pageTitle", "Danh sách Sách quá hạn");
        return "admin/overdue/list";
    }
}
