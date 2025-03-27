package com.dsoftn.utils;

import java.util.regex.Pattern;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import javafx.scene.paint.Color;

public class URichText {
    public static String updateCSS(String oldCss, String updateCss) {
        String[] oldCssArray = oldCss.split(Pattern.quote(";"));
        String[] updateCssArray = updateCss.split(Pattern.quote(";"));

        String newCss = "";

        boolean isFound = false;
        
        // Replace old css with new css lines
        for (String css : oldCssArray) {
            isFound = false;
            String cssItem = css.split(Pattern.quote(":"))[0].strip();
            if (cssItem.isEmpty()) {
                continue;
            }

            for (String uCss : updateCssArray) {
                String uCssItem = uCss.split(Pattern.quote(":"))[0].strip();
                if (uCssItem.isEmpty()) {
                    continue;
                }
                if (cssItem.equals(uCssItem)) {
                    newCss += uCss + ";";
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                newCss += css + ";";
            }
        }
        // Add new css lines
        for (String uCss : updateCssArray) {
            String uCssItem = uCss.split(Pattern.quote(":"))[0].strip();
            if (uCssItem.isEmpty()) {
                continue;
            }
            if (!newCss.contains(uCssItem)) {
                newCss += uCss + ";";
            }
        }
        
        return newCss;
    }

    public static String removeCSS(String oldCss, String removeCss) {
        String[] oldCssArray = oldCss.split(Pattern.quote(";"));
        String[] removeCssArray = removeCss.split(Pattern.quote(";"));

        String newCss = "";

        for (String css : oldCssArray) {
            String cssItem = css.split(Pattern.quote(":"))[0].strip();
            if (cssItem.isEmpty()) {
                continue;
            }

            boolean isFound = false;
            for (String rCss : removeCssArray) {
                String rCssItem = rCss.split(Pattern.quote(":"))[0].strip();
                if (rCssItem.isEmpty()) {
                    continue;
                }
                if (cssItem.equals(rCssItem)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                newCss += css + ";";
            }
        }

        return newCss;
    }

    public static String colorToHexString(Color color) {
        int r = (int)(color.getRed() * 255);
        int g = (int)(color.getGreen() * 255);
        int b = (int)(color.getBlue() * 255);
        int a = (int)(color.getOpacity() * 255);
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    public static String convertRtfToPlainText(String rtf) {
        if (rtf == null) return null;
        
        RTFEditorKit rtfParser = new RTFEditorKit();
        Document doc = new DefaultStyledDocument();
        
        try {
            rtfParser.read(new java.io.StringReader(rtf), doc, 0);
            return doc.getText(0, doc.getLength()).trim();
        } catch (Exception e) {
            UError.exception("UJavaFX.convertRtfToPlainText: Failed to convert RTF to plain text", e);
            return null;
        }
    }

    
}
