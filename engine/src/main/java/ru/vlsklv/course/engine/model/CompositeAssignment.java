package ru.vlsklv.course.engine.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public class CompositeAssignment implements Assignment {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = QuizAssignment.class, name = "quiz"),
            @JsonSubTypes.Type(value = CodeAssignment.class, name = "code"),
            @JsonSubTypes.Type(value = CompositeAssignment.class, name = "composite")
    })
    private List<Assignment> steps;

    public CompositeAssignment() {}

    public List<Assignment> getSteps() { return steps; }
    public void setSteps(List<Assignment> steps) { this.steps = steps; }
}
