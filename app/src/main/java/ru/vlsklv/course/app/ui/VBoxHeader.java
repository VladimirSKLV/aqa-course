package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class VBoxHeader {
    private final Label title;
    private final Label subtitle;

    public VBoxHeader(Label title, Label subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public Parent view() {
        VBox v = new VBox(6, title, subtitle);
        v.setPadding(new Insets(0, 0, 12, 0));
        return v;
    }
}
