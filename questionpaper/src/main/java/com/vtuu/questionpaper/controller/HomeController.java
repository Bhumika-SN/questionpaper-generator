package com.vtuu.questionpaper.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/") // ✅ homepage only
    public String home(Model model) {
        model.addAttribute("message", "Hello, Question Paper App is running!");
        return "index"; // will look for templates/index.html
    }
}