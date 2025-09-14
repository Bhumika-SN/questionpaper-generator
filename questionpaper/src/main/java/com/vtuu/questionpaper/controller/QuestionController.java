package com.vtuu.questionpaper.controller;

import com.vtuu.questionpaper.model.Question;
import com.vtuu.questionpaper.repository.QuestionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    // Show all questions
    @GetMapping
    public String listQuestions(Model model) {
        model.addAttribute("questions", questionRepository.findAll());
        return "questions";
    }

    // Show form to add question
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("question", new Question());
        return "add-question";
    }

    // Save question
    @PostMapping("/add")
    public String addQuestion(@ModelAttribute Question question) {
        questionRepository.save(question);
        return "redirect:/questions";
    }
}