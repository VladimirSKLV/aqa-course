package ru.vlsklv.course.engine.model;

public class QuizOption {
    private String text;
    private boolean correct;
    /**
     * Краткое объяснение.
     * - для correct=true: почему это правильный ответ
     * - для correct=false: почему вариант неверен/в чём подвох
     */
    private String explanation;

    public QuizOption() {}

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
