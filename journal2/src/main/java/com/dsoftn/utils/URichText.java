package com.dsoftn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import com.dsoftn.services.text_handler.StyleSheetChar;

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

    public static String getSpanCssStyles(List<StyleSheetChar> cssChars) {
        String result = "";

        StyleSheetChar curCss = null;
        if (cssChars.size() > 0) {
            curCss = cssChars.get(0);
        } else {
            return result;
        }

        int start = 0;

        for (int i = 0; i < cssChars.size(); i++) {
            if (i == cssChars.size() - 1) {
                if (!curCss.equals(cssChars.get(i))) {
                    result += start + "," + i + "\n";
                    result += curCss.getCss() + "\n";
                    start = i;
                    curCss = cssChars.get(i);
                }
                result += start + "," + (i + 1) + "\n";
                result += curCss.getCss() + "\n";
                break;
            }

            if (curCss.equals(cssChars.get(i))) {
                continue;
            }

            result += start + "," + i + "\n";
            result += curCss.getCss() + "\n";
            start = i;
            curCss = cssChars.get(i);
        }

        return result;
    }

    public static List<StyleSheetChar> getCharListFromSpanCssStyles(String styledText) {
        List<StyleSheetChar> css = new ArrayList<>();
        
        Integer start = null;
        Integer end = null;

        for (String line : styledText.split(Pattern.quote("\n"))) {
            if (start == null && end == null) {
                start = Integer.parseInt(line.split(Pattern.quote(","))[0]);
                end = Integer.parseInt(line.split(Pattern.quote(","))[1]);
                continue;
            }

            for (int i = start; i < end; i++) {
                css.add(i, new StyleSheetChar(line));
            }
            start = null;
            end = null;
        }

        return css;
    }



}
