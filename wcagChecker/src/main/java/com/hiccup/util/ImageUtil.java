package com.hiccup.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ImageUtil {

	public static String toBase64(PDImageXObject image) {
        try {
            BufferedImage bufferedImage = image.getImage();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
    }

}
