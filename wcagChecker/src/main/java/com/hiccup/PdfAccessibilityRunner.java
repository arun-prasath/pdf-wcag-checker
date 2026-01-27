package com.hiccup;

import java.io.File;
import java.util.List;

public class PdfAccessibilityRunner {

    public static void main(String[] args) throws Exception {
        File pdf = new File("input.pdf");
        File report = new File("wcag-report.html");

        List<AccessibilityIssue> issues = PdfWcagValidator.validate(pdf);
        HtmlReportGenerator.generate(issues, report);

        System.out.println("WCAG accessibility report generated: " + report.getAbsolutePath());
    }
}
