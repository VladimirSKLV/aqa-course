package ru.sobol.course.engine.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Lesson {
    private String id;
    private CourseLanguage language;
    private int order;
    private String title;

    private Theory theory;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = QuizAssignment.class, name = "quiz")
            // В будущем: code
    })
    private Assignment assignment;

    public Lesson() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CourseLanguage getLanguage() { return language; }
    public void setLanguage(CourseLanguage language) { this.language = language; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Theory getTheory() { return theory; }
    public void setTheory(Theory theory) { this.theory = theory; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public static class Theory {
        private String markdown; // classpath resource path

        public Theory() {}

        public String getMarkdown() { return markdown; }
        public void setMarkdown(String markdown) { this.markdown = markdown; }
    }
}
