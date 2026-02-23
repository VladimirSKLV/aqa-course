package ru.vlsklv.course.app.util;

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
                + "body{font-family:Inter,Segoe UI,Arial,sans-serif;line-height:1.65;padding:18px 24px;color:#0b1220;word-break:break-word;overflow-wrap:anywhere;}"
                + "h1,h2,h3{margin-top:20px;margin-bottom:12px;line-height:1.3;}"
                + "p,li{margin:0 0 10px 0;}"
                + "ul,ol{padding-left:22px;}"
                + "pre{background:#0b1020;color:#e8e8e8;padding:14px;border-radius:10px;overflow-x:auto;}"
                + "code{font-family:JetBrains Mono,Consolas,Menlo,monospace;font-size:0.95em;}"
                + "table{border-collapse:collapse;margin:14px 0;width:100%;}"
                + "th,td{border:1px solid #d0d7e2;padding:8px;vertical-align:top;}"
                + "blockquote{border-left:4px solid #a5b4fc;padding-left:12px;color:#334155;background:#eef2ff;border-radius:0 8px 8px 0;}"
                + "</style></head><body>";

        String tail = "</body></html>";

        return head + body + tail;
    }
}
