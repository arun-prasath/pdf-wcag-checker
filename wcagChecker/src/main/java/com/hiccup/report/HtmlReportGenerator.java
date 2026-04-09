package com.hiccup.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.hiccup.model.AccessibilityIssue;
import com.hiccup.model.ImageAltData;
import com.hiccup.util.ImageUtil;

public class HtmlReportGenerator {

    public static void generate(List<AccessibilityIssue> issues, File pdf, File output, List<ImageAltData> images)
            throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {

            writer.write("""
                <html>
                <head>
                  <title>PDF WCAG Accessibility Report</title>
                  <style>
                    body { font-family: Arial; }
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid #ccc; padding: 8px; }
                    th { background-color: #f4f4f4; }
                    .PASS { color: green; }
                    .FAIL { color: red; }
                    .WARN { color: orange; }
                  </style>
                </head>
                <body>
                <h1>WCAG 2.1 Accessibility Report</h1>
                """);
            
            writer.write(String.format(
                    "<h2>File: %s</h2>",
                    pdf.getName()
            ));
            
            writer.write("""
                <table>
                  <tr>
                    <th>WCAG Criterion</th>
                    <th>Level</th>
                    <th>Description</th>
                    <th>Status</th>
                  </tr>
                """);

            for (AccessibilityIssue issue : issues) {
                writer.write(String.format(
                        "<tr><td>%s</td><td>%s</td><td>%s</td><td class='%s'>%s</td></tr>",
                        issue.getWcagCriterion(),
                        issue.getLevel(),
                        issue.getDescription(),
                        issue.getStatus(),
                        issue.getStatus()
                ));
            }

            writer.write("""
                    </table>
                    """);
            
            writer.write(generateImageSection(images));

            writer.write("""
                </table>
                <p><b>Note:</b> This report covers automated WCAG checks only.
                Manual accessibility review is required.</p>
                </body>
                </html>
                """);
        }
    }
    
    public static String generateImageSection(List<ImageAltData> images) {

        StringBuilder html = new StringBuilder();

        html.append("<h2>Image Accessibility Report</h2>");
        html.append("<table border='1' style='border-collapse: collapse;'>");

        html.append("<tr>")
            .append("<th>Page</th>")
            .append("<th>Image</th>")
            .append("<th>ALT Text</th>")
            .append("<th>Status</th>")
            .append("</tr>");

        for (ImageAltData data : images) {

            String base64 = ImageUtil.toBase64(data.image);

            String altText = (data.altText != null) ? data.altText : "MISSING";

            String status;
            if (data.altText == null || data.altText.isBlank()) {
                status = "FAIL";
            } else {
                status = "PASS";
            }

            html.append("<tr>");

            // Page number
            html.append("<td>").append(data.pageNumber).append("</td>");

            // Image
            if (base64 != null) {
                html.append("<td>")
                    .append("<img src='data:image/png;base64,")
                    .append(base64)
                    .append("' width='150'/>")
                    .append("</td>");
            } else {
                html.append("<td>Unable to render</td>");
            }

            // ALT text
            html.append("<td>").append(escapeHtml(altText)).append("</td>");

            // Status
            html.append("<td class='").append(status).append("'>").append(status).append("</td>");

            html.append("</tr>");
        }

        html.append("</table>");

        return html.toString();
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

}
