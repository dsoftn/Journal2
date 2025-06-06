package com.dsoftn.services;

import org.fxmisc.richtext.StyledTextArea;

public class RTWidgetClean extends StyledTextArea<String, String> {
    public RTWidgetClean() {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        this.setWrapText(true);
    }

}
