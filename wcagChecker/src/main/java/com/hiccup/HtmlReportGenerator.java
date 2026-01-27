package com.hiccup;

import java.io.*;
import java.util.List;

public class HtmlReportGenerator {

    public static void generate(List<AccessibilityIssue> issues, File output)
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
                        issue.wcagCriterion,
                        issue.level,
                        issue.description,
                        issue.status,
                        issue.status
                ));
            }

            writer.write("""
                </table>
                <p><b>Note:</b> This report covers automated WCAG checks only.
                Manual accessibility review is required.</p>
                </body>
                </html>
                """);
        }
    }
}
