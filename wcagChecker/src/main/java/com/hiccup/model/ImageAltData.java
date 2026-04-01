package com.hiccup.model;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ImageAltData {
    public int pageNumber;
    public Integer mcid;
    public String altText;
    public PDImageXObject image;

    public ImageAltData(int pageNumber, Integer mcid, String altText, PDImageXObject image) {
        this.pageNumber = pageNumber;
        this.mcid = mcid;
        this.altText = altText;
        this.image = image;
    }
}
