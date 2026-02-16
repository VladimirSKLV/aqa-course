package ru.vlsklv.course.app.ui.kit;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.util.Duration;

public final class Fx {
    private Fx() {}

    /** Мягкий вход страницы: fade + slide */
    public static void pageEnter(Node node) {
        if (node == null) return;

        node.setOpacity(0);
        node.setTranslateY(10);

        FadeTransition fade = new FadeTransition(Duration.millis(170), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition move = new TranslateTransition(Duration.millis(170), node);
        move.setFromY(10);
        move.setToY(0);

        ParallelTransition pt = new ParallelTransition(fade, move);
        pt.setInterpolator(Interpolator.EASE_OUT);
        pt.play();
    }

    /** Универсально улучшает контролы внутри дерева */
    public static void enhance(Node root) {
        if (root == null) return;

        if (root instanceof Button b) {
            installButtonMotion(b);
        }

        // поднимаем панели/карточки при наведении
        if (root.getStyleClass().contains("card") || root.getStyleClass().contains("panel")) {
            installLift(root);
        }

        if (root instanceof Parent p) {
            for (Node ch : p.getChildrenUnmodifiable()) {
                enhance(ch);
            }
        }
    }

    /** Hover/Press анимации для кнопок (scale). */
    public static void installButtonMotion(Button b) {
        if (b == null) return;
        if (b.getProperties().containsKey("fx.motion.installed")) return;
        b.getProperties().put("fx.motion.installed", true);

        ScaleTransition hoverIn = new ScaleTransition(Duration.millis(110), b);
        hoverIn.setToX(1.02);
        hoverIn.setToY(1.02);
        hoverIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(110), b);
        hoverOut.setToX(1.0);
        hoverOut.setToY(1.0);
        hoverOut.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition press = new ScaleTransition(Duration.millis(80), b);
        press.setToX(0.99);
        press.setToY(0.99);
        press.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition release = new ScaleTransition(Duration.millis(80), b);
        release.setToX(1.02);
        release.setToY(1.02);
        release.setInterpolator(Interpolator.EASE_OUT);

        b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> {
            if (b.isDisabled()) return;
            hoverOut.stop();
            hoverIn.playFromStart();
        });

        b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> {
            hoverIn.stop();
            hoverOut.playFromStart();
        });

        b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            if (b.isDisabled()) return;
            press.playFromStart();
        });

        b.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            if (b.isDisabled()) return;
            if (b.isHover()) release.playFromStart();
            else hoverOut.playFromStart();
        });

        if (!b.getStyleClass().contains("btn")) b.getStyleClass().add("btn");
    }

    /** Подъём карточек/панелей при наведении (translateY). */
    private static void installLift(Node node) {
        if (node.getProperties().containsKey("fx.lift.installed")) return;
        node.getProperties().put("fx.lift.installed", true);

        TranslateTransition in = new TranslateTransition(Duration.millis(140), node);
        in.setToY(-2);
        in.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition out = new TranslateTransition(Duration.millis(140), node);
        out.setToY(0);
        out.setInterpolator(Interpolator.EASE_OUT);

        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> {
            out.stop();
            in.playFromStart();
        });
        node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> {
            in.stop();
            out.playFromStart();
        });
    }
}
