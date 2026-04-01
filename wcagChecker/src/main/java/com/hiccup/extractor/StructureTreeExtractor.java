package com.hiccup.extractor;

import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;

public class StructureTreeExtractor {

    public static Map<Integer, String> extractAltTextByMCID(PDStructureTreeRoot root) {
        Map<Integer, String> mcidToAlt = new HashMap<>();

        if (root == null) return mcidToAlt;

        for (Object kid : root.getKids()) {
            traverse(kid, mcidToAlt);
        }

        return mcidToAlt;
    }

    private static void traverse(Object node, Map<Integer, String> map) {

        if (node instanceof PDStructureElement element) {

            if ("Figure".equals(element.getStructureType())) {

                COSBase altBase = element.getCOSObject().getDictionaryObject(COSName.ALT);
                String altText = (altBase instanceof COSString)
                        ? ((COSString) altBase).getString()
                        : null;

                // Get MCID(s)
                COSBase k = element.getCOSObject().getDictionaryObject(COSName.K);

                if (k instanceof COSNumber mcid) {
                    map.put(mcid.intValue(), altText);
                }
            }

            for (Object kid : element.getKids()) {
                traverse(kid, map);
            }
        }
    }
}
