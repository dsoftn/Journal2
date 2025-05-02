package com.dsoftn.services.text_handler;

import com.dsoftn.CONSTANTS;
import com.dsoftn.models.StyleSheetChar;

public class MarkedItem {
    public enum MarkedType {
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
    }

    // Variables
    public int start = CONSTANTS.INVALID_ID;
    public int end = CONSTANTS.INVALID_ID;
    public int paragraph = CONSTANTS.INVALID_ID;
    public StyleSheetChar cssStyle = new StyleSheetChar(true);
    public MarkedType markedType = null;

    // Constructor
    public MarkedItem(int start, int end, int paragraph, StyleSheetChar styleObject, MarkedType markedType) {
        this.start = start;
        this.end = end;
        this.paragraph = paragraph;
        this.cssStyle = styleObject;
        this.markedType = markedType;
    }

    public MarkedItem(int start, int end, int paragraph, String cssStyle, MarkedType markedType) {
        this.start = start;
        this.end = end;
        this.paragraph = paragraph;
        this.cssStyle.setCss(cssStyle);
        this.markedType = markedType;
    }

}
