package com.vtuu.questionpaper.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String module;
    private String part;
    private String text;
    private String answer;

    // ✅ No-args constructor (needed for JPA and controllers)
    public Question() {}

    // ✅ Constructor with all fields (use if needed)
    public Question(String module, String part, String text) {
        this.module = module;
        this.part = part;
        this.text = text;
    }

    // ✅ Optionally include answer if you need
    public Question(String module, String part, String text, String answer) {
        this.module = module;
        this.part = part;
        this.text = text;
        this.answer = answer;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getPart() { return part; }
    public void setPart(String part) { this.part = part; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}