package com.dsoftn.services;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.ArrayList;

import com.dsoftn.CONSTANTS;
import com.dsoftn.services.text_handler.StyleSheetChar;
import com.dsoftn.services.text_handler.StyleSheetParagraph;
import com.dsoftn.services.text_handler.StyledParagraph;
import com.dsoftn.services.text_handler.StyledString;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UString;


public class RTWText {
    // Variables
    private String styledText = null;
    private String defaultCharCss = "";
    private String defaultParagraphCss = "";
    private List<StyledParagraph> styledParagraphs = new ArrayList<>();

    // Constructor
    public RTWText(List<StyledParagraph> styledParagraphs, Integer startIndex, Integer endIndex) {
        styledText = createStyledText(styledParagraphs, startIndex, endIndex);
        this.styledParagraphs = getStyledParagraphs(this.styledText);
    }

    public RTWText(List<StyledParagraph> styledParagraphs) {
        this(styledParagraphs, null, null);
    }

    public RTWText(RTWidget rtWidget) {
        this(rtWidget.getStyledParagraphs(), null, null);
    }

    public RTWText(String styledOrPlainText) {
        if (styledOrPlainText == null) {
            styledOrPlainText = "";
        }

        if (RTWText.isStyledText(styledOrPlainText)) {
            styledText = styledOrPlainText;
            this.styledParagraphs = getStyledParagraphs(this.styledText);
            return;
        } else {
            styledText = RTWText.transformToStyledText(styledOrPlainText);
            this.styledParagraphs = getStyledParagraphs(this.styledText);
        }
    }

    public RTWText() {}

    // Public methods

    public List<StyledParagraph> getStyledParagraphs() {
        return styledParagraphs;
    }

    public StyleSheetChar getDefaultCharCss() {
        return new StyleSheetChar(defaultCharCss);
    }

    public void setDefaultCharCss(StyleSheetChar defaultCharCss) {
        this.defaultCharCss = defaultCharCss.getCss();
    }

    public StyleSheetParagraph getDefaultParagraphCss() {
        return new StyleSheetParagraph(defaultParagraphCss);
    }

    public void setDefaultParagraphCss(StyleSheetParagraph defaultParagraphCss) {
        this.defaultParagraphCss = defaultParagraphCss.getCss();
    }

    public String loadTextFromRTWidget(RTWidget rtWidget) {
        styledText = createStyledText(rtWidget.getStyledParagraphs(), null, null);
        this.styledParagraphs = getStyledParagraphs(this.styledText);

        return styledText;
    }

    public String getStyledText() {
        return styledText;
    }

    public String getStyledText(RTWidget rtWidget) {
        return loadTextFromRTWidget(rtWidget);
    }

    public void loadStyledOrPlainText(String styledOrPlainText) {
        if (isStyledText(styledOrPlainText)) {
            styledText = styledOrPlainText;
            this.styledParagraphs = getStyledParagraphs(this.styledText);
            return;
        }
        styledText = RTWText.transformToStyledText(styledOrPlainText, defaultCharCss, defaultParagraphCss);
        this.styledParagraphs = getStyledParagraphs(this.styledText);
    }

    public String getPlainText() { return extractPlainText(styledText); }

    public Integer countParagraphs() {
        return countParagraphs(styledText);
    }

    // Private methods
    private String extractPlainText(String styledText) {
        String result = "";

        for (StyledParagraph paragraph : getStyledParagraphs()) {
            result += paragraph.getPlainText() + "\n";
        }

        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        return result.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");
    }

    private String createStyledText(List<StyledParagraph> styledParagraphs, Integer startIndex, Integer endIndex) {
        if (styledParagraphs == null) {
            return "";
        }
        if (startIndex == null) {
            startIndex = 0;
        }
        if (endIndex == null) {
            endIndex = Integer.MAX_VALUE;
        }

        styledParagraphs = buildValidStyledParagraphs(styledParagraphs, startIndex, endIndex);

        String result = CONSTANTS.RTW_TEXT_START + "\n";

        for (StyledParagraph paragraph : styledParagraphs) {
            result += CONSTANTS.RTW_TEXT_PARAGRAPH_START + "\n";
            result += paragraph.getCssParagraphStyle() + "\n";

            for (StyledString styledString : paragraph.getStyledStringList()) {
                result += styledString.getText() + "\n";
                result += styledString.getCssCharStyle() + "\n";
            }

            result += CONSTANTS.RTW_TEXT_PARAGRAPH_END + "\n";
        }

        result += CONSTANTS.RTW_TEXT_END + "\n";

        return result;
    }

    private List<StyledParagraph> buildValidStyledParagraphs(List<StyledParagraph> styledParagraphs, Integer startIndex, Integer endIndex) {
        List<StyledParagraph> resultAll = new ArrayList<>();
        int pos = 0;

        if ((startIndex == 0 || startIndex == null) && (endIndex == Integer.MAX_VALUE || endIndex == null)) {
            return styledParagraphs;
        }

        if (startIndex == null) {
            startIndex = 0;
        }
        if (endIndex == null) {
            endIndex = Integer.MAX_VALUE;
        }

        for (StyledParagraph paragraph : styledParagraphs) {
            StyledParagraph workingParagraph = paragraph.duplicate();
            int paragraphLength = workingParagraph.getPlainText().length();
            
            // Case when paragraph is before startIndex
            if (startIndex > pos + paragraphLength) {
                continue;
            }

            // Case when paragraph is after endIndex
            if (endIndex <= pos) {
                break;
            }

            // Case when complete paragraph is in range
            if (startIndex <= pos && endIndex >= pos + paragraphLength) {
                resultAll.add(workingParagraph);
                pos += paragraphLength;
                continue;
            }

            // Case when paragraph is split
            if (startIndex > pos && endIndex < pos + paragraphLength) {
                workingParagraph.delete(0, startIndex - pos);
                workingParagraph.delete(endIndex - pos, paragraphLength);
                resultAll.add(workingParagraph);
                pos += paragraphLength;
                continue;
            }

            // Case when paragraph is split and endIndex is after paragraph
            if (startIndex > pos && endIndex >= pos + paragraphLength) {
                workingParagraph.delete(0, startIndex - pos);
                resultAll.add(workingParagraph);
                pos += paragraphLength;
                continue;
            }

            // Case when paragraph is split and startIndex is before paragraph
            if (startIndex <= pos && endIndex < pos + paragraphLength) {
                workingParagraph.delete(endIndex - pos, paragraphLength);
                resultAll.add(workingParagraph);
                pos += paragraphLength;
                continue;
            }

            UError.error("RTWText.buildValidStyledParagraphs: Should not reach this point.", "Error");
            pos += paragraphLength;
        }

        return resultAll;





        // boolean finished = false;
        // for (StyledParagraph paragraph : styledParagraphs) {
        //     StyledParagraph resultParagraph = new StyledParagraph(paragraph.getCssParagraphStyleObject(), new StyleSheetChar());
        //     resultParagraph.setStyledStringList(new ArrayList<>());

        //     List<StyledString> resultStyledStrings = new ArrayList<>();

        //     int pos = 0;
        //     for (StyledString styledString : paragraph.getStyledStringList()) {
        //         pos += styledString.getText().length();
        //         if (startIndex > pos) {
        //             continue;
        //         }
        //         if (startIndex < pos && startIndex >= pos - styledString.getText().length()) {
        //             if (endIndex <= pos) {
        //                 resultStyledStrings.add(new StyledString(0, 0, styledString.getText().substring(startIndex, endIndex), styledString.getCssCharStyleObject()));
        //                 finished = true;
        //                 break;
        //             }

        //             resultStyledStrings.add(new StyledString(0, 0, styledString.getText().substring(startIndex), styledString.getCssCharStyleObject()));
        //             continue;
        //         }
                
        //         if (finished) {
        //             break;
        //         }

        //         if (startIndex <= pos) {
        //             if (endIndex > pos) {
        //                 resultStyledStrings.add(styledString);
        //                 continue;
        //             }
        //             resultStyledStrings.add(new StyledString(0, 0, styledString.getText().substring(0, endIndex), styledString.getCssCharStyleObject()));
        //             finished = true;
        //             break;
        //         }
        //     }

        //     resultParagraph.setStyledStringList(resultStyledStrings);
        //     resultAll.add(resultParagraph);
        //     if (finished) {
        //         break;
        //     }

        //     int paragraphLength = paragraph.getStyledStringList().stream().mapToInt(styledString -> styledString.getText().length()).sum() + 1;
        //     startIndex -= paragraphLength;
        //     endIndex -= paragraphLength;

        // }

        // List<StyledParagraph> result = new ArrayList<>();

        // for (StyledParagraph paragraph : resultAll) {
        //     if (paragraph.getStyledStringList().isEmpty()) {
        //         continue;
        //     }
        //     result.add(paragraph);
        // }

        // styledParagraphs = result;
        // return result;
    }

    private List<StyledParagraph> getStyledParagraphs(String styledText) {
        List<StyledParagraph> result = new ArrayList<>();

        if (!RTWText.isStyledText(styledText)) {
            return result;
        }

        boolean isStarted = false;
        String parContent = "";
        for (String line : styledText.split(Pattern.quote("\n"))) {
            if (line.startsWith(CONSTANTS.RTW_TEXT_PARAGRAPH_START)) {
                isStarted = true;
                parContent = line + "\n";
                continue;
            }
            if (line.startsWith(CONSTANTS.RTW_TEXT_PARAGRAPH_END)) {
                parContent += line + "\n";
                StyledParagraph paragraph = new StyledParagraph(parContent);
                result.add(paragraph);
                isStarted = false;
                parContent = "";
                continue;
            }
            if (isStarted) {
                parContent += line + "\n";
            }
        }

        return result;
    }

    // Static methods

    public static String transformToPlainText(String styledOrPlainText) {
        if (RTWText.isStyledText(styledOrPlainText)) {
            return new RTWText(styledOrPlainText).extractPlainText(styledOrPlainText);
        }

        return styledOrPlainText.replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "").replaceAll("\\R", "\n");
    }

    public static String transformToPlainTextWithZeroWidthSpace(String plainText) {
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
        return text.startsWith(CONSTANTS.RTW_TEXT_START);
    }

    public static RTWText copy(RTWidget rtwWidget) {
        if (rtwWidget.getSelectedText().isEmpty()) {
            return new RTWText();
        }
        
        RTWText rtwText = new RTWText(rtwWidget.getStyledParagraphs(), rtwWidget.getSelection().getStart(), rtwWidget.getSelection().getEnd());

        return rtwText;
    }

    public static String transformToStyledText(String plainText, String defaultCharCss, String defaultParagraphCss) {
        if (plainText == null) {
            plainText = "";
        }

        if (RTWText.isStyledText(plainText)) {
            return plainText;
        }

        plainText = transformToPlainText(plainText);

        if (defaultCharCss == null) {
            defaultCharCss = "";
        }
        if (defaultParagraphCss == null) {
            defaultParagraphCss = "";
        }

        String result = CONSTANTS.RTW_TEXT_START + "\n";

        for (String line : plainText.split(Pattern.quote("\n"), -1)) {
            result += CONSTANTS.RTW_TEXT_PARAGRAPH_START + "\n";
            result += defaultParagraphCss + "\n";

            result += line + "\n";
            result += defaultCharCss + "\n";

            result += CONSTANTS.RTW_TEXT_PARAGRAPH_END + "\n";
        }

        result += CONSTANTS.RTW_TEXT_END + "\n";

        return result;
    }

    public static String transformToStyledText(String plainText) {
        return transformToStyledText(plainText, "", "");
    }
    
    /**
     * Plain or styled text
     */
    public static int countParagraphs(String text) {
        if (!RTWText.isStyledText(text)) {
            return UString.Count(text, "\n") + 1;
        }

        return UString.Count(text, CONSTANTS.RTW_TEXT_PARAGRAPH_START);

    }

    // Implement equals() and hashCode() methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RTWText rtwText = (RTWText) obj;
        return Objects.equals(styledText, rtwText.styledText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(styledText);
    }

}
