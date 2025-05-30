package com.dsoftn.services;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.ArrayList;

import com.dsoftn.CONSTANTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.URichText;
import com.dsoftn.utils.UString;


public class RTWText {
    // Variables
    private String resultString = null;


    // Constructor
    public RTWText(String text, List<StyleSheetChar> cssChars, List<StyleSheetParagraph> cssParagraphs, Integer startIndex, Integer endIndex) {
        if (text == null) {
            text = "";
        }

        if (startIndex == null) {
            startIndex = 0;
        }

        if (endIndex == null) {
            endIndex = text.length();
        }

        if (startIndex != 0 || endIndex != text.length()) {
            text = text.substring(startIndex, endIndex);
            cssChars = cssChars.subList(startIndex, endIndex);
            cssParagraphs = null;
        }

        resultString = getResultHeader() + "\n"
        + getResultCharStyle(cssChars) + "\n"
        + getResultParagraphStyle(cssParagraphs) + "\n"
        + getResultText(text);
    }

    public RTWText(String text, List<StyleSheetChar> cssChars, List<StyleSheetParagraph> cssParagraphs) {
        this(text, cssChars, cssParagraphs, null, null);
    }

    public RTWText(RTWidget rtWidget) {
        this(rtWidget.getTextNoAC(), rtWidget.cssStyles, rtWidget.cssParagraphStyles);
    }

    public RTWText(String styledOrPlainText) {
        if (styledOrPlainText == null) {
            styledOrPlainText = "";
        }

        if (RTWText.isStyledText(styledOrPlainText)) {
            resultString = styledOrPlainText;
            return;
        } else {
            resultString =RTWText.transformToStyledText(styledOrPlainText);
        }
    }

    public RTWText() {}

    // Public methods
    public String loadDataFromRTWidget(RTWidget rtWidget) {
        resultString = getResultHeader() + "\n"
        + getResultCharStyle(rtWidget.cssStyles) + "\n"
        + getResultParagraphStyle(rtWidget.cssParagraphStyles) + "\n"
        + getResultText(rtWidget.getTextNoAC());

        return resultString;
    }

    public String getStyledText() {
        return resultString;
    }

    public String getStyledText(RTWidget rtWidget) {
        return loadDataFromRTWidget(rtWidget);
    }

    public void loadStyledText(String styledText) {
        if (!isStyledText(styledText)) {
            UError.error("RTWText.loadStyledText: Styled text is not valid", "Styled text is not valid");
            return;
        }

        resultString = styledText;
    }

    public void setDataToRTWidget(RTWidget rtWidget) {
        if (resultString == null) {
            UError.error("RTWText.setDataToRTWidget: Styled text is not loaded", "Styled text is not loaded");
            return;
        }

        rtWidget.cssStyles.clear();
        rtWidget.cssParagraphStyles.clear();
        rtWidget.clear();

        rtWidget.cssStyles = extractCssChars(resultString);
        rtWidget.cssParagraphStyles = extractCssParagraphs(resultString);
        rtWidget.insertText(0, extractText(resultString));
        for (int i = 0; i < rtWidget.cssStyles.size(); i++) {
            rtWidget.setStyle(i, i + 1, rtWidget.cssStyles.get(i).getCss());
        }
        for (int i = 0; i < rtWidget.cssParagraphStyles.size(); i++) {
            rtWidget.setParagraphStyle(i, rtWidget.cssParagraphStyles.get(i).getCss());
        }
    }

    public String getPlainText() { return extractText(resultString).replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, ""); }

    public static String transformToPlainText(String text) {
        if (RTWText.isStyledText(text)) {
            String result = "";

            boolean isText = false;
    
            for (String line : text.split(Pattern.quote("\n"))) {
                if (line.equals(CONSTANTS.RTW_TEXT_CONTENT)) {
                    isText = true;
                    continue;
                }
                if (!isText) {
                    continue;
                }
    
                result += line + "\n";
            }
    
            if (result.endsWith("\n")) {
                result = result.substring(0, result.length() - 1);
            }
    
            text = result;
        }
        
        return text.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "").replaceAll("\\R", "\n");
    }

    public static String transformToTextWithZeroWidthSpace(String plainText) {
        plainText = plainText.replaceAll("\\R", "\n");
        String result = "";

        for (String line : plainText.split(Pattern.quote("\n"))) {
            result += CONSTANTS.EMPTY_PARAGRAPH_STRING + line + "\n";
        }

        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    public static boolean isStyledText(String text) {
        return text.startsWith(CONSTANTS.RTW_TEXT_HEADER);
    }

    public static RTWText copy(RTWidget rtwWidget) {
        if (rtwWidget.getSelectedText().isEmpty()) {
            return new RTWText();
        }
        
        String selText = rtwWidget.getSelectedText();
        List<StyleSheetChar> selCss = rtwWidget.cssStyles.subList(rtwWidget.getSelection().getStart(), rtwWidget.getSelection().getEnd());
        List<StyleSheetParagraph> selParCss = rtwWidget.cssParagraphStyles.subList(rtwWidget.getParIndex(rtwWidget.getSelection().getStart()), rtwWidget.getParIndex(rtwWidget.getSelection().getEnd()) + 1);

        RTWText rtwText = new RTWText(selText, selCss, selParCss);

        return rtwText;
    }

    public static void paste(RTWidget rtwWidget, RTWText rtwText) {
        if (RTWText.transformToPlainText(rtwText.getStyledText()).isEmpty()) {
            return;
        }

        String pText = rtwText.extractText(rtwText.resultString);
        List<StyleSheetChar> pCharCss = rtwText.extractCssChars(rtwText.resultString);
        List<StyleSheetParagraph> pParCss = rtwText.extractCssParagraphs(rtwText.resultString);
        
        if (pText.startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
            pText = pText.substring(1);
            pCharCss.remove(0);
        }
        if (!pText.isEmpty()) {
            if (pText.endsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING) && !pText.contains("\n")) {
                pText = pText.substring(0, pText.length() - 1);
                pCharCss.remove(pCharCss.size() - 1);
            }
        }

        rtwWidget.pastePlainText(null, pText, pCharCss, pParCss);
    }

    public static void paste(RTWidget rtwWidget, String styledText) {
        paste(rtwWidget, new RTWText(styledText));
    }

    // Private methods
    private String extractText(String styledText) {
        String result = "";

        boolean isText = false;

        for (String line : styledText.split(Pattern.quote("\n"))) {
            if (line.equals(CONSTANTS.RTW_TEXT_CONTENT)) {
                isText = true;
                continue;
            }
            if (!isText) {
                continue;
            }

            result += line + "\n";
        }

        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result.replaceAll("\\R", "\n");
    }

    private List<StyleSheetChar> extractCssChars(String styledText) {
        List<StyleSheetChar> css = new ArrayList<>();

        if (styledText == null) {
            return css;
        }

        boolean isCharStyle = false;

        Integer start = null;
        Integer end = null;

        for (String line : styledText.split(Pattern.quote("\n"))) {
            if (line.equals(CONSTANTS.RTW_TEXT_CHAR_STYLE_START)) {
                isCharStyle = true;
                continue;
            }
            if (line.equals(CONSTANTS.RTW_TEXT_CHAR_STYLE_END)) {
                isCharStyle = false;
                break;
            }
            if (!isCharStyle) {
                continue;
            }

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

    private List<StyleSheetParagraph> extractCssParagraphs(String styledText) {
        List<StyleSheetParagraph> css = new ArrayList<>();

        if (styledText == null) {
            return css;
        }

        boolean isParagraphStyle = false;

        Integer start = null;
        Integer end = null;

        for (String line : styledText.split(Pattern.quote("\n"))) {
            if (line.equals(CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_START)) {
                isParagraphStyle = true;
                continue;
            }
            if (line.equals(CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_END)) {
                isParagraphStyle = false;
                break;
            }
            if (!isParagraphStyle) {
                continue;
            }

            if (start == null && end == null) {
                start = Integer.parseInt(line.split(Pattern.quote(","))[0]);
                end = Integer.parseInt(line.split(Pattern.quote(","))[1]);
                continue;
            }

            for (int i = start; i < end; i++) {
                css.add(i, new StyleSheetParagraph(line));
            }
            start = null;
            end = null;
        }

        return css;
    }

    private String getResultHeader() {
        return CONSTANTS.RTW_TEXT_HEADER;
    }

    private String getResultCharStyle(List<StyleSheetChar> cssChars) {
        String result = CONSTANTS.RTW_TEXT_CHAR_STYLE_START;

        if (cssChars == null) {
            result += "\n";
            result += CONSTANTS.RTW_TEXT_CHAR_STYLE_END;
            return result;
        }
        result += "\n";

        result += URichText.getSpanCssStyles(cssChars);

        result += CONSTANTS.RTW_TEXT_CHAR_STYLE_END;
        return result;
    }

    private String getResultParagraphStyle(List<StyleSheetParagraph> cssParagraphs) {
        String result = CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_START;

        if (cssParagraphs == null) {
            result += "\n";
            result += CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_END;
            return result;
        }
        result += "\n";

        StyleSheetParagraph curCss = null;
        if (cssParagraphs.size() > 0) {
            curCss = cssParagraphs.get(0);
        }
        int start = 0;

        for (int i = 0; i < cssParagraphs.size(); i++) {
            if (i == cssParagraphs.size() - 1) {
                if (!curCss.equals(cssParagraphs.get(i))) {
                    result += start + "," + i + "\n";
                    result += curCss.getCss() + "\n";
                    start = i;
                    curCss = cssParagraphs.get(i);
                }
                result += start + "," + (i + 1) + "\n";
                result += curCss.getCss() + "\n";
                break;
            }

            if (curCss.equals(cssParagraphs.get(i))) {
                continue;
            }

            result += start + "," + i + "\n";
            result += curCss.getCss() + "\n";
            start = i;
            curCss = cssParagraphs.get(i);
        }

        result += CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_END;
        return result;

    }

    private String getResultText(String text) {
        return CONSTANTS.RTW_TEXT_CONTENT + "\n" + text;
    }

    // Static methods

    public static String transformToStyledText(String plainText) {
        if (plainText == null) {
            plainText = "";
        }

        if (RTWText.isStyledText(plainText)) {
            return plainText;
        }

        plainText = plainText.replaceAll("\\R", "\n");
        plainText = RTWText.transformToTextWithZeroWidthSpace(plainText);

        String result = "";

        result += CONSTANTS.RTW_TEXT_HEADER + "\n";
        result += CONSTANTS.RTW_TEXT_CHAR_STYLE_START + "\n";
        result += "0," + plainText.length() + "\n";
        result += new StyleSheetChar().getCss() + "\n";
        result += CONSTANTS.RTW_TEXT_CHAR_STYLE_END + "\n";
        result += CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_START + "\n";
        result += "0," + UString.Count(plainText, "\n") + 1 + "\n";
        result += new StyleSheetParagraph().getCss() + "\n";
        result += CONSTANTS.RTW_TEXT_PARAGRAPH_STYLE_END + "\n";
        result += CONSTANTS.RTW_TEXT_CONTENT + "\n";
        result += plainText;

        return result;
    }
    
    /**
     * Plain or styled text
     */
    public static int countParagraphs(String text) {
        if (!RTWText.isStyledText(text)) {
            return UString.Count(text, "\n") + 1;
        }

        int counter = 0;
        boolean isText = false;

        for (String line : text.split(Pattern.quote("\n"))) {
            if (line.equals(CONSTANTS.RTW_TEXT_CONTENT)) {
                isText = true;
                continue;
            }
            if (!isText) {
                continue;
            }

            counter++;
        }

        return counter;
    }

    // Implement equals() and hashCode() methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RTWText rtwText = (RTWText) obj;
        return Objects.equals(resultString, rtwText.resultString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultString);
    }

}
