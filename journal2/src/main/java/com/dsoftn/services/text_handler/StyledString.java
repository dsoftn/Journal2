package com.dsoftn.services.text_handler;

import com.dsoftn.CONSTANTS;

public class StyledString {
    // StyleType enum
    public enum StyleType {
        NORMAL,
        FIND_REPLACE,
        INTEGER,
        DOUBLE,
        DATE,
        TIME,
        WEB_LINK,
        EMAIL,
        SERBIAN_MOBILE_NUMBER,
        SERBIAN_LANDLINE_NUMBER,
        INTERNATIONAL_PHONE_NUMBER
        ;

        public StyleType fromName(String name) {
            for (StyleType type : StyleType.values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    // Variables
    private String text = "";
    private int start = CONSTANTS.INVALID_ID;
    private int end = CONSTANTS.INVALID_ID;
    private String cssCharStyle = null;
    private String css2 = "";
    private StyleType styleTypeEnum = StyleType.NORMAL;

    // Constructor
    
    public StyledString(int start, int end, String text, StyleSheetChar charStyleObject, StyleType styleTypeEnum, String css2) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = charStyleObject.getCss();
        this.styleTypeEnum = styleTypeEnum;
        this.css2 = css2;
    }

    public StyledString(int start, int end, String text, StyleSheetChar charStyleObject, StyleType styleTypeEnum) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = charStyleObject.getCss();
        this.styleTypeEnum = styleTypeEnum;
    }

    public StyledString(int start, int end, String text, String cssCharStyle, StyleType styleTypeEnum, String css2) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = cssCharStyle;
        this.styleTypeEnum = styleTypeEnum;
        this.css2 = css2;
    }

    public StyledString(int start, int end, String text, String cssCharStyle, StyleType styleTypeEnum) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = cssCharStyle;
        this.styleTypeEnum = styleTypeEnum;
    }

    public StyledString(int start, int end, String text, StyleSheetChar charStyleObject) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = charStyleObject.getCss();
        this.styleTypeEnum = StyleType.NORMAL;
    }

    public StyledString(int start, int end, String text, String cssCharStyle) {
        this.text = text;
        this.start = start;
        this.end = end;
        this.cssCharStyle = cssCharStyle;
        this.styleTypeEnum = StyleType.NORMAL;
    }

    // Public methods

    public StyledString duplicate() {
        return new StyledString(start, end, text, cssCharStyle, styleTypeEnum);
    }

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }
    public int getEnd() { return end; }
    public void setEnd(int end) { this.end = end; }
    public StyleSheetChar getCssCharStyleObject() { return new StyleSheetChar(cssCharStyle); }
    public String getCssCharStyle() { return cssCharStyle; }
    public void setCssCharStyleObject(StyleSheetChar cssCharStyle) { this.cssCharStyle = cssCharStyle.getCss(); }
    public void setCssCharStyle(String cssCharStyle) { this.cssCharStyle = cssCharStyle; }
    public StyleType getStyleTypeEnum() { return styleTypeEnum; }
    public void setStyleTypeEnum(StyleType styleTypeEnum) { this.styleTypeEnum = styleTypeEnum; }
    public String getCss2() { return css2; }
    public void setCss2(String css2) { this.css2 = css2; }


}
