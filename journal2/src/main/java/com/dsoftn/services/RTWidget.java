package com.dsoftn.services;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.StyledSegment;
import org.fxmisc.richtext.model.TwoDimensional;
import org.reactfx.util.Either;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.utils.UString;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import okhttp3.internal.Util;

import java.util.function.BiConsumer;


public class RTWidget extends StyledTextArea<String, String> {
    // Variables
    private StyleSheetChar cssChar = null;
    private StyleSheetParagraph cssParagraph = new StyleSheetParagraph();
    private Integer maxLines = null;
    private Integer maxChars = null;
    private Integer maxCharsPerLine = null;
    private int minTextWidgetHeight = 24;
    public List<StyleSheetChar> cssStyles = new ArrayList<>();
    public List<StyleSheetParagraph> cssParagraphStyles = new ArrayList<>();
    private TextHandler textHandler = null;
    private boolean ignoreTextChange = false;
    private boolean ignoreCaretPositionChange = false;
    private boolean ignoreTextChangePERMANENT = false;
    private boolean ignoreCaretPositionChangePERMANENT = false;
    private boolean busy = false;

    public boolean isOverwriteMode = false;
   
    // Constructors
    public RTWidget(StyleSheetChar cssStyleSheet, Integer maxLines, Integer maxChars, Integer maxCharsPerLine) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );
    
        this.cssChar = cssStyleSheet;
        this.maxLines = maxLines;
        this.maxChars = maxChars;
        this.maxCharsPerLine = maxCharsPerLine;

        this.cssParagraphStyles.add(cssParagraph.duplicate());
    }
    
    public RTWidget(StyleSheetChar cssStyleSheet) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        this.cssChar = cssStyleSheet;

        this.cssParagraphStyles.add(cssParagraph.duplicate());
    }

    public RTWidget() {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        this.cssChar = new StyleSheetChar();

        this.cssParagraphStyles.add(cssParagraph.duplicate());
    }

    // Public methods

    public void msgFromHandler(String messageSTRING) {
        // Process information from handler
    }

    public void msgFromHandler(StyleSheetChar styleSheet) {
        this.cssChar = styleSheet;
    }

    public void msgFromHandler(StyleSheetParagraph styleSheet, int paragraphIndex) {
        this.cssParagraph = styleSheet;
        this.cssParagraphStyles.set(paragraphIndex, styleSheet.duplicate());
        this.setParagraphStyle(paragraphIndex, styleSheet.getCss());
    }
    
    public void msgFromHandler(StyleSheetParagraph styleSheet) {
        msgFromHandler(styleSheet, this.getCurrentParagraph());
    }
    
    public void setCssChar(StyleSheetChar css) {
        if (css == null) {
            this.cssChar = new StyleSheetChar();
        } else {
            this.cssChar = css;
        }
    }

    public StyleSheetChar getCssChar() {
        return this.cssChar;
    }

    public void setParagraphCss(StyleSheetParagraph css) {
        this.cssParagraph = css;
    }

    public StyleSheetParagraph getParagraphCss() {
        return this.cssParagraph;
    }

    public void setMinTextWidgetHeight(int minTextWidgetHeight) {
        this.minTextWidgetHeight = minTextWidgetHeight;
        this.setMinHeight(minTextWidgetHeight);
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
    }

    public void setupWidget() {
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setWrapText(true);
        this.getStyleClass().add("rich-text");
        this.setMinHeight(minTextWidgetHeight);

        // Text change
        this.textProperty().addListener((obs, oldText, newText) -> {
            handleTextChange(obs, oldText, newText);
        });

        // Caret position change
        this.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            handleCaretPositionChange(obs, oldPos, newPos);
        });

        // Update widget height
        this.multiPlainChanges()
        .successionEnds(Duration.ofMillis(50))
        .subscribe(change -> {
            Platform.runLater(() -> {
                Double height = this.totalHeightEstimateProperty().getValue();
                if (height != null) {
                    this.setPrefHeight(height + 2);
                }
            });
        });

        // Mouse pressed
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            handleMousePressed(event);
        });

        
        // Key pressed
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (this.busy) {
                System.out.println("Busy: " + this.busy);
                event.consume();
                return;
            }

            // INSERT
            if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_INSERT();
            }
            // ENTER
            if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_ENTER();
            }
            // DELETE
            if (event.getCode() == KeyCode.DELETE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DELETE(event);
            }
            // BACKSPACE
            if (event.getCode() == KeyCode.BACK_SPACE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_BACKSPACE(event);
            }
            // ARROW UP
            if (event.getCode() == KeyCode.UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_UP(event);
            }
            // ARROW DOWN
            if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DOWN(event);
            }
            // PAGE UP
            if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgUP(event);
            }
            // PAGE DOWN
            if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgDOWN(event);
            }


        });


        
        // this.appendText("-");
        // this.replaceText("");

        // inlineCssTextArea.getStyleOfChar(0)
        // inlineCssTextArea.getParagraph(0).getText(); // Prvi paragraf
        // inlineCssTextArea.getParagraphs().size();
        // inlineCssTextArea.setParagraphStyle(1, "-fx-font-size: 20px; -fx-text-fill: blue;");

        this.requestFocus();
    }

    // Private methods

    private void msgForHandler(StyleSheetChar css) {
        if (!this.getSelectedText().isEmpty()) {
            return;
        }

        textHandler.msgFromWidget(css.duplicate());
    }

    private void msgForHandler(StyleSheetParagraph cssParagraph) {
        textHandler.msgFromWidget(cssParagraph.duplicate());
    }

    private void msgForHandler(String messageSTRING) {
    }

    private void handleMousePressed(MouseEvent event) {
        this.busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        for (int i = 0; i < this.getParagraphs().size(); i++) {
            if (this.getParagraph(i).getText().isEmpty() || this.getParagraph(i).getText().equals("\n")) {
                this.insertText(i, 0, CONSTANTS.EMPTY_PARAGRAPH_STRING);
            }
        }

        Platform.runLater(() -> {
            this.busy = true;
            int moveToCaretPosition = this.getCaretPosition() - UString.Count(this.getText().substring(0, this.getCaretPosition()), CONSTANTS.EMPTY_PARAGRAPH_STRING);
            for (int i = 0; i < this.getParagraphs().size(); i++) {
                if (this.getParagraph(i).getText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING +"\n") || this.getParagraph(i).getText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                    this.deleteText(i, 0, i, 1);
                }
            }

            Platform.runLater(() -> {
                this.busy = true;
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;

                this.moveTo(moveToCaretPosition);

                Platform.runLater(() -> {
                    this.busy = false;
                });
            });
        });
    }

    private void handleKeyPressed_INSERT() {
        this.busy = true;
        isOverwriteMode = !isOverwriteMode;
        if (!isOverwriteMode) {
            if (this.getCaretPosition() < this.cssStyles.size()) {
                // If caret is not at end then there is marked character, demark it
                this.setStyle(this.getCaretPosition(), this.getCaretPosition() + 1, cssStyles.get(this.getCaretPosition()).getCss());
            }
        }
        if (this.cssStyles.size() > 0) {
            if (this.getCaretPosition() < this.cssStyles.size()) {
                handleCaretPositionChange(null, this.getCaretPosition() + 1, this.getCaretPosition());
            }
            else {
                handleCaretPositionChange(null, this.getCaretPosition() - 1, this.getCaretPosition());
            }
        }

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_ENTER() {
        // Add new paragraph
        this.busy = true;
        if (OBJECTS.SETTINGS.getvBOOLEAN("PreserveParagraphStyle")) {
            this.cssParagraphStyles.add(getCurrentParagraph() + 1, this.cssParagraph.duplicate());
        }
        else {
            this.cssParagraphStyles.add(getCurrentParagraph() + 1, new StyleSheetParagraph());
        }

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_DELETE(KeyEvent event) {
        this.busy = true;
        // If caret is at the end of the text then do nothing
        if (this.getCaretPosition() == this.cssStyles.size()) {
            event.consume();
            Platform.runLater(() -> {
                this.busy = false;
            });
            return;
        }
        // Check if deleted char is "\n"
        if (this.getText().charAt(this.getCaretPosition()) == '\n') {
            this.cssParagraphStyles.remove(this.getCurrentParagraph());
        }

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_BACKSPACE(KeyEvent event) {
        this.busy = true;
        // If caret is at the beginning of the text then do nothing
        if (this.getCaretPosition() == 0) {
            event.consume();
            Platform.runLater(() -> {
                this.busy = false;
            });
            return;
        }
        // Check if deleted char is "\n"
        if (this.getText().charAt(this.getCaretPosition() - 1) == '\n') {
            this.cssParagraphStyles.remove(this.getCurrentParagraph() - 1);
        }

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_UP(KeyEvent event) {
        HELPER_UpDownPgUpPgDown(event);
    }

    private void handleKeyPressed_DOWN(KeyEvent event) {
        HELPER_UpDownPgUpPgDown(event);
    }

    private void handleKeyPressed_PgUP(KeyEvent event) {
        this.busy = true;
        if (this.getCurrentParagraph() == 0) {
            event.consume();
            this.busy = false;
            return;
        }

        if (
            this.getParagraph(this.getCurrentParagraph()).getText().isEmpty() || 
            this.getParagraph(this.getCurrentParagraph()).getText().equals("\n") ||
            this.getParagraph(this.getCurrentParagraph() - 1).getText().isEmpty() || 
            this.getParagraph(this.getCurrentParagraph() - 1).getText().equals("\n")) {
            
            event.consume();
            for (int i = this.getCurrentParagraph() - 1; i > 0; i--) {
                if (!this.getParagraph(i).getText().isEmpty() && !this.getParagraph(i).getText().equals("\n")) {
                    this.moveTo(i, 0);
                    this.requestFollowCaret();
                    Platform.runLater(() -> {
                        this.busy = false;
                    });
                    return;
                }
            }
            
            this.moveTo(0, 0);
            this.requestFollowCaret();
            Platform.runLater(() -> {
                this.busy = false;
            });
            return;
        }

        HELPER_UpDownPgUpPgDown(event);
    }

    private void handleKeyPressed_PgDOWN(KeyEvent event) {
        this.busy = true;
        if (this.getCurrentParagraph() == this.getParagraphs().size() - 1) {
            event.consume();
            this.busy = false;
            return;
        }

        if (
            this.getParagraph(this.getCurrentParagraph()).getText().isEmpty() || 
            this.getParagraph(this.getCurrentParagraph()).getText().equals("\n") ||
            this.getCurrentParagraph() < this.getParagraphs().size() - 1 &&
            (this.getParagraph(this.getCurrentParagraph() + 1).getText().isEmpty() || 
            this.getParagraph(this.getCurrentParagraph() + 1).getText().equals("\n"))) {
            
            event.consume();
            for (int i = this.getCurrentParagraph() + 1; i < this.getParagraphs().size(); i++) {
                if (!this.getParagraph(i).getText().isEmpty() && !this.getParagraph(i).getText().equals("\n")) {
                    this.moveTo(i, 0);
                    this.requestFollowCaret();
                    Platform.runLater(() -> {
                        this.busy = false;
                    });
                    return;
                }
            }
            
            this.moveTo(this.getParagraphs().size() - 1, 0);
            this.requestFollowCaret();
            Platform.runLater(() -> {
                this.busy = false;
            });
            return;
        }

        HELPER_UpDownPgUpPgDown(event);
    }

    private void HELPER_UpDownPgUpPgDown(KeyEvent event) {
        this.busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        int savedPar = this.getCurrentParagraph();
        int savedCol = this.getCaretColumn();

        for (int i = 0; i < this.getParagraphs().size(); i++) {
            if (this.getParagraph(i).getText().isEmpty() || this.getParagraph(i).getText().equals("\n")) {
                this.insertText(i, 0, CONSTANTS.EMPTY_PARAGRAPH_STRING);
            }
        }
        this.moveTo(savedPar, savedCol);

        Platform.runLater(() -> {
            this.busy = true;
            int laterPar = this.getCurrentParagraph();
            int laterCol = this.getCaretColumn();
            String laterParText = this.getParagraph(laterPar).getText();

            for (int i = 0; i < this.getParagraphs().size(); i++) {
                if (this.getParagraph(i).getText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING +"\n") || this.getParagraph(i).getText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                    this.deleteText(i, 0, i, 1);
                }
            }

            Platform.runLater(() -> {
                this.busy = true;
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;

                if (laterParText.isEmpty() || laterParText.equals("\n") || laterParText.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING) || laterParText.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING + "\n")) {
                    this.moveTo(laterPar, 0);
                }
                else {
                    this.moveTo(laterPar, laterCol);
                }
                Platform.runLater(() -> {
                    this.busy = false;
                });
            });
        });
    }

    private void HELPER_printSegments(String msg, int curP) {
        System.out.println("Segments (" + this.cssStyles.size() + "):" + msg + "  Caret: " + this.getCaretPosition() + "  CurPar: " + this.getCurrentParagraph() + "  WorkPar: " + curP);
        for (int i = 0; i < this.cssStyles.size(); i++) {
            System.out.println("[" + this.getText().charAt(i) + "]");
        }
    }

    private void handleTextChange(Object obs, String oldText, String newText) {
        if (this.ignoreTextChange || this.ignoreTextChangePERMANENT) {
            this.ignoreTextChange = false;
            return;
        }
        this.busy = true;

        // Added new characters
        if (newText.length() > oldText.length()) {
            for (int i = this.getCaretPosition() - (newText.length() - oldText.length()); i < this.getCaretPosition(); i++) {
                this.cssStyles.add(i, cssChar.duplicate());
            }
            this.setStyle(this.getCaretPosition() - (newText.length() - oldText.length()), this.getCaretPosition(), cssChar.getCss());
        }
        // Removed characters
        else if (newText.length() < oldText.length()) {
            for (int i = newText.length(); i < oldText.length(); i++) {
                this.cssStyles.remove(this.getCaretPosition());
            }
        }
        // New and Old text are same length, that occurs usually in OVERWRITE mode
        else {
            this.cssChar = this.cssStyles.get(this.getCaretPosition()).duplicate();
            this.setStyle(this.getCaretPosition(), this.getCaretPosition() + 1, this.cssChar.getCss());
        }

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleCaretPositionChange(Object obs, Integer oldPos, Integer newPos) {
        if (this.ignoreCaretPositionChange || this.ignoreCaretPositionChangePERMANENT) {
            this.ignoreCaretPositionChange = false;
            return;
        }

        // When user in overwrite mode change char with same char then "textProperty" does not trigger listener
        // This code will handle that situation and properly change character style
        if (isOverwriteMode && this.cssStyles.size() > 0 && newPos.intValue() > oldPos.intValue() && this.getSelectedText().isEmpty()) {
            if (!this.cssChar.equals(this.cssStyles.get(newPos.intValue() - 1))) {
                this.cssChar = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                this.setStyle(newPos.intValue() - 1, newPos.intValue(), this.cssChar.getCss());
                sendToHandlerCharAndParagraphCurrentStyle();
            }
        }

        int curPar = this.offsetToPosition(newPos.intValue(), TwoDimensional.Bias.Forward).getMajor();

        // Case when caret is at the beginning of the paragraph in both modes
        // Global CSS will be as char after caret (Char in pos 0 in paragraph)
        if (newPos.intValue() == this.cssStyles.size()) {
            // Caret is at the end of the text
            if (this.cssStyles.size() > 0 && (!cssChar.equals(this.cssStyles.get(newPos.intValue() - 1)) || !cssParagraph.equals(cssParagraphStyles.get(curPar)))) {
                cssChar = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();
            }
        } else {
            TwoDimensional.Position pos = this.offsetToPosition(newPos.intValue(), TwoDimensional.Bias.Forward);
            if (pos.getMinor() == 0) {
                // If caret is at the beginning of the paragraph
                if (!this.cssChar.equals(this.cssStyles.get(newPos.intValue())) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                    cssChar = this.cssStyles.get(newPos.intValue() + 1).duplicate();
                    cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                    sendToHandlerCharAndParagraphCurrentStyle();
                }
            }
        }

        // Case when OVERWRITE mode is on or caret is not at the beginning of the text in both modes
        if (newPos.intValue() != 0 || isOverwriteMode) {
            if (isOverwriteMode) {
                // Update global CSS as CSS of char under caret in OVERWRITE mode
                if (newPos.intValue() == this.cssStyles.size()) {
                    // Caret is at the end of the text
                    if (!cssChar.equals(this.cssStyles.get(newPos.intValue() - 1)) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                        cssChar = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                        cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                        sendToHandlerCharAndParagraphCurrentStyle();
                    }
                }
                else {
                    // Caret is in the middle of the text
                    if (!cssChar.equals(this.cssStyles.get(newPos.intValue())) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                        cssChar = this.cssStyles.get(newPos.intValue()).duplicate();
                        cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                        sendToHandlerCharAndParagraphCurrentStyle();
                    }
                }

                // Demark old character and Mark new character
                if (isOverwriteMode) {
                    // If oldPos is not at the end of the text, demark last character
                    if (oldPos.intValue() < this.cssStyles.size()) {
                        this.setStyle(oldPos.intValue(), oldPos.intValue() + 1, this.cssStyles.get(oldPos.intValue()).getCss());
                    }
                    // If there is no selection and caret is not at end, mark new character
                    if (this.getSelectedText().isEmpty() && newPos.intValue() < this.cssStyles.size()) {
                        StyleSheetChar newStylesheet = this.cssStyles.get(newPos.intValue()).duplicate();
                        newStylesheet.setBgColor("#dfdfdf");
                        newStylesheet.setFgColor("#202020");
                        this.setStyle(newPos.intValue(), newPos.intValue() + 1, newStylesheet.getCss());
                        
                        // Update global CSS with char under caret and update toolbar
                        if (!cssChar.equals(this.cssStyles.get(newPos.intValue())) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                            cssChar = this.cssStyles.get(newPos.intValue()).duplicate();
                            cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                            sendToHandlerCharAndParagraphCurrentStyle();
                        }
                    }
                }
            }
            else {
                // Case when INSERT mode is on
                if (this.getText().charAt(newPos.intValue() - 1) == '\n') {
                    // If caret is at the beginning of the paragraph, update global CSS with next character
                    if (this.cssStyles.size() > newPos.intValue() + 1) {
                        if (!cssChar.equals(this.cssStyles.get(newPos.intValue() + 1)) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                            cssChar = this.cssStyles.get(newPos.intValue() + 1).duplicate();
                            cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                            sendToHandlerCharAndParagraphCurrentStyle();
                        }
                    }
                } else {
                    if (!cssChar.equals(this.cssStyles.get(newPos.intValue() - 1)) || !cssParagraph.equals(cssParagraphStyles.get(curPar))) {
                        cssChar = this.cssStyles.get(newPos.intValue() - 1).duplicate();
                        cssParagraph = cssParagraphStyles.get(curPar).duplicate();
                        sendToHandlerCharAndParagraphCurrentStyle();
                    }
                }
            }

        }
    }

    private void sendToHandlerCharAndParagraphCurrentStyle() {
        // Send current char style to Handler
        msgForHandler(this.cssChar);
        // Send current paragraph style to Handler
        msgForHandler(this.cssParagraph);
    }


}
