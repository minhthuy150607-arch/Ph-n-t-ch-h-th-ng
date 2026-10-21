package com.library.controller.admin;

import com.library.model.Book;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String listBooks(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "category", required = false) String category,
                            Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("books", bookService.searchBooks(keyword));
        } else if (category != null && !category.trim().isEmpty()) {
            model.addAttribute("books", bookService.getBooksByCategory(category));
        } else {
            model.addAttribute("books", bookService.getAllBooks());
        }

        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("activeMenu", "books");
        model.addAttribute("pageTitle", "Quản lý Sách");
        return "admin/books/list";
    }

    @GetMapping("/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("activeMenu", "books");
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "admin/books/form";
    }

    @PostMapping("/new")
    public String saveBook(@ModelAttribute Book book,
                           @RequestParam(value = "coverFile", required = false) org.springframework.web.multipart.MultipartFile coverFile,
                           RedirectAttributes redirectAttributes) {
        try {
            bookService.saveBookWithCover(book, coverFile);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sách mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("categories", bookService.getAllCategories());
                    model.addAttribute("activeMenu", "books");
                    model.addAttribute("pageTitle", "Chỉnh sửa sách: " + book.getTitle());
                    return "admin/books/form";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
                    return "redirect:/admin/books";
                });
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute Book book,
                             @RequestParam(value = "coverFile", required = false) org.springframework.web.multipart.MultipartFile coverFile,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBook(id, book, coverFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin sách thành công!");
            return "redirect:/admin/books/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/books/edit/" + id;
        }
    }

    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("activeMenu", "books");
                    model.addAttribute("pageTitle", "Chi tiết sách: " + book.getTitle());
                    return "admin/books/detail";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
                    return "redirect:/admin/books";
                });
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sách đang có bản sao hoặc phiếu mượn!");
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/copies/add")
    public String addCopy(@PathVariable Long id,
                          @RequestParam(required = false) String barcode,
                          @RequestParam(defaultValue = "GOOD") String condition,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes redirectAttributes) {
        try {
            bookService.addCopy(id, barcode, condition, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bản sao thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thêm bản sao: " + e.getMessage());
        }
        return "redirect:/admin/books/" + id;
    }

    @PostMapping("/copies/delete/{copyId}")
    public String deleteCopy(@PathVariable Long copyId,
                             @RequestParam Long bookId,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteCopy(copyId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bản sao thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bản sao đang được mượn!");
        }
        return "redirect:/admin/books/" + bookId;
    }

    @PostMapping("/copies/update/{copyId}")
    public String updateCopy(@PathVariable Long copyId,
                             @RequestParam Long bookId,
                             @RequestParam String status,
                             @RequestParam String condition,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes redirectAttributes) {
        try {
            bookService.updateCopyStatus(copyId, status, condition, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái bản sao thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/books/" + bookId;
    }

    @GetMapping("/{id}/qr-print")
    public String printQrLabels(@PathVariable Long id,
                                @RequestParam(required = false) Long copyId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    if (copyId != null) {
                        model.addAttribute("copies", book.getCopies().stream()
                                .filter(c -> c.getId().equals(copyId))
                                .toList());
                    } else {
                        model.addAttribute("copies", book.getCopies());
                    }
                    return "admin/books/qr-print";
                }).orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
                    return "redirect:/admin/books";
                });
    }
}
