package com.hiccup;

import java.io.File;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.hiccup.model.AccessibilityIssue;
import com.hiccup.model.ImageAltData;
import com.hiccup.report.HtmlReportGenerator;
import com.hiccup.validator.PdfWcagValidator;

public class PdfAccessibilityRunner {

    public static void main(String[] args) throws Exception {
        File pdf = new File(args[0]);
        File report = new File(args[1]);

        try (PDDocument document = PDDocument.load(pdf)) {

            List<AccessibilityIssue> issues = PdfWcagValidator.validate(document);
            List<ImageAltData> images = PdfWcagValidator.extractImagesWithAlt(document);

            HtmlReportGenerator.generate(issues, pdf, report, images);
        }

        System.out.println("WCAG accessibility report generated: " + report.getAbsolutePath());
        System.out.println("PDF File: " + args[0]);
    }

}
