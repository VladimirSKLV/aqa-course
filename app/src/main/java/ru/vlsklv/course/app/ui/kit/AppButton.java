package ru.vlsklv.course.app.ui.kit;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public final class AppButton extends Button {
    public enum Variant { PRIMARY, SECONDARY, GHOST, DANGER }

    public AppButton(String text, Variant variant) {
        super(text);
        setFocusTraversable(false);
        getStyleClass().add("btn");
        applyVariant(variant);
        Fx.installButtonMotion(this);
    }

    public void applyVariant(Variant v) {
        getStyleClass().removeAll("btn-primary", "btn-secondary", "btn-ghost", "btn-danger",
                "primary", "secondary");
        switch (v) {
            case PRIMARY -> { getStyleClass().add("btn-primary"); getStyleClass().add("primary"); }
            case SECONDARY -> { getStyleClass().add("btn-secondary"); getStyleClass().add("secondary"); }
            case GHOST -> getStyleClass().add("btn-ghost");
            case DANGER -> getStyleClass().add("btn-danger");
        }
    }

    public static AppButton primary(String text, EventHandler<ActionEvent> onAction) {
        AppButton b = new AppButton(text, Variant.PRIMARY);
        if (onAction != null) b.setOnAction(onAction);
        return b;
    }

    public static AppButton secondary(String text, EventHandler<ActionEvent> onAction) {
        AppButton b = new AppButton(text, Variant.SECONDARY);
        if (onAction != null) b.setOnAction(onAction);
        return b;
    }

    public static AppButton ghost(String text, EventHandler<ActionEvent> onAction) {
        AppButton b = new AppButton(text, Variant.GHOST);
        if (onAction != null) b.setOnAction(onAction);
        return b;
    }

    public static AppButton danger(String text, EventHandler<ActionEvent> onAction) {
        AppButton b = new AppButton(text, Variant.DANGER);
        if (onAction != null) b.setOnAction(onAction);
        return b;
    }
}
