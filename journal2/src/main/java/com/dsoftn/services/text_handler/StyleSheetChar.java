package com.dsoftn.services.text_handler;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dsoftn.OBJECTS;
import com.dsoftn.utils.UString;

public class StyleSheetChar {
    // Variables
    private String fontName = null; //OBJECTS.SETTINGS.getvSTRING("DefaultTextToolbarFontName"); // -fx-font-family: 'Arial';
    private Integer fontSize = null; //OBJECTS.SETTINGS.getvINTEGER("DefaultTextToolbarFontSize"); // -fx-font-size: 12px;
    private String fgColor = null; //"#ffff00"; // -fx-fill: #ffff00;
    private String bgColor = null; //"transparent"; // -rtfx-background-color: transparent;
    private Boolean bold = null; // false; // -fx-font-weight: bold;
    private Boolean italic = null; // false; // -fx-font-style: italic;
    private Boolean underline = null; // false; // -fx-underline: true;
    private Boolean strike = null; // false; // -fx-strikethrough: true;

    private String stroke = null; //"transparent"; // -fx-stroke: #000000;
    private Integer strokeWidth = null; //0; // -fx-stroke-width: 1px;
    private String strokeType = null; //"outside"; // -fx-stroke-type: (inside, outside, centered)

    // Constructors

    public StyleSheetChar() {}

    public StyleSheetChar(String css) {
        setCss(css);
    }


    // Public methods

    public String getCss() {
        String css = "";

        if (fontName != null && !fontName.isEmpty()) {
            css += "-fx-font-family: '" + fontName + "';";
        }

        if (fontSize != null) {
            css += "-fx-font-size: " + fontSize + "px;";
        }

        if (fgColor != null && !fgColor.isEmpty()) {
            css += "-fx-fill: " + fgColor + ";";
        }

        if (bgColor != null && !bgColor.isEmpty()) {
            css += "-rtfx-background-color: " + bgColor + ";";
        }

        if (bold != null) {
            if (bold) {
                css += "-fx-font-weight: bold;";
            } else {
                css += "-fx-font-weight: normal;";
            }
        }

        if (italic != null) {
            if (italic) {
                css += "-fx-font-style: italic;";
            } else {
                css += "-fx-font-style: normal;";
            }
        }

        if (underline != null) {
            if (underline) {
                css += "-fx-underline: true;";
            } else {
                css += "-fx-underline: false;";
            }
        }

        if (strike != null) {
            if (strike) {
                css += "-fx-strikethrough: true;";
            } else {
                css += "-fx-strikethrough: false;";
            }
        }

        if (stroke != null && !stroke.isEmpty()) {
            css += "-fx-stroke: " + stroke + ";";
        }

        if (strokeWidth != null) {
            css += "-fx-stroke-width: " + strokeWidth + "px;";
        }

        if (strokeType != null && !strokeType.isEmpty()) {
            css += "-fx-stroke-type: " + strokeType + ";";
        }

        return css;
    }

    public void setCss(String css) {
        // Parse css string
        String[] cssArray = css.split(Pattern.quote(";"));
        for (String cssItem : cssArray) {
            String[] cssItemArray = cssItem.split(Pattern.quote(":"));
            if (cssItemArray.length == 2) {
                String cssItemName = cssItemArray[0].strip();
                String cssItemValue = cssItemArray[1].strip();
                switch (cssItemName) {
                    case "-fx-font-family":
                        setFontName(UString.stripCharacters(cssItemValue, "'"));
                        break;
                    case "-fx-font-size":
                        setFontSize(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-fill":
                        setFgColor(cssItemValue);
                        break;
                    case "-rtfx-background-color":
                        setBgColor(cssItemValue);
                        break;
                    case "-fx-font-weight":
                        setBold(cssItemValue.equals("bold"));
                        break;
                    case "-fx-font-style":
                        setItalic(cssItemValue.equals("italic"));
                        break;
                    case "-fx-underline":
                        setUnderline(cssItemValue.equals("true"));
                        break;
                    case "-fx-strikethrough":
                        setStrikethrough(cssItemValue.equals("true"));
                        break;
                    case "-fx-stroke":
                        setStroke(cssItemValue);
                        break;
                    case "-fx-stroke-width":
                        setStrokeWidth(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-stroke-type":
                        setStrokeType(cssItemValue);
                        break;
                }
            }
        }
    }

    public StyleSheetChar duplicate() {
        StyleSheetChar result = new StyleSheetChar();

        result.fontName = this.fontName;
        result.fontSize = this.fontSize;
        result.fgColor = this.fgColor;
        result.bgColor = this.bgColor;
        result.bold = this.bold;
        result.italic = this.italic;
        result.underline = this.underline;
        result.strike = this.strike;
        result.stroke = this.stroke;
        result.strokeWidth = this.strokeWidth;
        result.strokeType = this.strokeType;

        return result;
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will change current object</p>
     * @param styleObjectToMerge - StyleSheetChar to merge with
     */
    public void merge(StyleSheetChar styleObjectToMerge) {
        this.setCss(styleObjectToMerge.getCss());
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will change current object</p>
     * @param cssStyleToMerge - Css string to merge with
     */
    public void merge(String cssStyleToMerge) {
        this.setCss(cssStyleToMerge);
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will return new object and <b>will not</b> change current object</p>
     * @param styleObjectToMerge
     * @return StyleSheetChar - New StyleSheetChar object
     */
    public StyleSheetChar mergeGetNew(StyleSheetChar styleObjectToMerge) {
        StyleSheetChar result = duplicate();
        result.merge(styleObjectToMerge);
        return result;
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will return new object and <b>will not</b> change current object</p>
     * @param cssStyleToMerge
     * @return StyleSheetChar - New StyleSheetChar object
     */
    public StyleSheetChar mergeGetNew(String cssStyleToMerge) {
        StyleSheetChar result = duplicate();
        result.merge(cssStyleToMerge);
        return result;
    }

    // Properties getters and setters

    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }
    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) {
        if (fontSize == null) {
            this.fontSize = null;
            return;
        }

        if (fontSize < OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize")) {
            fontSize = OBJECTS.SETTINGS.getvINTEGER("MinTextToolbarFontSize");
        }
        if (fontSize > OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize")) {
            fontSize = OBJECTS.SETTINGS.getvINTEGER("MaxTextToolbarFontSize");
        }
        this.fontSize = fontSize;
    }
    public String getFgColor() { return fgColor; }
    public void setFgColor(String fgColor) { this.fgColor = fgColor; }
    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public Boolean isBold() { return bold; }
    public void setBold(Boolean bold) { this.bold = bold; }
    public Boolean isItalic() { return italic; }
    public void setItalic(Boolean italic) { this.italic = italic; }
    public Boolean isUnderline() { return underline; }
    public void setUnderline(Boolean underline) { this.underline = underline; }
    public Boolean isStrikethrough() { return strike; }
    public void setStrikethrough(Boolean strike) { this.strike = strike; }
    public String getStroke() { return stroke; }
    public void setStroke(String stroke) { this.stroke = stroke; }
    public Integer getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(Integer strokeWidth) { this.strokeWidth = strokeWidth; }
    public String getStrokeType() { return strokeType; }
    public void setStrokeType(String strokeType) { this.strokeType = strokeType; }

    // Overrides methods "equals()" and "hashCode()"
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        StyleSheetChar other = (StyleSheetChar) obj;

        return Objects.equals(this.fontName, other.fontName) &&
               this.fontSize == other.fontSize &&
               Objects.equals(this.fgColor, other.fgColor) &&
               Objects.equals(this.bgColor, other.bgColor) &&
               this.bold == other.bold &&
               this.italic == other.italic &&
               this.underline == other.underline &&
               this.strike == other.strike &&
               Objects.equals(this.stroke, other.stroke) &&
               this.strokeWidth == other.strokeWidth &&
               Objects.equals(this.strokeType, other.strokeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontName, fontSize, fgColor, bgColor, bold, italic, underline, strike, stroke, strokeWidth, strokeType);
    }

}
