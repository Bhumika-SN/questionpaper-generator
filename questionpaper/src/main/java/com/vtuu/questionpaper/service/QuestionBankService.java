package com.vtuu.questionpaper.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionBankService {

    private final Map<String, List<String>> moduleQuestions = new HashMap<>();

    private String sem;
    private String monthYear;
    private String subject;
    private String subjectCode;

    // save questions
    public void saveQuestionsByModule(Map<String, List<String>> questionsByModule) {
        moduleQuestions.clear();
        moduleQuestions.putAll(questionsByModule);
    }

    // generate random 2 questions per part
    public Map<String, Map<String, List<String>>> getCombinedPaper(int perPart) {
        Map<String, Map<String, List<String>>> paper = new LinkedHashMap<>();

        for (String module : moduleQuestions.keySet()) {
            List<String> questions = new ArrayList<>(moduleQuestions.get(module));
            Collections.shuffle(questions);

            Map<String, List<String>> parts = new LinkedHashMap<>();
            parts.put("Part A", questions.subList(0, Math.min(perPart, questions.size())));
            if (questions.size() > perPart) {
                parts.put("Part B", questions.subList(perPart, Math.min(perPart * 2, questions.size())));
            }
            paper.put(module, parts);
        }
        return paper;
    }

    // exam details setters
    public void setExamDetails(String sem, String monthYear, String subject, String subjectCode) {
        this.sem = sem;
        this.monthYear = monthYear;
        this.subject = subject;
        this.subjectCode = subjectCode;
    }

    // exam details getters
    public String getSem() { return sem; }
    public String getMonthYear() { return monthYear; }
    public String getSubject() { return subject; }
    public String getSubjectCode() { return subjectCode; }
}