package ru.vlsklv.course.app.ui.kit;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import ru.vlsklv.course.app.editor.CompletionEngine;

import java.util.function.Consumer;
import java.util.function.Function;

public final class CodeUi {
    private CodeUi() {
    }

    public record EditorBundle(
            CodeArea editor,
            VirtualizedScrollPane<CodeArea> scroll,
            Popup popup,
            ListView<CompletionEngine.CompletionItem> list,
            Runnable hidePopup
    ) {
    }

    public static EditorBundle createJavaEditor(String initialText) {
        return createEditor(initialText, new CompletionEngine());
    }

    public static EditorBundle createEditor(String initialText, CompletionEngine completionEngine) {
        CodeArea editor = new CodeArea();
        editor.getStyleClass().add("code-area");
        editor.getStyleClass().add("app-scrollable");
        editor.setPrefWidth(Region.USE_COMPUTED_SIZE);
        editor.setMaxWidth(Double.MAX_VALUE);
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.replaceText(initialText == null ? "" : initialText);

        VirtualizedScrollPane<CodeArea> editorScroll = new VirtualizedScrollPane<>(editor);
        editorScroll.setMaxWidth(Double.MAX_VALUE);

        Popup completionPopup = new Popup();
        completionPopup.setAutoHide(true);

        ListView<CompletionEngine.CompletionItem> completionList = new ListView<>();
        completionList.getStyleClass().add("completion-list");
        completionList.setPrefWidth(460);
        completionList.setPrefHeight(240);

        completionList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CompletionEngine.CompletionItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("completion-snippet");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.label);
                    if (item.isSnippet) getStyleClass().add("completion-snippet");
                }
            }
        });

        completionPopup.getContent().add(completionList);

        PauseTransition debounce = new PauseTransition(Duration.millis(120));
        final CompletionEngine.Suggestion[] lastSuggestion =
                new CompletionEngine.Suggestion[]{CompletionEngine.Suggestion.empty()};

        Runnable hideCompletion = () -> {
            completionPopup.hide();
            lastSuggestion[0] = CompletionEngine.Suggestion.empty();
        };

        Consumer<Boolean> showCompletion = (force) -> {
            int caret = editor.getCaretPosition();
            String text = editor.getText();

            CompletionEngine.Suggestion s = completionEngine.suggest(text, caret, force);
            if (s.isEmpty()) {
                hideCompletion.run();
                return;
            }

            lastSuggestion[0] = s;
            completionList.getItems().setAll(s.items());
            completionList.getSelectionModel().select(0);

            var caretBoundsOpt = editor.getCaretBounds();
            if (caretBoundsOpt.isEmpty()) {
                hideCompletion.run();
                return;
            }

            Bounds screen = editor.localToScreen(caretBoundsOpt.get());
            if (screen == null) {
                hideCompletion.run();
                return;
            }

            double x = screen.getMinX();
            double y = screen.getMaxY() + 4;

            if (!completionPopup.isShowing()) completionPopup.show(editor, x, y);
            else {
                completionPopup.setX(x);
                completionPopup.setY(y);
            }
        };

        Consumer<CompletionEngine.CompletionItem> applyCompletion = (item) -> {
            CompletionEngine.Suggestion s = lastSuggestion[0];
            if (s == null || s.isEmpty() || item == null) return;

            String insert = item.insertText;
            int cursorPosInInsert = -1;

            final String marker = "{{cursor}}";
            int idx = insert.indexOf(marker);
            if (idx >= 0) {
                cursorPosInInsert = idx;
                insert = insert.replace(marker, "");
            }

            String preparedInsert = normalizeSnippetIndentation(editor.getText(), s.replaceFrom(), insert);
            editor.replaceText(s.replaceFrom(), s.replaceTo(), preparedInsert);

            int newCaret = (cursorPosInInsert >= 0)
                    ? (s.replaceFrom() + cursorPosInInsert)
                    : (s.replaceFrom() + preparedInsert.length());

            editor.moveTo(newCaret);
            editor.requestFollowCaret();
            hideCompletion.run();
        };

        completionList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                applyCompletion.accept(completionList.getSelectionModel().getSelectedItem());
            }
        });

        completionList.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                applyCompletion.accept(completionList.getSelectionModel().getSelectedItem());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                e.consume();
                hideCompletion.run();
                editor.requestFocus();
            }
        });

        Function<CompletionEngine.Suggestion, CompletionEngine.CompletionItem> pickBestForTab = (s) -> {
            if (s == null || s.isEmpty()) return null;

            for (CompletionEngine.CompletionItem it : s.items()) {
                if (it.isSnippet && it.matchKey != null && it.matchKey.equalsIgnoreCase(s.prefix())) return it;
            }

            if (s.items().size() == 1) return s.items().get(0);

            return null;
        };

        editor.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
                e.consume();
                showCompletion.accept(true);
                return;
            }

            if (completionPopup.isShowing()) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    e.consume();
                    hideCompletion.run();
                    return;
                }
                if (e.getCode() == KeyCode.UP) {
                    e.consume();
                    completionList.getSelectionModel().selectPrevious();
                    completionList.scrollTo(completionList.getSelectionModel().getSelectedIndex());
                    return;
                }
                if (e.getCode() == KeyCode.DOWN) {
                    e.consume();
                    completionList.getSelectionModel().selectNext();
                    completionList.scrollTo(completionList.getSelectionModel().getSelectedIndex());
                    return;
                }
                if (e.getCode() == KeyCode.TAB || e.getCode() == KeyCode.ENTER) {
                    e.consume();
                    applyCompletion.accept(completionList.getSelectionModel().getSelectedItem());
                    return;
                }
            }

            if (e.getCode() == KeyCode.TAB) {
                e.consume();

                CompletionEngine.Suggestion s = completionEngine.suggest(editor.getText(), editor.getCaretPosition(), true);
                if (!s.isEmpty()) {
                    lastSuggestion[0] = s;
                    CompletionEngine.CompletionItem best = pickBestForTab.apply(s);
                    if (best != null) {
                        applyCompletion.accept(best);
                    } else {
                        showCompletion.accept(true);
                    }
                    return;
                }

                editor.replaceSelection("    ");
            }
        });

        editor.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            String ch = e.getCharacter();
            if (ch == null || ch.isEmpty()) return;

            char c = ch.charAt(0);
            boolean trigger = Character.isLetterOrDigit(c) || c == '_' || c == '.';

            if (!trigger) {
                if (completionPopup.isShowing()) hideCompletion.run();
                return;
            }

            debounce.stop();
            debounce.setOnFinished(ev -> Platform.runLater(() -> showCompletion.accept(false)));
            debounce.playFromStart();
        });

        editor.setOnMousePressed(e -> {
            if (completionPopup.isShowing()) hideCompletion.run();
        });

        return new EditorBundle(editor, editorScroll, completionPopup, completionList, hideCompletion);
    }

    private static String normalizeSnippetIndentation(String text, int replaceFrom, String insert) {
        if (insert == null || !insert.contains("\n")) {
            return insert == null ? "" : insert;
        }

        String baseIndent = currentLineIndent(text, replaceFrom);
        String[] lines = insert.split("\\n", -1);

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append('\n').append(baseIndent);
            out.append(lines[i]);
        }
        return out.toString();
    }

    private static String currentLineIndent(String text, int pos) {
        if (text == null || text.isEmpty()) return "";

        int safePos = Math.max(0, Math.min(pos, text.length()));
        int lineStart = text.lastIndexOf('\n', Math.max(0, safePos - 1));
        lineStart = lineStart < 0 ? 0 : lineStart + 1;

        int i = lineStart;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == ' ' || c == '\t') i++;
            else break;
        }
        return text.substring(lineStart, i);
    }
}
