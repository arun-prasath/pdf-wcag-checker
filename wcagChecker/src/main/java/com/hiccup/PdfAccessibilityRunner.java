package com.hiccup;

import java.io.File;
import java.util.List;

import com.hiccup.model.AccessibilityIssue;
import com.hiccup.report.HtmlReportGenerator;
import com.hiccup.validator.PdfWcagValidator;

public class PdfAccessibilityRunner {

    public static void main(String[] args) throws Exception {
        File pdf = new File(args[0]);
        File report = new File(args[1]);

        List<AccessibilityIssue> issues = PdfWcagValidator.validate(pdf);
        HtmlReportGenerator.generate(issues, pdf, report);

        System.out.println("WCAG accessibility report generated: " + report.getAbsolutePath());
    }
}
