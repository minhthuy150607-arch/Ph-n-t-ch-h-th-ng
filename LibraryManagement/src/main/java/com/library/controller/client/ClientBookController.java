package com.library.controller.client;

import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/client/books")
@RequiredArgsConstructor
public class ClientBookController {

    private final BookService bookService;

    @GetMapping
    public String searchBooks(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "category", required = false) String category,
                              Model model) {
        java.util.List<com.library.model.Book> books;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        if (hasKeyword && hasCategory) {
            String catTrim = category.trim();
            books = bookService.searchBooks(keyword.trim()).stream()
                    .filter(b -> b.getCategory() != null && b.getCategory().equalsIgnoreCase(catTrim))
                    .toList();
        } else if (hasKeyword) {
            books = bookService.searchBooks(keyword.trim());
        } else if (hasCategory) {
            books = bookService.getBooksByCategory(category.trim());
        } else {
            books = bookService.getAllBooks();
        }
        model.addAttribute("books", books);

        model.addAttribute("categories", bookService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("pageTitle", "Tra cứu Sách Thư viện");
        return "client/books/list";
    }

    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        return bookService.getBookById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("pageTitle", book.getTitle());
                    return "client/books/detail";
                }).orElse("redirect:/client/books");
    }
}
