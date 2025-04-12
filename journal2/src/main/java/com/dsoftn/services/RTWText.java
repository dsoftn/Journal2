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

    private String styledText = null;


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
        return textWithZeroWidthSpace.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");
    }

    public static String transformToTextWithZeroWidthSpace(String plainText) {
        String result = "";

        for (String line : plainText.split(Pattern.quote("\n"))) {
            result += CONSTANTS.EMPTY_PARAGRAPH_STRING + line + "\n";
        }

        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    public static String getStyledTextHeader() {
        return CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "RTW";
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
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "T")) {
                isText = true;
                continue;
            }
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-T")) {
                isText = false;
                break;
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
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "C")) {
                isCharStyle = true;
                continue;
            }
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-C")) {
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
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "P")) {
                isParagraphStyle = true;
                continue;
            }
            if (line.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-P")) {
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
        return CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "RTW";
    }

    private String getResultCharStyle() {
        String result = CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "C";

        if (cssChars == null) {
            result += "\n";
            result += CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-C";
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

        result += CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-C";
        return result;
    }

    private String getResultParagraphStyle() {
        String result = CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "P";

        if (cssParagraphs == null) {
            result += "\n";
            result += CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-P";
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

        result += CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "-P";
        return result;

    }

    private String getResultText() {
        return CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(3) + "T" + "\n" + this.text;
    }



}
