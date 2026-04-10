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

            StringBuilder header = new StringBuilder();
            header.append("<html>\n")
                  .append("<head>\n")
                  .append("  <title>PDF WCAG Accessibility Report</title>\n")
                  .append("  <style>\n")
                  .append("    body { font-family: Arial; }\n")
                  .append("    table { border-collapse: collapse; width: 100%; }\n")
                  .append("    th, td { border: 1px solid #ccc; padding: 8px; }\n")
                  .append("    th { background-color: #f4f4f4; }\n")
                  .append("    .PASS { color: green; }\n")
                  .append("    .FAIL { color: red; }\n")
                  .append("    .WARN { color: orange; }\n")
                  .append("  </style>\n")
                  .append("</head>\n")
                  .append("<body>\n")
                  .append("<h1>WCAG 2.1 Accessibility Report</h1>\n");
            writer.write(header.toString());
            
            writer.write(String.format(
                    "<h2>File: %s</h2>",
                    pdf.getName()
            ));
            
            StringBuilder tableHeader = new StringBuilder();
            tableHeader.append("<table>\n")
                       .append("  <tr>\n")
                       .append("    <th>WCAG Criterion</th>\n")
                       .append("    <th>Level</th>\n")
                       .append("    <th>Description</th>\n")
                       .append("    <th>Status</th>\n")
                       .append("  </tr>\n");
            writer.write(tableHeader.toString());

            for (AccessibilityIssue issue : issues) {
                if (issue.getDescription().startsWith("ALT texts missing")) {
                    StringBuilder trBuilder = new StringBuilder();
                    trBuilder.append("<tr><td>")
                        .append(issue.getWcagCriterion())
                        .append("</td><td>")
                        .append(issue.getLevel())
                        .append("</td><td>")
                        .append(issue.getDescription())
                        .append(generateMissingAltSection(images))
                        .append("</td><td class='")
                        .append(issue.getStatus())
                        .append("'>")
                        .append(issue.getStatus())
                        .append("</td></tr>");
                    writer.write(trBuilder.toString());
                } else {
                    writer.write(String.format(
                            "<tr><td>%s</td><td>%s</td><td>%s</td><td class='%s'>%s</td></tr>",
                            issue.getWcagCriterion(),
                            issue.getLevel(),
                            issue.getDescription(),
                            issue.getStatus(),
                            issue.getStatus()
                    ));
                }
            }

            // close table
            writer.write("</table>\n");
            
            StringBuilder note = new StringBuilder();
            note.append("<p><b>Note:</b> This report covers automated WCAG checks only.\n")
                .append("Manual accessibility review is required.</p>\n")
                .append("</body>\n")
                .append("</html>\n");
            writer.write(note.toString());
        }
    }
    
    public static String generateMissingAltSection(List<ImageAltData> images) {

        StringBuilder html = new StringBuilder();

        // Foldable section
        html.append("<details style='margin-top:10px;'>");
        html.append("<summary style='cursor:pointer; font-weight:bold;'>")
            .append("View all PDF Images")
            .append("</summary>");

        html.append("<br/>");

        html.append("<table border='1' style='border-collapse: collapse;'>");

        html.append("<tr>")
            .append("<th>Page</th>")
            .append("<th>Image</th>")
            .append("<th>ALT Text</th>")
            .append("</tr>");

        for (ImageAltData data : images) {

            String base64 = ImageUtil.toBase64(data.image);

            html.append("<tr>");

            // Page number
            html.append("<td>").append(data.pageNumber).append("</td>");

            // Image preview
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
            html.append("<td style='color:red;'>MISSING</td>");

            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</details>");

        return html.toString();
    }


    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

}
