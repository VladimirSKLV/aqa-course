package ru.vlsklv.course.engine.model;

import java.util.List;

public class QuizQuestion {
    private String text;
    private List<QuizOption> options;

    public QuizQuestion() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<QuizOption> getOptions() { return options; }
    public void setOptions(List<QuizOption> options) { this.options = options; }
}
