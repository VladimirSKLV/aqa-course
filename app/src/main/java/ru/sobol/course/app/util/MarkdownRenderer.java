package ru.sobol.course.app.util;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.List;

public class MarkdownRenderer {
    private static final Parser parser;
    private static final HtmlRenderer renderer;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public static String toHtml(String md) {
        String body = renderer.render(parser.parse(md == null ? "" : md));

        String head = ""
                + "<html><head><meta charset=\"utf-8\"/>"
                + "<style>"
                + "body{font-family:-apple-system,Segoe UI,Arial,sans-serif;line-height:1.55;padding:16px;}"
                + "h1,h2,h3{margin-top:18px;}"
                + "pre{background:#0b1020;color:#e8e8e8;padding:12px;border-radius:10px;overflow-x:auto;}"
                + "code{font-family:Consolas,Menlo,monospace;}"
                + "table{border-collapse:collapse;margin:12px 0;}"
                + "th,td{border:1px solid #d0d0d0;padding:8px;}"
                + "blockquote{border-left:4px solid #c0c0c0;padding-left:12px;color:#444;}"
                + "</style></head><body>";

        String tail = "</body></html>";

        return head + body + tail;
    }
}
