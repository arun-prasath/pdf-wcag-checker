package com.hiccup.validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;

import com.hiccup.extractor.ImageExtractorEngine;
import com.hiccup.extractor.StructureTreeExtractor;
import com.hiccup.model.AccessibilityIssue;
import com.hiccup.model.ImageAltData;


public class PdfWcagValidator {

    public static List<AccessibilityIssue> validate(PDDocument document) throws Exception {

        List<AccessibilityIssue> issues = new ArrayList<>();


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

                issues.add(new AccessibilityIssue("1.3.1", "A",
                		"List structure present",
                		hasList ? "PASS" : "WARN"));
                
                issues.add(new AccessibilityIssue("1.3.1", "A",
                		"Table structure present",
                		hasTable ? "PASS" : "WARN"));

                issues.add(new AccessibilityIssue("2.4.6", "AA",
                        "Headings present for navigation",
                        hasHeading ? "PASS" : "FAIL"));

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


            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);
            /* ---------------- WCAG 1.4.4 – Resize Text ---------------- */

            issues.add(new AccessibilityIssue(
                    "1.4.4", "AA",
                    "Text content extractable (supports resize/zoom)",
                    extractedText.trim().isEmpty() ? "FAIL" : "PASS"));

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


            /* -------------------------------------------------------
               IMAGE VALIDATIONS
            ------------------------------------------------------- */

            boolean hasText = !extractedText.trim().isEmpty();
            int totalImages = 0;
            int largeImages = 0;
            Set<String> imageHashes = new HashSet<>();
            boolean possibleImageOnly = !hasText;

            for (PDPage page : document.getPages()) {

                PDResources resources = page.getResources();

                for (COSName name : resources.getXObjectNames()) {

                    PDXObject xObject = resources.getXObject(name);

                    if (xObject instanceof PDImageXObject image) {

                        totalImages++;

                        // --- Image size heuristic ---
                        int width = image.getWidth();
                        int height = image.getHeight();

                        if (width > 1000 || height > 1000) {
                            largeImages++;
                        }

                        // --- Simple duplicate detection ---
                        String hash = width + "x" + height + "_" + image.getSuffix();
                        imageHashes.add(hash);
                    }
                }
            }

            /* ---------------- WCAG 1.4.5 – Image-only PDF or images uses as text ---------------- */

            if (possibleImageOnly && totalImages > 0) {
                issues.add(new AccessibilityIssue(
                        "1.4.5", "AA",
                        "PDF appears to be image-only (scanned document)",
                        "FAIL"));
            } else if (totalImages > 0 && extractedText.length() < 50) {
                issues.add(new AccessibilityIssue(
                        "1.4.5", "AA",
                        "Very little text detected → possible image-based content",
                        "WARN"));
            }

            /* ---------------- ALT text Validation ---------------- */

            if (totalImages == 0) {
            	issues.add(new AccessibilityIssue(
                        "1.1.1", "A",
                        "No images found in document",
                        "PASS"));
            } else {
            	if (structureRoot == null) {
                    issues.add(new AccessibilityIssue(
                            "1.1.1", "A",
                            "Images present but no structure tree → alt text not possible",
                            "FAIL"));
                }

            	List<ImageAltData> imageAltResult = extractImagesWithAlt(document);
                int altTextCount = 0;
                for (ImageAltData data : imageAltResult) {
                	if (data.altText != null) {
                		altTextCount++;
                	}
                }

                if (totalImages == altTextCount) {
                	issues.add(new AccessibilityIssue(
                            "1.1.1", "A",
                            "All Images have ALT texts",
                            "PASS"));
                } else {
                	issues.add(new AccessibilityIssue(
                            "1.1.1", "A",
                            "ALT texts missing for " + (totalImages - altTextCount) + " out of " + totalImages + " images",
                            "FAIL"));
                }
            }

            /* ---------------- Large Image Detection ---------------- */

            /*if (largeImages > 0) {
                issues.add(new AccessibilityIssue(
                        "1.1.1", "A",
                        "Large images detected (" + largeImages + ") → likely require meaningful alt text",
                        "WARN"));
            }*/


        issues.sort(Comparator.comparing(AccessibilityIssue::getWcagCriterion));

        return issues;
    }

    public static List<ImageAltData> extractImagesWithAlt(PDDocument document) throws Exception {

        List<ImageAltData> result = new ArrayList<>();

        PDStructureTreeRoot root = document.getDocumentCatalog().getStructureTreeRoot();
        Map<Integer, String> mcidToAlt = StructureTreeExtractor.extractAltTextByMCID(root);

        int pageIndex = 0;

        for (PDPage page : document.getPages()) {

            pageIndex++;

            ImageExtractorEngine engine =
                    new ImageExtractorEngine(pageIndex, mcidToAlt);

            engine.processPage(page);

            result.addAll(engine.images);
        }

        return result;
    }

    public static String checkAltText(String altText) {

    	System.out.println("altText: " + altText);
        if (altText == null || altText.isBlank()) {
            return "WARN";
        }

        return "PASS";
    }


}
