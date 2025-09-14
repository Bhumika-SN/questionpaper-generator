package com.vtuu.questionpaper.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuestionPaperService {

    // This method randomly picks 2 questions
    public List<String> generateRandomQuestions(List<String> allQuestions) {
        if (allQuestions == null || allQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        // Shuffle the list so order is random
        Collections.shuffle(allQuestions);

        // Pick only 2 or fewer if not enough
        int limit = Math.min(2, allQuestions.size());

        return new ArrayList<>(allQuestions.subList(0, limit));
    }
}