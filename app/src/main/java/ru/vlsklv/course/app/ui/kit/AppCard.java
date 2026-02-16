package ru.vlsklv.course.app.ui.kit;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public final class AppCard extends VBox {
    public AppCard(Node... children) {
        super(14, children);
        getStyleClass().add("card");
        setPadding(new Insets(22));
        setMaxWidth(720);
    }
}
