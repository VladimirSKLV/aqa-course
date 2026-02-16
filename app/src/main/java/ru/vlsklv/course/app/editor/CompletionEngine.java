package ru.vlsklv.course.app.editor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CompletionEngine {

    private static final List<String> JAVA_KEYWORDS = List.of(
            "abstract","assert","boolean","break","byte","case","catch","char","class",
            "continue","default","do","double","else","enum","extends","final","finally",
            "float","for","if","implements","import","instanceof","int","interface",
            "long","new","package","private","protected","public","return","short","static",
            "super","switch","synchronized","this","throw","throws","try","void","volatile","while",
            "var","record","sealed","permits","non-sealed"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");

    private static final List<CompletionItem> SNIPPETS = List.of(
            CompletionItem.snippet("if (...) { ... }", "if",
                    """
                            if ({{cursor}}) {
                               \s
                            }
                            """),
            CompletionItem.snippet("if (...) { ... } else { ... }", "if",
                    """
                            if ({{cursor}}) {
                               \s
                            } else {
                               \s
                            }
                            """),
            CompletionItem.snippet("System.out.println(...)", "System",
                    "System.out.println({{cursor}});\n"),
            CompletionItem.snippet("assertTrue(...)", "assert",
                    "assertTrue({{cursor}});"),
            CompletionItem.snippet("assertFalse(...)", "assert",
                    "assertFalse({{cursor}});"),
            CompletionItem.snippet("assertEquals(expected, actual)", "assert",
                    "assertEquals({{cursor}});"),
            CompletionItem.snippet("assertNotNull(...)", "assert",
                    "assertNotNull({{cursor}});")
    );

    public Suggestion suggest(String fullText, int caretPos, boolean force) {
        Prefix p = extractPrefix(fullText, caretPos);
        if (p.prefix.isBlank()) return Suggestion.empty();

        if (!force && p.prefix.length() < 2) return Suggestion.empty();

        if (!force && p.prefix.endsWith(".")) return Suggestion.empty();

        String prefixLower = p.prefix.toLowerCase(Locale.ROOT);

        List<CompletionItem> out = new ArrayList<>();

        for (CompletionItem s : SNIPPETS) {
            if (startsWithIgnoreCase(s.matchKey, prefixLower)) out.add(s);
        }

        for (String kw : JAVA_KEYWORDS) {
            if (startsWithIgnoreCase(kw, prefixLower)) out.add(CompletionItem.word(kw));
        }

        if (fullText != null && !fullText.isBlank()) {
            Set<String> tokens = extractTokens(fullText);
            for (String t : tokens) {
                if (t.equals(p.prefix)) continue;
                if (startsWithIgnoreCase(t, prefixLower)) out.add(CompletionItem.word(t));
            }
        }

        out = dedupe(out);
        out.sort(Comparator
                .comparingInt((CompletionItem i) -> score(i, prefixLower))
                .thenComparing(i -> i.label.toLowerCase(Locale.ROOT)));

        if (out.isEmpty()) return Suggestion.empty();
        return new Suggestion(p.start, caretPos, p.prefix, out);
    }

    private static int score(CompletionItem i, String prefixLower) {
        String mk = i.matchKey == null ? "" : i.matchKey.toLowerCase(Locale.ROOT);
        String lbl = i.label == null ? "" : i.label.toLowerCase(Locale.ROOT);

        // Сниппеты для ключевого слова должны быть выше обычного keyword "if"
        if (i.isSnippet && mk.equals(prefixLower)) return 0;
        if (i.isSnippet) return 1;

        // Точное совпадение слова (keyword/token)
        if (lbl.equals(prefixLower)) return 2;

        // Остальные
        return 3;
    }

    private static boolean startsWithIgnoreCase(String value, String prefixLower) {
        if (value == null) return false;
        return value.toLowerCase(Locale.ROOT).startsWith(prefixLower);
    }

    private static Prefix extractPrefix(String text, int caretPos) {
        if (text == null) return new Prefix(caretPos, "");
        int pos = Math.min(Math.max(caretPos, 0), text.length());
        int start = pos;

        while (start > 0) {
            char c = text.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') start--;
            else break;
        }

        return new Prefix(start, text.substring(start, pos));
    }

    private static Set<String> extractTokens(String text) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        Matcher m = TOKEN_PATTERN.matcher(text);
        while (m.find()) {
            String t = m.group();
            if (t.length() < 2) continue;
            tokens.add(t);
            if (tokens.size() >= 500) break;
        }
        return tokens;
    }

    private static List<CompletionItem> dedupe(List<CompletionItem> items) {
        LinkedHashMap<String, CompletionItem> map = new LinkedHashMap<>();
        for (CompletionItem i : items) {
            String key = i.label + "\u0000" + i.insertText + "\u0000" + i.isSnippet;
            map.putIfAbsent(key, i);
        }
        return new ArrayList<>(map.values());
    }

    private record Prefix(int start, String prefix) {}

    public record Suggestion(int replaceFrom, int replaceTo, String prefix, List<CompletionItem> items) {
        public static Suggestion empty() { return new Suggestion(0, 0, "", List.of()); }
        public boolean isEmpty() { return items == null || items.isEmpty(); }
    }

    public static final class CompletionItem {
        public final String label;
        public final String insertText;
        public final String matchKey;
        public final boolean isSnippet;

        private CompletionItem(String label, String insertText, String matchKey, boolean isSnippet) {
            this.label = label;
            this.insertText = insertText;
            this.matchKey = matchKey;
            this.isSnippet = isSnippet;
        }

        public static CompletionItem word(String word) {
            return new CompletionItem(word, word, word, false);
        }

        public static CompletionItem snippet(String label, String matchKey, String insertText) {
            return new CompletionItem(label, insertText, matchKey, true);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
