package com.dsoftn.services.text_handler;

import java.util.Objects;
import java.util.regex.Pattern;

import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.elements.TextEditToolbarController.AlignmentEnum;
import com.dsoftn.utils.UString;

public class StyleSheetParagraph {
    // Variables
    private String fontName = null; // -fx-font-family: 'Arial';
    private Integer fontSize = null; // -fx-font-size: 12px;
    private String fgColor = null; // -fx-fill: #ffff00;
    private String bgColor = null; // -rtfx-background-color: transparent;
    private Boolean bold = null; // -fx-font-weight: bold;
    private Boolean italic = null; // -fx-font-style: italic;
    private Boolean underline = null; // -fx-underline: true;
    private Boolean strike = null; // -fx-strikethrough: true;

    private Integer bgRadius = null; // -rtfx-background-radius: 0;
    private String borderColor = null; // -fx-border-color: #00ff00;
    private Integer borderWidth = null; // -fx-border-width: 0;
    private String borderStyle = null; // -fx-border-style: (solid, dashed, dotted)
    private Integer borderRadius = null; // -fx-border-radius: 0;
    private String padding = null; // -fx-padding: 0;
    private String alignment = null; // -fx-text-alignment: (left, right, center, justify)

    public String stroke = null; // -fx-stroke: #000000;
    public Integer strokeWidth = null; // -fx-stroke-width: 1px;
    public String strokeType = null; // -fx-stroke-type: (inside, outside, center)
    public String effect = null; // -fx-effect: dropshadow(gaussian, #000000, 10, 0, 0, 0);
    

    // Constructors

    public StyleSheetParagraph() {}

    public StyleSheetParagraph(String paragraphCss) {
        setCss(paragraphCss);
    }
    

    // Public methods

    public String getCss() {
        String css = "";

        if (!(fontName == null) && !fontName.isEmpty()) {
            css += "-fx-font-family: '" + fontName + "';";
        }

        if (!(fontSize == null)) {
            css += "-fx-font-size: " + fontSize + "px;";
        }

        if (!(fgColor == null) && !fgColor.isEmpty()) {
            css += "-fx-fill: " + fgColor + ";";
        }

        if (!(bgColor == null) && !bgColor.isEmpty()) {
            // css += "-rtfx-background-color: " + bgColor + ";";
            css += "-fx-background-color: " + bgColor + ";";
        }

        if (!(bold == null)) {
            if (bold) {
                css += "-fx-font-weight: bold;";
            } else {
                css += "-fx-font-weight: normal;";
            }
        }

        if (!(italic == null)) {
            if (italic) {
                css += "-fx-font-style: italic;";
            } else {
                css += "-fx-font-style: normal;";
            }
        }

        if (!(underline == null)) {
            if (underline) {
                css += "-fx-underline: true;";
            } else {
                css += "-fx-underline: false;";
            }
        }

        if (!(strike == null)) {
            if (strike) {
                css += "-fx-strikethrough: true;";
            } else {
                css += "-fx-strikethrough: false;";
            }
        }

        if (!(bgRadius == null)) {
            // css += "-rtfx-background-radius: " + bgRadius + ";";
            css += "-fx-background-radius: " + bgRadius + "px;";
        }

        if (!(borderColor == null) && !borderColor.isEmpty()) {
            css += "-fx-border-color: " + borderColor + ";";
        }

        if (!(borderWidth == null)) {
            css += "-fx-border-width: " + borderWidth + "px;";
        }

        if (!(borderStyle == null) && !borderStyle.isEmpty()) {
            css += "-fx-border-style: " + borderStyle + ";";
        }

        if (!(borderRadius == null)) {
            css += "-fx-border-radius: " + borderRadius + "px;";
        }

        if (!(padding == null) && !padding.isEmpty()) {
            css += "-fx-padding: " + padding + "px;";
        }

        if (!(alignment == null) && !alignment.isEmpty()) {
            css += "-fx-text-alignment: " + alignment + ";";
        }

        if (!(stroke == null) && !stroke.isEmpty()) {
            css += "-fx-stroke: " + stroke + ";";
        }

        if (!(strokeWidth == null)) {
            css += "-fx-stroke-width: " + strokeWidth + "px;";
        }

        if (!(strokeType == null) && !strokeType.isEmpty()) {
            css += "-fx-stroke-type: " + strokeType + ";";
        }

        if (!(effect == null) && !effect.isEmpty()) {
            css += "-fx-effect: " + effect + ";";
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
                    case "-fx-background-color":
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
                    case "-rtfx-background-radius":
                        setBgRadius(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-background-radius":
                        setBgRadius(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-border-color":
                        setBorderColor(cssItemValue);
                        break;
                    case "-fx-border-width":
                        setBorderWidth(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));    
                        break;
                    case "-fx-border-style":
                        setBorderStyle(cssItemValue);
                        break;
                    case "-fx-border-radius":
                        setBorderRadius(Integer.parseInt(cssItemValue.split(Pattern.quote("px"))[0]));
                        break;
                    case "-fx-padding":
                        setPadding(cssItemValue.split(Pattern.quote("px"))[0]);
                        break;
                    case "-fx-text-alignment":
                        setAlignment(cssItemValue);
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
                    case "-fx-effect":
                        setEffect(cssItemValue);
                        break;
                }
            }
        }
    }

    public StyleSheetParagraph duplicate() {
        StyleSheetParagraph result = new StyleSheetParagraph();

        result.fontName = this.fontName;
        result.fontSize = this.fontSize;
        result.fgColor = this.fgColor;
        result.bgColor = this.bgColor;
        result.bold = this.bold;
        result.italic = this.italic;
        result.underline = this.underline;
        result.strike = this.strike;
        result.bgRadius = this.bgRadius;
        result.borderColor = this.borderColor;
        result.borderWidth = this.borderWidth;
        result.borderStyle = this.borderStyle;
        result.borderRadius = this.borderRadius;
        result.padding = this.padding;
        result.alignment = this.alignment;
        result.stroke = this.stroke;
        result.strokeWidth = this.strokeWidth;
        result.strokeType = this.strokeType;
        result.effect = this.effect;

        return result;
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will change current object</p>
     * @param styleObjectToMerge - StyleSheetParagraph to merge with
     */
    public void merge(StyleSheetParagraph styleObjectToMerge) {
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
     * @return StyleSheetParagraph - New StyleSheetParagraph object
     */
    public StyleSheetParagraph mergeGetNew(StyleSheetParagraph styleObjectToMerge) {
        StyleSheetParagraph result = duplicate();
        result.merge(styleObjectToMerge);
        return result;
    }

    /**
     * Merge two style sheets, old styles will be overwritten by new styles
     * <p>This method will return new object and <b>will not</b> change current object</p>
     * @param cssStyleToMerge
     * @return StyleSheetParagraph - New StyleSheetParagraph object
     */
    public StyleSheetParagraph mergeGetNew(String cssStyleToMerge) {
        StyleSheetParagraph result = duplicate();
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

    public Integer getBgRadius() { return bgRadius; }
    public void setBgRadius(Integer bgRadius) { this.bgRadius = bgRadius; }
    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }
    public Integer getBorderWidth() { return borderWidth; }
    public void setBorderWidth(Integer borderWidth) { this.borderWidth = borderWidth; }
    public String getBorderStyle() { return borderStyle; }
    public void setBorderStyle(String borderStyle) { this.borderStyle = borderStyle; }
    public Integer getBorderRadius() { return borderRadius; }
    public void setBorderRadius(Integer borderRadius) { this.borderRadius = borderRadius; }
    public String getPadding() { return padding; }
    public void setPadding(String padding) { this.padding = padding; }
    public String getAlignment() { return alignment; }
    public AlignmentEnum getAlignmentEnum() {
        if (alignment == null) return AlignmentEnum.LEFT;
        
        switch (alignment) {
            case "left":
                return AlignmentEnum.LEFT;
            case "center":
                return AlignmentEnum.CENTER;
            case "right":
                return AlignmentEnum.RIGHT;
            case "justify":
                return AlignmentEnum.JUSTIFY;
            default:
                return AlignmentEnum.LEFT;
        }
    }
    public void setAlignment(String alignment) { this.alignment = alignment; }
    public void setAlignmentEnum(AlignmentEnum alignment) { this.alignment = alignment.name().toLowerCase(); }
    public String getStroke() { return stroke; }
    public void setStroke(String stroke) { this.stroke = stroke; }
    public Integer getStrokeWidth() { return strokeWidth; }
    public void setStrokeWidth(Integer strokeWidth) { this.strokeWidth = strokeWidth; }
    public String getStrokeType() { return strokeType; }
    public void setStrokeType(String strokeType) { this.strokeType = strokeType; }
    public String getEffect() { return effect; }
    public void setEffect(String effect) {
        if (effect != null) {
            if (effect.isEmpty()) {
                effect = null;
            } else if (effect.strip().startsWith("-fx-effect:")) {
                effect = effect.strip().substring(11).strip();
            }
        }
        this.effect = effect;
    }

    // Overrides methods "equals()" and "hashCode()"
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        StyleSheetParagraph other = (StyleSheetParagraph) obj;

        return Objects.equals(this.fontName, other.fontName) &&
               this.fontSize == other.fontSize &&
               Objects.equals(this.fgColor, other.fgColor) &&
               Objects.equals(this.bgColor, other.bgColor) &&
               this.bold == other.bold &&
               this.italic == other.italic &&
               this.underline == other.underline &&
               this.strike == other.strike &&
               this.bgRadius == other.bgRadius &&
               Objects.equals(this.borderColor, other.borderColor) &&
               this.borderWidth == other.borderWidth &&
               Objects.equals(this.borderStyle, other.borderStyle) &&
               this.borderRadius == other.borderRadius &&
               Objects.equals(this.padding, other.padding) &&
               Objects.equals(this.alignment, other.alignment) &&
               Objects.equals(this.stroke, other.stroke) &&
               this.strokeWidth == other.strokeWidth &&
               Objects.equals(this.strokeType, other.strokeType) &&
               Objects.equals(this.effect, other.effect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontName, fontSize, fgColor, bgColor, bold, italic, underline, strike, bgRadius, borderColor, borderWidth, borderStyle, borderRadius, padding, alignment, stroke, strokeWidth, strokeType, effect);
    }

}
