package com.hiccup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfWcagValidator {

    public static List<AccessibilityIssue> validate(File pdfFile) throws Exception {
        List<AccessibilityIssue> issues = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            // WCAG 1.3.1 – Tagged PDF
            PDStructureTreeRoot structureRoot = document.getDocumentCatalog().getStructureTreeRoot();
            if (structureRoot == null) {
                issues.add(new AccessibilityIssue(
                        "1.3.1",
                        "A",
                        "PDF is not tagged (missing structure tree)",
                        "FAIL"
                ));
            } else {
                issues.add(new AccessibilityIssue("1.3.1", "A",
                        "PDF contains a structure tree", "PASS"));
            }

            // WCAG 2.4.2 – Document title
            String title = document.getDocumentInformation().getTitle();
            if (title == null || title.trim().isEmpty()) {
                issues.add(new AccessibilityIssue(
                        "2.4.2",
                        "A",
                        "Document title is missing",
                        "FAIL"
                ));
            } else {
                issues.add(new AccessibilityIssue("2.4.2", "A",
                        "Document title is present", "PASS"));
            }

            // WCAG 3.1.1 – Language of document
            String lang = document.getDocumentCatalog().getLanguage();
            if (lang == null || lang.isEmpty()) {
                issues.add(new AccessibilityIssue(
                        "3.1.1",
                        "A",
                        "Document language is not specified",
                        "FAIL"
                ));
            } else {
                issues.add(new AccessibilityIssue("3.1.1", "A",
                        "Document language specified: " + lang, "PASS"));
            }

            // WCAG 1.1.1 – Image alt text
            boolean imageFound = false;
            boolean missingAlt = false;

            for (PDPage page : document.getPages()) {
                for (COSName name : page.getResources().getXObjectNames()) {
                    PDXObject xObject = page.getResources().getXObject(name);
                    if (xObject instanceof PDImageXObject) {
                        imageFound = true;
                        // PDFBox cannot truly validate Alt text; heuristic check
                        if (structureRoot == null) {
                            missingAlt = true;
                        }
                    }
                }
            }

            if (imageFound && missingAlt) {
                issues.add(new AccessibilityIssue(
                        "1.1.1",
                        "A",
                        "Images detected but alt text cannot be verified",
                        "WARN"
                ));
            } else if (imageFound) {
                issues.add(new AccessibilityIssue("1.1.1", "A",
                        "Images detected; manual alt text review required", "WARN"));
            }

            // Detect scanned PDF (image-only)
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text.trim().isEmpty()) {
                issues.add(new AccessibilityIssue(
                        "1.4.5",
                        "AA",
                        "PDF appears to be image-only (likely scanned)",
                        "FAIL"
                ));
            } else {
                issues.add(new AccessibilityIssue("1.4.5", "AA",
                        "Text content detected", "PASS"));
            }
        }

        return issues;
    }
}
