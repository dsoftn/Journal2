package com.dsoftn.services;

import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;

import com.dsoftn.CONSTANTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;


public class RTWText {
    // Variables
    private String text = null;
    private List<StyleSheetChar> cssChars = null;
    private List<StyleSheetParagraph> cssParagraphs = null;
    private Integer startIndex = null;
    private Integer endIndex = null;
    private String resultString = null;


    // Constructor
    public RTWText(String text, List<StyleSheetChar> cssChars, List<StyleSheetParagraph> cssParagraphs, Integer startIndex, Integer endIndex) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = text;
        }

        this.cssParagraphs = cssParagraphs;

        if (startIndex == null) {
            this.startIndex = 0;
        } else {
            this.startIndex = startIndex;
        }

        if (endIndex == null) {
            this.endIndex = this.text.length();
        } else {
            this.endIndex = endIndex;
        }

        if (this.startIndex == 0 && this.endIndex == this.text.length()) {
            this.cssChars = cssChars;
        } else {
            this.text = this.text.substring(this.startIndex, this.endIndex);
            this.cssChars = cssChars.subList(this.startIndex, this.endIndex);
        }
        getStyledText();
    }

    public RTWText(String text, List<StyleSheetChar> cssChars, List<StyleSheetParagraph> cssParagraphs) {
        this(text, cssChars, cssParagraphs, null, null);
    }

    public RTWText(String styledText) {
        createCharAndParLists(styledText);
    }

    public RTWText() {}

    // Public methods
    public String getDataFromRTWidget(RTWidget rtWidget) {
        this.text = rtWidget.getText();
        this.cssChars = rtWidget.cssStyles;
        this.cssParagraphs = rtWidget.cssParagraphStyles;
        this.startIndex = 0;
        this.endIndex = this.text.length();
        return getStyledText();
    }

    public String getStyledText() {
        if (resultString == null) {
            resultString = getResultHeader() + "\n"
                + getResultCharStyle() + "\n"
                + getResultParagraphStyle() + "\n"
                + getResultText();
        }

        return resultString;
    }

    public String getStyledText(RTWidget rtWidget) {
        getDataFromRTWidget(rtWidget);
        return getStyledText();
    }

    public void loadStyledText(String styledText) {
        createCharAndParLists(styledText);
    }

    public void setDataToRTWidget(RTWidget rtWidget) {
        rtWidget.cssStyles = this.cssChars;
        rtWidget.cssParagraphStyles = this.cssParagraphs;
        rtWidget.clear();
        rtWidget.insertText(0, this.text);
        for (int i = 0; i < this.cssChars.size(); i++) {
            rtWidget.setStyle(i, i + 1, this.cssChars.get(i).getCss());
        }
        for (int i = 0; i < this.cssParagraphs.size(); i++) {
            rtWidget.setParagraphStyle(i, this.cssParagraphs.get(i).getCss());
        }
        rtWidget.moveTo(0, 1);
        rtWidget.requestFollowCaret();
    }

    public List<StyleSheetChar> geCharStyles() { return this.cssChars; }

    public List<StyleSheetParagraph> getParagraphStyles() { return this.cssParagraphs; }

    public String getTextWithZeroWidthSpace() { return this.text; }

    public String getPlainText() { return this.text.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, ""); }

    public static String transformToPlainText(String textWithZeroWidthSpace) {
        return textWithZeroWidthSpace.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "").replaceAll("\\R", "\n");
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

        RTWText rtwText = new RTWText();
        rtwText.text = rtwWidget.getSelectedText();
        rtwText.cssChars = rtwWidget.cssStyles.subList(rtwWidget.getSelection().getStart(), rtwWidget.getSelection().getEnd());
        return rtwText;
    }

    public static void paste(RTWidget rtwWidget, String styledText) {
        if (!isStyledText(styledText)) {
            return;
        }

        RTWText rtwText = new RTWText(styledText);
        if (rtwText.text.isEmpty()) {
            return;
        }

        if (rtwText.text.startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
            rtwText.text = rtwText.text.substring(1);
            rtwText.cssChars.remove(0);
        }
        if (rtwText.text.endsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING) && !rtwText.text.contains("\n")) {
            rtwText.text = rtwText.text.substring(0, rtwText.text.length() - 1);
            rtwText.cssChars.remove(rtwText.cssChars.size() - 1);
        }

        rtwWidget.pastePlainText(null, rtwText.text, rtwText.cssChars);
    }


    // Private methods
    private void createCharAndParLists(String styledText) {
        this.text = extractText(styledText);
        this.cssChars = extractCssChars(styledText);
        this.cssParagraphs = extractCssParagraphs(styledText);
    }

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

        return result;
    }

    private List<StyleSheetChar> extractCssChars(String styledText) {
        List<StyleSheetChar> css = new ArrayList<>();

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
            if (!isCharStyle || line.strip().isEmpty()) {
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
            if (!isParagraphStyle || line.strip().isEmpty()) {
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

    private String getResultCharStyle() {
        String result = CONSTANTS.RTW_TEXT_CHAR_STYLE_START;

        if (cssChars == null) {
            result += "\n";
            result += CONSTANTS.RTW_TEXT_CHAR_STYLE_END;
            return result;
        }
        result += "\n";

        StyleSheetChar curCss = null;
        if (cssChars.size() > 0) {
            curCss = cssChars.get(0);
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

        result += CONSTANTS.RTW_TEXT_CHAR_STYLE_END;
        return result;
    }

    private String getResultParagraphStyle() {
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

    private String getResultText() {
        return CONSTANTS.RTW_TEXT_CONTENT + "\n" + this.text;
    }



}
