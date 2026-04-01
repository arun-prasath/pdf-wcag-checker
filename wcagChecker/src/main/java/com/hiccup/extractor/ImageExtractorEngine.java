package com.hiccup.extractor;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.hiccup.model.ImageAltData;

public class ImageExtractorEngine extends PDFStreamEngine {

    public List<ImageAltData> images = new ArrayList<>();

    private int pageNumber;
    private Map<Integer, String> mcidToAlt;

    // Stack to track nested marked content
    private Deque<Integer> mcidStack = new ArrayDeque<>();

    public ImageExtractorEngine(int pageNumber, Map<Integer, String> mcidToAlt) {
        this.pageNumber = pageNumber;
        this.mcidToAlt = mcidToAlt;
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {

        String op = operator.getName();

        switch (op) {

            case "BDC": // Begin Marked Content with properties
                handleBDC(operands);
                break;

            case "BMC": // Begin Marked Content (no properties)
                mcidStack.push(null);
                break;

            case "EMC": // End Marked Content
                if (!mcidStack.isEmpty()) {
                    mcidStack.pop();
                }
                break;

            case "Do": // Draw object (image)
                handleImage(operands);
                break;
        }

        super.processOperator(operator, operands);
    }

    private void handleBDC(List<COSBase> operands) {

        if (operands.size() < 2) {
            mcidStack.push(null);
            return;
        }

        COSBase properties = operands.get(1);

        if (properties instanceof COSDictionary dict) {

            COSBase mcidBase = dict.getDictionaryObject(COSName.MCID);

            if (mcidBase instanceof COSNumber mcidNumber) {
                mcidStack.push(mcidNumber.intValue());
                return;
            }
        }

        mcidStack.push(null);
    }

    private void handleImage(List<COSBase> operands) throws IOException {

        COSName objectName = (COSName) operands.get(0);
        PDXObject xobject = getResources().getXObject(objectName);

        if (xobject instanceof PDImageXObject image) {

            Integer mcid = mcidStack.isEmpty() ? null : mcidStack.peek();

            String altText = (mcid != null) ? mcidToAlt.get(mcid) : null;

            images.add(new ImageAltData(
                    pageNumber,
                    mcid,
                    altText,
                    image
            ));
        }
    }
}