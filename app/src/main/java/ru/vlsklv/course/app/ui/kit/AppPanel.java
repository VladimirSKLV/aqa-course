package ru.vlsklv.course.app.ui.kit;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class AppPanel extends VBox {
    public AppPanel(Node... children) {
        super(10, children);
        getStyleClass().addAll("card", "panel");
        setPadding(new Insets(14));
        setFillWidth(true);
    }

    public static void grow(Node node) {
        VBox.setVgrow(node, Priority.ALWAYS);
    }
}
