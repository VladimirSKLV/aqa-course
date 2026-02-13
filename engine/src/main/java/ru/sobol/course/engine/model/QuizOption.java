package ru.sobol.course.engine.model;

public class QuizOption {
    private String text;
    private boolean correct;

    public QuizOption() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
