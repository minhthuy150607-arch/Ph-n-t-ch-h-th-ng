package com.library.controller;

import com.library.dto.BookCopyScanDto;
import com.library.model.LoanDetail;
import com.library.model.User;
import com.library.service.LoanService;
import com.library.service.QrCodeService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QrApiController {

    private final QrCodeService qrCodeService;
    private final LoanService loanService;
    private final UserService userService;

    /**
     * Xuất ảnh QR Code (PNG) trực tiếp từ mã bản sách.
     * Có thể hiển thị trực tiếp trong thẻ <img> hoặc tải về máy (download=true).
     */
    @GetMapping("/qr/image")
    public ResponseEntity<byte[]> getQrCodeImage(@RequestParam String code,
                                                 @RequestParam(defaultValue = "300") int size,
                                                 @RequestParam(defaultValue = "false") boolean download) {
        try {
            byte[] imageBytes = qrCodeService.generateQrCodeImage(code, size, size);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            if (download) {
                headers.setContentDispositionFormData("attachment", "qrcode-" + code.replaceAll("[^a-zA-Z0-9_-]", "_") + ".png");
            } else {
                headers.setCacheControl("max-age=86400, public");
            }
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API tra cứu chi tiết bản sách qua mã Barcode / QR Code.
     * Sử dụng khi quét mã tại giao diện mượn và trả sách.
     */
    @GetMapping("/book-copies/{barcode}")
    public ResponseEntity<?> getCopyDetails(@PathVariable String barcode, org.springframework.security.core.Authentication authentication) {
        try {
            BookCopyScanDto scanDto = loanService.getBookCopyScanInfo(barcode);
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                scanDto.setReaderName(null);
                scanDto.setReaderCode(null);
                scanDto.setReaderPhone(null);
                scanDto.setActiveLoanCode(null);
            }
            return ResponseEntity.ok(scanDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi tra cứu bản sách: " + e.getMessage()));
        }
    }

    /**
     * API kiểm tra tính hợp lệ của bản sách khi chuẩn bị mượn.
     */
    @GetMapping("/book-copies/{barcode}/validate-borrow")
    public ResponseEntity<?> validateForBorrow(@PathVariable String barcode) {
        try {
            BookCopyScanDto scanDto = loanService.getBookCopyScanInfo(barcode);
            if (!"AVAILABLE".equalsIgnoreCase(scanDto.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "valid", false,
                        "message", "Bản sách (" + scanDto.getBarcode() + " - " + scanDto.getTitle() + ") hiện đang ở trạng thái: " + scanDto.getStatus() + ", không thể mượn!"
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "valid", true,
                    "data", scanDto
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API thực hiện trả sách nhanh bằng cách quét Barcode / QR Code.
     */
    @PostMapping("/loans/return-by-barcode")
    public ResponseEntity<?> returnBookByBarcode(@RequestParam String barcode,
                                                 @RequestParam(defaultValue = "GOOD") String conditionOnReturn,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long librarianId = null;
            if (userDetails != null) {
                User librarian = userService.getUserByUsername(userDetails.getUsername()).orElse(null);
                if (librarian != null) librarianId = librarian.getId();
            }

            LoanDetail detail = loanService.returnBookByBarcode(barcode, conditionOnReturn, librarianId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã trả thành công bản sách: " + detail.getBookCopy().getBarcode() + " (" + detail.getBookCopy().getBook().getTitle() + ")",
                    "barcode", detail.getBookCopy().getBarcode(),
                    "title", detail.getBookCopy().getBook().getTitle(),
                    "loanCode", detail.getLoan().getLoanCode(),
                    "readerName", detail.getLoan().getUser().getFullName(),
                    "isLoanFullyReturned", "RETURNED".equalsIgnoreCase(detail.getLoan().getStatus())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
