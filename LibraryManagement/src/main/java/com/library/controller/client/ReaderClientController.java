package com.library.controller.client;

import com.library.model.Loan;
import com.library.model.User;
import com.library.service.LoanService;
import com.library.service.ReservationService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reader")
@RequiredArgsConstructor
public class ReaderClientController {

    private final LoanService loanService;
    private final ReservationService reservationService;
    private final UserService userService;

    @GetMapping("/loans")
    public String myLoans(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("loanDetails", loanService.getLoanDetailsByUserId(user.getId()));
        model.addAttribute("pageTitle", "Sách đang mượn của tôi");
        return "client/reader/loans";
    }

    @PostMapping("/borrow/{bookId}")
    public String borrowOnline(@PathVariable Long bookId,
                              @RequestParam(required = false) String notes,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        try {
            Loan loan = loanService.borrowBookOnline(user.getId(), bookId, notes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "⚡ Mượn online thành công! Mã phiếu: " + loan.getLoanCode() + " | Hạn trả: " + loan.getDueDate() + ". Vui lòng tới quầy thư viện nhận sách khi thuận tiện!");
            return "redirect:/reader/loans";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/client/books/" + bookId;
        }
    }

    @PostMapping("/loans/renew/{detailId}")
    public String renewLoanOnline(@PathVariable Long detailId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        try {
            loanService.renewBookByReader(detailId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Gia hạn mượn sách online thành công thêm 7 ngày!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reader/loans";
    }

    @GetMapping("/reservations")
    public String myReservations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("reservations", reservationService.getReservationsByUserId(user.getId()));
        model.addAttribute("pageTitle", "Sách đã đặt trước của tôi");
        return "client/reader/reservations";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserveBook(@PathVariable Long bookId,
                              @RequestParam(required = false) String notes,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        try {
            reservationService.reserveBook(user.getId(), bookId, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt trước sách thành công! Thư viện sẽ giữ sách cho bạn trong 3 ngày.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/reader/reservations";
    }

    @PostMapping("/reservations/cancel/{id}")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy yêu cầu đặt trước!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/reader/reservations";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Thông tin tài khoản");
        return "client/reader/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        try {
            userService.updateProfile(user.getId(), fullName, email, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/reader/profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("pageTitle", "Đổi mật khẩu");
        return "client/reader/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        try {
            userService.changePassword(user.getId(), oldPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            return "redirect:/reader/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reader/change-password";
        }
    }
}
