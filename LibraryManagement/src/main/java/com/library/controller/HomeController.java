package com.library.controller;

import com.library.service.BookService;
import com.library.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;
    private final BookService bookService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        model.addAllAttributes(stats);

        var allBooks = bookService.getAllBooks();
        model.addAttribute("featuredBooks", allBooks.stream().limit(4).toList());
        model.addAttribute("pageTitle", "Trang chủ Thư viện");
        return "client/home";
    }
}
