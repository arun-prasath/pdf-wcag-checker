package com.hiccup.validator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;

import com.hiccup.model.AccessibilityIssue;

public class PdfWcagValidator {

    public static List<AccessibilityIssue> validate(File pdfFile) throws Exception {

        List<AccessibilityIssue> issues = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {

            PDDocumentCatalog catalog = document.getDocumentCatalog();
            PDStructureTreeRoot structureRoot = catalog.getStructureTreeRoot();

            /* ---------------- WCAG 1.3.1 – Info and Relationships ---------------- */

            if (structureRoot == null) {
                issues.add(new AccessibilityIssue(
                        "1.3.1", "A",
                        "PDF is not tagged (missing structure tree)",
                        "FAIL"));
            } else {
                issues.add(new AccessibilityIssue(
                        "1.3.1", "A",
                        "PDF contains a structure tree",
                        "PASS"));

                // Check for headings, lists, tables
                boolean hasHeading = false;
                boolean hasList = false;
                boolean hasTable = false;

                for (Object kid : structureRoot.getKids()) {
                    if (kid instanceof PDStructureElement element) {
                        String type = element.getStructureType();
                        if (type != null) {
                            if (type.matches("H[1-6]")) hasHeading = true;
                            if ("L".equals(type)) hasList = true;
                            if ("Table".equals(type)) hasTable = true;
                        }
                    }
                }

                issues.add(new AccessibilityIssue("2.4.6", "AA",
                        "Headings present for navigation",
                        hasHeading ? "PASS" : "FAIL"));

                issues.add(new AccessibilityIssue("1.3.1", "A",
                        "List structure present",
                        hasList ? "PASS" : "WARN"));

                issues.add(new AccessibilityIssue("1.3.1", "A",
                        "Table structure present",
                        hasTable ? "PASS" : "WARN"));
            }

            /* ---------------- WCAG 2.4.2 – Page Titled ---------------- */

            String title = document.getDocumentInformation().getTitle();
            issues.add(new AccessibilityIssue(
                    "2.4.2", "A",
                    "Document title present",
                    (title == null || title.isBlank()) ? "FAIL" : "PASS"));

            /* ---------------- WCAG 3.1.1 – Language of Page ---------------- */

            String lang = catalog.getLanguage();
            issues.add(new AccessibilityIssue(
                    "3.1.1", "A",
                    "Document language specified",
                    (lang == null || lang.isBlank()) ? "FAIL" : "PASS"));

            /* ---------------- WCAG 1.4.4 – Resize Text ---------------- */

            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            issues.add(new AccessibilityIssue(
                    "1.4.4", "AA",
                    "Text content extractable (supports resize/zoom)",
                    extractedText.trim().isEmpty() ? "FAIL" : "PASS"));

            /* ---------------- WCAG 1.4.5 – Images of Text ---------------- */

            if (extractedText.trim().isEmpty()) {
                issues.add(new AccessibilityIssue(
                        "1.4.5", "AA",
                        "PDF appears image-only (likely scanned)",
                        "FAIL"));
            }

            /* ---------------- WCAG 2.1.1 – Keyboard Accessible ---------------- */

            PDAcroForm form = catalog.getAcroForm();
            if (form != null && !form.getFields().isEmpty()) {
                issues.add(new AccessibilityIssue(
                        "2.1.1", "A",
                        "Interactive form fields detected (keyboard review required)",
                        "WARN"));
            } else {
                issues.add(new AccessibilityIssue(
                        "2.1.1", "A",
                        "No interactive elements detected",
                        "PASS"));
            }

            /* ---------------- WCAG 4.1.2 – Name, Role, Value ---------------- */

            if (form != null) {
                boolean missingFieldNames = false;
                for (PDField field : form.getFieldTree()) {
                    if (field.getFullyQualifiedName() == null) {
                        missingFieldNames = true;
                        break;
                    }
                }
                issues.add(new AccessibilityIssue(
                        "4.1.2", "A",
                        "Form fields have accessible names",
                        missingFieldNames ? "FAIL" : "PASS"));
            }


            /* ---------------- WCAG 2.4.5 - Bookmarks for long documents ---------------- */

            PDDocumentOutline outline = catalog.getDocumentOutline();
            issues.add(new AccessibilityIssue(
                    "2.4.5", "AA",
                    "Bookmarks present for navigation",
                    outline == null ? "WARN" : "PASS"));
        }

        return issues;
    }
}
