package ru.vlsklv.course.engine.model;

import java.util.List;

public class QuizAssignment implements Assignment {
    private int passPercent = 80;
    private List<QuizQuestion> questions;

    public QuizAssignment() {}

    public int getPassPercent() { return passPercent; }
    public void setPassPercent(int passPercent) { this.passPercent = passPercent; }

    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
}
