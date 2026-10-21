package com.library.controller.admin;

import com.library.model.Loan;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;
    private final BookService bookService;

    @GetMapping
    public String listLoans(Model model) {
        model.addAttribute("loans", loanService.getAllLoans());
        model.addAttribute("activeMenu", "loans");
        model.addAttribute("pageTitle", "Quản lý Mượn trả");
        return "admin/loans/list";
    }

    @GetMapping("/quick-return")
    public String quickReturnPage(Model model) {
        model.addAttribute("activeMenu", "loans");
        model.addAttribute("pageTitle", "Quét QR Trả sách Nhanh");
        return "admin/loans/quick-return";
    }

    @GetMapping({"/new", "/form", "/create"})
    public String newLoanForm(Model model) {
        model.addAttribute("readers", userService.getAllReaders());
        model.addAttribute("activeMenu", "loans");
        model.addAttribute("pageTitle", "Tạo Phiếu mượn mới");
        return "admin/loans/form";
    }

    @PostMapping({"/new", "/form", "/create"})
    public String createLoan(@RequestParam Long userId,
                             @RequestParam String barcodes,
                             @RequestParam(required = false) String notes,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Long librarianId = null;
            if (userDetails != null) {
                User librarian = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
                if (librarian != null) librarianId = librarian.getId();
            }

            List<String> barcodeList = Arrays.stream(barcodes.split("[,;\n\r]+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            Loan loan = loanService.createLoan(userId, librarianId, barcodeList, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu mượn thành công! Mã phiếu: " + loan.getLoanCode());
            return "redirect:/admin/loans/" + loan.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/loans/new";
        }
    }

    @GetMapping("/{id}")
    public String loanDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return loanService.getLoanById(id)
                .map(loan -> {
                    model.addAttribute("loan", loan);
                    model.addAttribute("activeMenu", "loans");
                    model.addAttribute("pageTitle", "Chi tiết Phiếu mượn: " + loan.getLoanCode());
                    return "admin/loans/detail";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phiếu mượn!");
                    return "redirect:/admin/loans";
                });
    }

    @PostMapping("/return/{detailId}")
    public String returnBook(@PathVariable Long detailId,
                             @RequestParam Long loanId,
                             @RequestParam(defaultValue = "GOOD") String conditionOnReturn,
                             RedirectAttributes redirectAttributes) {
        try {
            loanService.returnBook(detailId, conditionOnReturn);
            redirectAttributes.addFlashAttribute("successMessage", "Xác nhận trả sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/loans/" + loanId;
    }

    @PostMapping("/renew/{detailId}")
    public String renewBook(@PathVariable Long detailId,
                            @RequestParam Long loanId,
                            RedirectAttributes redirectAttributes) {
        try {
            loanService.renewBook(detailId);
            redirectAttributes.addFlashAttribute("successMessage", "Gia hạn sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/loans/" + loanId;
    }
}
