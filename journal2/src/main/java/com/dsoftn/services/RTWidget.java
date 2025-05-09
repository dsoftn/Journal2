package com.dsoftn.services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.Caret.CaretVisibility;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.TwoDimensional;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.controllers.elements.TextInputController;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.models.StyleSheetChar;
import com.dsoftn.models.StyleSheetParagraph;
import com.dsoftn.services.text_handler.ACHandler;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.services.timer.ContinuousTimer;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.UString;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;


public class RTWidget extends StyledTextArea<String, String> { // InlineCssTextArea
    // Variables
    public StyleSheetChar cssChar = null; // Current char style
    private StyleSheetChar cssCharPrev = null; // Previous char style that has been sent to handler
    private StyleSheetParagraph cssParagraph = new StyleSheetParagraph(); // Current paragraph style
    private StyleSheetParagraph cssParagraphPrev = null; // Previous paragraph style that has been sent to handler
    private Integer maxNumberOfParagraphs = Integer.MAX_VALUE;
    private boolean readOnly = false;
    private int minTextWidgetHeight = 24;
    public List<StyleSheetChar> cssStyles = new ArrayList<>();
    public List<StyleSheetParagraph> cssParagraphStyles = new ArrayList<>();
    private TextHandler textHandler = null;
    private boolean ignoreTextChange = false;
    public boolean ignoreCaretPositionChange = false;
    public boolean ignoreTextChangePERMANENT = false;
    private boolean ignoreCaretPositionChangePERMANENT = false;
    public boolean busy = false;
    public boolean stateChanged = true; // Used with Undo/Redo
    private ContinuousTimer timer = new ContinuousTimer(OBJECTS.SETTINGS.getvINTEGER("IntervalBetweenTextChangesMS"));
    private PauseTransition pauseAC = null;
    // public TextHandler.Behavior behavior = null;
    public ACHandler ac = null;

    public boolean isOverwriteMode = false;
   
    // Constructors
    public RTWidget(StyleSheetChar cssStyleSheet, Integer maxNumberOfParagraphs) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );
    
        this.cssChar = cssStyleSheet;
        
        if (maxNumberOfParagraphs == null) {
            maxNumberOfParagraphs = Integer.MAX_VALUE;
        } else if (maxNumberOfParagraphs < 1) {
            maxNumberOfParagraphs = 1;
        } else {
            this.maxNumberOfParagraphs = maxNumberOfParagraphs;
        }

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

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            this.setShowCaret(CaretVisibility.OFF);
            timer.stop();
        } else {
            this.setShowCaret(CaretVisibility.ON);
            timer.play(() -> takeSnapshot());
        }
        
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setMaxNumberOfParagraphs(Integer maxNumberOfParagraphs) {
        if (maxNumberOfParagraphs == null) {
            maxNumberOfParagraphs = Integer.MAX_VALUE;
        } else if (maxNumberOfParagraphs < 1) {
            maxNumberOfParagraphs = 1;
        }

        this.maxNumberOfParagraphs = maxNumberOfParagraphs;
    }

    public boolean canBeClosed() {
        if (ac.hasCurrentAC()) {
            return false;
        }

        return true;
    }

    // public void setBehavior(TextHandler.Behavior behavior) {
    //     this.behavior = behavior;
    //     ac = new ACHandler(this);
    // }

    public void msgFromHandler(String messageSTRING) {
        // Process information from handler
        if (messageSTRING.equals("CUT")) {
            if (!this.getSelectedText().isEmpty()) {
                copySelectedText();
                handleKeyPressed_DELETE(null);
            }
        }
        else if (messageSTRING.equals("COPY")) {
            if (!this.getSelectedText().isEmpty()) {
                copySelectedText();
            }
        }
        else if (messageSTRING.equals("PASTE")) {
            pasteText();
        }
    }

    public void msgFromHandler(StyleSheetChar styleSheet) {
        this.timer.resetInterval();
        // If has selection
        if (!this.getSelectedText().isEmpty()) {
            int start = this.getSelection().getStart();
            if (start > 0 && this.getTextNoAC().charAt(start - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
                start--;
            }
            int end = this.getSelection().getEnd();
            this.cssChar = styleSheet;
            this.cssCharPrev = styleSheet;
            for (int i = start; i < end; i++) {
                this.cssStyles.set(i, styleSheet.duplicate());
            }
            this.setStyle(start, end, styleSheet.getCss());
            fixHeight();
            this.stateChanged = true;
            return;
        }

        this.cssChar = styleSheet;
        this.cssCharPrev = styleSheet;
        fixHeight();
    }

    public void msgFromHandler(StyleSheetParagraph styleSheet, Integer paragraphIndex) {
        this.timer.resetInterval();
        // If has selection
        if (!this.getSelectedText().isEmpty()) {
            int start = getParIndex(this.getSelection().getStart());
            int end = getParIndex(this.getSelection().getEnd() - 1);
            this.cssParagraph = styleSheet;
            this.cssParagraphPrev = styleSheet;
            for (int i = start; i <= end; i++) {
                this.cssParagraphStyles.set(i, styleSheet.duplicate());
                this.setParagraphStyle(i, styleSheet.getCss());
            }
            fixHeight();
            this.stateChanged = true;
            return;
        }

        if (paragraphIndex == null) {
            paragraphIndex = this.getCurrentParagraph();
        }
        this.cssParagraph = styleSheet;
        this.cssParagraphPrev = styleSheet;
        this.cssParagraphStyles.set(paragraphIndex, styleSheet.duplicate());
        this.setParagraphStyle(paragraphIndex, styleSheet.getCss());
        this.stateChanged = true;
        fixHeight();
    }
    
    public void msgFromHandler(StyleSheetParagraph styleSheet) {
        msgFromHandler(styleSheet, null);
    }
    
    public void setCssChar(StyleSheetChar css) {
        if (css == null) {
            this.cssChar = new StyleSheetChar();
        } else {
            this.cssChar = css.duplicate();
        }

        sendToHandlerCharAndParagraphCurrentStyle();
    }

    public StyleSheetChar getCssChar() {
        return this.cssChar;
    }

    public void setParagraphCss(StyleSheetParagraph cssParagraph) {
        if (cssParagraph == null) {
            this.cssParagraph = new StyleSheetParagraph();
        } else {
            this.cssParagraph = cssParagraph.duplicate();
        }
        
        this.setParagraphStyle(this.getCurrentParagraph(), cssParagraph.getCss());
        this.cssParagraphStyles.set(this.getCurrentParagraph(), cssParagraph.duplicate());
        sendToHandlerCharAndParagraphCurrentStyle();
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

    /**
     * Returns text without any formatting and without zero width space characters
     */
    public String getTextPlain() {
        return this.getTextNoAC().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");
    }

    public String getTextNoAC() {
        if (!ac.hasCurrentAC()) {
            return this.getText();
        }

        String text = this.getText().substring(0, ac.getCurrentWidgetPosition());
        if (ac.getCurrentWidgetPosition() < cssStyles.size()) {
            if (this.getText().length() > cssStyles.size()) {
                text += this.getText().substring(ac.getCurrentWidgetPosition() + ac.getCurrentAC().length());
            }
        }

        return text;
    }

    public String getParagraphTextNoAC(int paragraphIndex) {
        if (!ac.hasCurrentAC()) {
            return this.getParagraph(paragraphIndex).getText();
        }

        // String text = this.getParagraph(paragraphIndex).getText().substring(0, ac.getCurrentWidgetPosition());
        String text = this.getParagraph(paragraphIndex).getText().substring(0, offsetToPosition(ac.getCurrentWidgetPosition(), TwoDimensional.Bias.Forward).getMinor());
        if (ac.getCurrentWidgetPosition() < cssStyles.size()) {
            text += this.getParagraph(paragraphIndex).getText().substring(ac.getCurrentWidgetPosition());
        }

        return text;
    }

    /**
     * Sets plain text, if styled text is passed then it will set it as plain text
     */
    public void setTextPlain(String text) {
        ac.removeCurrentAC();
        text = RTWText.transformToTextWithZeroWidthSpace(text);

        busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        actionBeforeChanges();

        this.clear();
        this.insertText(0, text);

        this.cssStyles.clear();
        this.cssParagraphStyles.clear();
        
        for (int i = 0; i < this.getTextNoAC().length(); i++) {
            this.cssStyles.add(this.cssChar.duplicate());
        }

        this.setStyle(0, this.getTextNoAC().length(), this.cssChar.getCss());

        for (int i = 0; i < this.getParagraphs().size(); i++) {
            this.cssParagraphStyles.add(this.cssParagraph.duplicate());
            this.setParagraphStyle(i, this.cssParagraph.getCss());
        }

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            this.moveTo(0, 1);
            this.cssChar = getPredictedCssChar(this.getPosGlobal(0, 1));
            sendToHandlerCharAndParagraphCurrentStyle();
            markCharOVERWRITE(this.getCaretPosition());
            this.stateChanged = true;
            this.busy = false;
        });
    }

    /**
     * Returns RTWText object, if indexes are not specified then it will return entire text
     */
    public RTWText getRTWTextObject(Integer startIndex, Integer endIndex) {
        return new RTWText(this.getTextNoAC(), this.cssStyles, this.cssParagraphStyles, startIndex, endIndex);
    }

    /**
     * Returns RTWText object with entire styled text
     */
    public RTWText getRTWTextObject() {
        return this.getRTWTextObject(null, null);
    }

    /**
     * Sets RTWText object in widget
     */
    public void setRTWTextObject(RTWText rtwText) {
        busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        ac.removeCurrentAC();
        actionBeforeChanges();

        rtwText.setDataToRTWidget(this);

        this.requestFollowCaret();
        this.requestFocus();
        this.moveTo(0, 1);

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            this.stateChanged = true;
            this.busy = false;
        });
    }

    /**
     * Sets styled text, if plain text is passed then it will be set as plain text
     */
    public void setTextStyled(String text) {
        busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        ac.removeCurrentAC();
        actionBeforeChanges();

        if (text == null) {
            text = "";
        }

        if (RTWText.isStyledText(text)) {
            RTWText rtwText = new RTWText(text);
            rtwText.setDataToRTWidget(this);
        } else {
            this.setTextPlain(text);
        }

        this.moveTo(0, 1);

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            this.requestFollowCaret();
            this.requestFocus();
            this.stateChanged = true;
            this.busy = false;
        });
    }

    public void setupWidget() {
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setWrapText(true);
        this.getStyleClass().add("rich-text");
        this.setMinHeight(minTextWidgetHeight);

        fixSafeCharInAllParagraphs();
        sendToHandlerCharAndParagraphCurrentStyle();

        //  Selected text
        this.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                msgForHandler("SELECTED: False");
                return;
            }
            msgForHandler("SELECTED: True");
        });
                
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
            fixHeight();
        });

        // Update widget height when width is changed
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            fixHeight();
        });

        // Mouse pressed
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            handleMousePressed(event);
        });

        // On focus lost remove AutoComplete
        this.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                ac.removeCurrentAC();
            }
        });

        // Key pressed
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            this.timer.resetInterval();
            if (this.busy) {
                event.consume();
                return;
            }

            msgForHandler("CONTEXT_MENU:HIDE");

            // INSERT
            if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_INSERT();
            }
            // ENTER
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_ENTER(event);
            }
            // DELETE
            else if (event.getCode() == KeyCode.DELETE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_DELETE(event);
            }
            // BACKSPACE
            else if (event.getCode() == KeyCode.BACK_SPACE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_BACKSPACE(event);
            }
            // ARROW UP
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_UP(event);
            }
            // ARROW UP + SHIFT
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_UP_SHIFT(event);
            }
            // ARROW UP + CTRL
            else if (event.getCode() == KeyCode.UP && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_UP_CTRL(event);
            }
            // ARROW DOWN
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_DOWN(event);
            }
            // ARROW DOWN + SHIFT
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DOWN_SHIFT(event);
            }
            // ARROW DOWN + CTRL
            else if (event.getCode() == KeyCode.DOWN && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_DOWN_CTRL(event);
            }
            // ARROW LEFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_LEFT(event);
            }
            // ARROW LEFT + SHIFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_LEFT_SHIFT(event);
            }
            // ARROW RIGHT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_RIGHT(event);
            }
            // ARROW RIGHT + SHIFT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_RIGHT_SHIFT(event);
            }
            // ARROW RIGHT + CTRL
            else if (event.getCode() == KeyCode.RIGHT && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_RIGHT_CTRL(event);
            }
            // PAGE UP
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_PgUP(event);
            }
            // PAGE UP + SHIFT
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgUP_SHIFT(event);
            }
            // PAGE DOWN
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_PgDOWN(event);
            }
            // PAGE DOWN + SHIFT
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgDOWN_SHIFT(event);
            }
            // HOME
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_HOME(event);
            }
            // HOME + SHIFT
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_HOME_SHIFT(event);
            }
            // HOME + CTRL
            else if (event.getCode() == KeyCode.HOME && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_HOME_CTRL(event);
            }
            // END
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_END(event);
            }
            // END + SHIFT
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_END_SHIFT(event);
            }
            // END + CTRL
            else if (event.getCode() == KeyCode.END && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_END_CTRL(event);
            }
            // CTRL + C
            else if (event.getCode() == KeyCode.C && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                copySelectedText();
            }
            // INSERT + CTRL
            else if (event.getCode() == KeyCode.INSERT && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                copySelectedText();
            }
            // CTRL + V
            else if (event.getCode() == KeyCode.V && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                pasteText();
            }
            // INSERT + SHIFT
            else if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                pasteText();
            }
            // TAB
            else if (event.getCode() == KeyCode.TAB && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_TAB(event);
            }
            // ESC
            else if (event.getCode() == KeyCode.ESCAPE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_ESC(event);
            }
            // CTRL + A
            else if (event.getCode() == KeyCode.A && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_CTRL_A(event);
            }
            // CTRL + F
            else if (event.getCode() == KeyCode.F && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                if (this.getSelectedText().isEmpty()) {
                    msgForHandler(TextToolbarActionEnum.FIND_SHOW.name());
                } else {
                    if (this.getSelectedText().contains("\n")) {
                        this.deselect();
                        msgForHandler(TextToolbarActionEnum.FIND_SHOW.name());
                    } else {
                        String selectedText = this.getSelectedText();
                        this.deselect();
                        msgForHandler(TextToolbarActionEnum.FIND_SHOW.name() + CONSTANTS.EMPTY_PARAGRAPH_STRING + selectedText);
                    }
                }
            }
            // CTRL + H
            else if (event.getCode() == KeyCode.H && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                if (this.getSelectedText().isEmpty()) {
                    msgForHandler(TextToolbarActionEnum.REPLACE_SHOW.name());
                } else {
                    if (this.getSelectedText().contains("\n")) {
                        this.deselect();
                        msgForHandler(TextToolbarActionEnum.REPLACE_SHOW.name());
                    } else {
                        String selectedText = this.getSelectedText();
                        this.deselect();
                        msgForHandler(TextToolbarActionEnum.REPLACE_SHOW.name() + CONSTANTS.EMPTY_PARAGRAPH_STRING + selectedText);
                    }
                }
            }
            // CTRL + Z
            else if (event.getCode() == KeyCode.Z && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler(TextToolbarActionEnum.UNDO.name());
            }
            // CTRL + Y
            else if (event.getCode() == KeyCode.Y && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler(TextToolbarActionEnum.REDO.name());
            }
            // CTRL + S
            else if (event.getCode() == KeyCode.S && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler("SAVE: Ctrl+S");
            }
            // CTRL + SHIFT + S
            else if (event.getCode() == KeyCode.S && event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler("SAVE: Ctrl+Shift+S");
            }
            // ALT + ENTER
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler("SAVE: Alt+ENTER");
            }
            // CTRL + SPACE
            else if (event.getCode() == KeyCode.SPACE && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                event.consume();
                msgForHandler("CONTEXT_MENU:SHOW");
            }

            
            
            else {
                if (this.readOnly) { event.consume(); return; }
                this.busy = true;
                event.consume();
                this.busy = false;
            }
        });

        // Key typed
        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (this.readOnly) {
                event.consume();
                return;
            }

            msgForHandler("CONTEXT_MENU:HIDE");

            if (event.getCharacter().equals("")) return;
            
            if (this.busy) {
                event.consume();
                return;
            }
            this.busy = true;
            this.timer.resetInterval();

            try {
                handleKeyTyped_CHARACTER(event);
            } catch (Exception e) {
                UError.exception("handleKeyTyped_CHARACTER", e);
            }

            Platform.runLater(() -> {
                while (getTextNoAC().length() > cssStyles.size()) {
                    this.deletePreviousChar();
                }
                this.busy = false;
            });

        });
        
        
        if (!this.readOnly) {
            this.timer.play(() -> takeSnapshot());
            this.timer.resetInterval();
        } else {
            this.timer.stop();
        }

        // this.requestFocus();
    }

    public void copySelectedText() {
        if (this.getSelectedText().isEmpty()) {
            return;
        }

        busy = true;

        RTWText rtwCopiedText = RTWText.copy(this);
        OBJECTS.CLIP.setStyledText(rtwCopiedText.getStyledText());

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    public void pasteText() {
        if (!OBJECTS.CLIP.hasText()) {
            return;
        }

        busy = true;

        int pasteParagraphsCount = RTWText.countParagraphs(OBJECTS.CLIP.getStyledText()) - 1;
        int selectedTextParagraphsCount = RTWText.countParagraphs(this.getSelectedText()) - 1;
        if ((this.getParagraphs().size() - selectedTextParagraphsCount) + pasteParagraphsCount > this.maxNumberOfParagraphs) {
            msgForHandler("PARAGRAPHS_LIMIT_EXCEEDED");
            this.busy = false;
            return;
        }

        ac.removeCurrentAC();
        actionBeforeChanges();

        deleteSelectedText();

        Platform.runLater(() -> {
            if (RTWText.isStyledText(OBJECTS.CLIP.getStyledText())) {
                RTWText.paste(this, OBJECTS.CLIP.getStyledText());
            } else {
                pastePlainText(null, OBJECTS.CLIP.getStyledText());
            }

            Platform.runLater(() -> {
                this.stateChanged = true;
                fixHeight();
                this.busy = false;
            });
        });
    }

    // Private methods

    private void actionBeforeChanges() {
        msgForHandler(TextToolbarActionEnum.FIND_CLOSE.name());
    }

    /**
     * Sends request to Handler to take snapshot that can be used in Undo/Redo
     */
    private void takeSnapshot() {
        if (this.busy) {
            return;
        }
        this.busy = true;
        textHandler.msgFromWidget("TAKE_SNAPSHOT");
        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void showAC() {
        if (pauseAC != null) {
            pauseAC.playFromStart();
            return;
        }
        
        pauseAC = new PauseTransition(javafx.util.Duration.millis(ac.autoCompleteDelay));
        pauseAC.setOnFinished(e -> {
            pauseAC = null;
            Platform.runLater(() -> {
                ac.showAC(this.getCaretPosition(), getParagraphTextNoAC(this.getCurrentParagraph()));
            });
        });
        pauseAC.play();
    }

    private void fixSafeCharInAllParagraphs() {
        ignoreCaretPositionChangePERMANENT = true;
        ignoreTextChangePERMANENT = true;
        int curCaretPosition = this.getCaretPosition();

        for (int i = this.getParagraphs().size() - 1; i >= 0; i--) {
            if (getParagraphTextNoAC(i).isEmpty() || !getParagraphTextNoAC(i).startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                int positionToInsert = getPosGlobal(i, 0);
                this.cssStyles.add(positionToInsert, getPredictedCssChar(positionToInsert));
                if (curCaretPosition >= getPosGlobal(i, 0)) { curCaretPosition++; }
                this.insertText(positionToInsert, CONSTANTS.EMPTY_PARAGRAPH_STRING);
                this.setStyle(positionToInsert, positionToInsert + 1, this.cssStyles.get(positionToInsert).getCss());
            }
            boolean hasChanges = true;
            while (hasChanges) {
                hasChanges = false;
                if (getParagraphTextNoAC(i).startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(2))) {
                    int positionToRemove = getPosGlobal(i, 0);
                    this.cssStyles.remove(positionToRemove);
                    if (curCaretPosition >= getPosGlobal(i, 0)) { curCaretPosition--; }
                    this.deleteText(positionToRemove, positionToRemove + 1);
                    hasChanges = true;
                }
            }
        }
        
        int moveToPos = curCaretPosition;
        Platform.runLater(() -> {
            this.moveTo(moveToPos);
            fixCaretPosition(moveToPos);
            Platform.runLater(() -> {
                cssChar = getPredictedCssChar(moveToPos);
                cssParagraph = this.cssParagraphStyles.get(this.getParIndex(moveToPos)).duplicate();
                ignoreCaretPositionChangePERMANENT = false;
                ignoreTextChangePERMANENT = false;
                sendToHandlerCharAndParagraphCurrentStyle();
            });
        });
    }

    // Caret positions
    private int getPosGlobal(int paragraphIndex, int column) {
        return this.position(paragraphIndex, column).toOffset();
    }
    private int getParIndex(int globalPosition) {
        return this.offsetToPosition(globalPosition, TwoDimensional.Bias.Forward).getMajor();
    }
    private int getColIndex(int globalPosition) {
        return this.offsetToPosition(globalPosition, TwoDimensional.Bias.Forward).getMinor();
    }
    private void fixCaretPosition(int globalPosition) {
        if (this.getTextNoAC().length() > globalPosition && this.getTextNoAC().charAt(globalPosition) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.moveTo(globalPosition + 1);
        }
    }

    // Overwrite mode
    private void markCharOVERWRITE(int globalPosition) {
        this.requestFollowCaret();
        if (!isOverwriteMode) {
            return;
        }
        if (this.cssStyles.size() > globalPosition) {
            StyleSheetChar newStylesheet = this.cssStyles.get(globalPosition).duplicate();
            newStylesheet.setBgColor("#dfdfdf");
            newStylesheet.setFgColor("#202020");
            this.setStyle(globalPosition, globalPosition + 1, newStylesheet.getCss());
        }
    }
    private void demarkCharOVERWRITE(int globalPosition) {
        if (!isOverwriteMode) {
            return;
        }
        if (this.cssStyles.size() > globalPosition) {
            this.setStyle(globalPosition, globalPosition + 1, this.cssStyles.get(globalPosition).getCss());
        }
    }

    // Height
    public void fixHeight() {
        Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> {
                Double height = this.totalHeightEstimateProperty().getValue();
                if (height != null) {
                    height = height + 2;
                    if (height < minTextWidgetHeight) {
                        height = (double) minTextWidgetHeight;
                    }
                    this.setPrefHeight(height);
                }
            });
            pause.play();
        });
    }

    private StyleSheetChar getPredictedCssChar(Integer globalPosition) {
        // Position is at beginning of the text
        if (globalPosition == 0) {
            if (this.cssStyles.size() > 0) {
                return this.cssStyles.get(0).duplicate();
            }
            else {
                return this.cssChar.duplicate();
            }
        }

        // Position is at end of the text
        if (globalPosition == this.getTextNoAC().length()) {
            if (this.cssStyles.size() > 0) {
                return this.cssStyles.get(this.cssStyles.size() - 1).duplicate();
            }
            else {
                return this.cssChar.duplicate();
            }
        }

        // Position is in the middle of the text

        // Overwrite mode
        if (isOverwriteMode) {
            return this.cssStyles.get(globalPosition).duplicate();
        }

        // Insert mode
        return this.cssStyles.get(globalPosition - 1).duplicate();
    }

    private void msgForHandler(StyleSheetChar css) {
        if (textHandler == null) {
            return;
        }

        if (!this.getSelectedText().isEmpty()) {
            return;
        }

        textHandler.msgFromWidget(css.duplicate());
    }

    private void msgForHandler(StyleSheetParagraph cssParagraph) {
        if (textHandler == null) {
            return;
        }

        textHandler.msgFromWidget(cssParagraph.duplicate());
    }

    private void msgForHandler(String messageSTRING) {
        if (textHandler == null) {
            return;
        }

        textHandler.msgFromWidget(messageSTRING);
    }

    private boolean deleteSelectedText() {
        if (this.getSelectedText().isEmpty()) {
            return false;
        }
        int start = this.getSelection().getStart();
        int end = this.getSelection().getEnd();
        
        this.cssStyles.subList(start, end).clear();
        this.cssParagraphStyles.subList(getParIndex(start), getParIndex(end)).clear();
        
        this.deleteText(start, end);
        this.moveTo(start);
        fixSafeCharInAllParagraphs();
        return true;
    }

    public void pastePlainText(Integer globalPosition, String text, List<StyleSheetChar> cssList) {
        if (globalPosition == null) {
            globalPosition = this.getCaretPosition();
        }
        text = RTWText.transformToPlainText(text);

        StyleSheetChar cssC = getPredictedCssChar(globalPosition);
        StyleSheetParagraph cssP = this.cssParagraphStyles.get(getParIndex(globalPosition)).duplicate();

        for (int i = 0; i < text.length(); i++) {
            if (cssList != null && cssList.size() > i) {
                cssC = cssList.get(i).duplicate();
            }
            this.cssStyles.add(globalPosition + i, cssC.duplicate());
        }

        for (int i = getParIndex(globalPosition) + 1; i < getParIndex(globalPosition ) + 1 + UString.Count(text, "\n"); i++) {
            if (OBJECTS.SETTINGS.getvBOOLEAN("PreserveParagraphStyle")) {
                this.cssParagraphStyles.add(i, cssP.duplicate());
            } else {
                this.cssParagraphStyles.add(i, new StyleSheetParagraph());
            }
        }
        
        this.insertText(globalPosition, text);

        for (int i = 0; i < text.length(); i++) {
            if (cssList != null && cssList.size() > i) {
                cssC = cssList.get(i).duplicate();
            }
            this.setStyle(globalPosition + i, globalPosition + i + 1, cssC.getCss());
        }

        for (int i = getParIndex(globalPosition) + 1; i < getParIndex(globalPosition ) + 1 + UString.Count(text, "\n"); i++) {
            if (OBJECTS.SETTINGS.getvBOOLEAN("PreserveParagraphStyle")) {
                this.setParagraphStyle(i, cssP.getCss());
            } else {
                this.setParagraphStyle(i, new StyleSheetParagraph().getCss());
            }
        }

        fixSafeCharInAllParagraphs();
        this.stateChanged = true;
    }

    private void pastePlainText(Integer globalPosition, String text) {
        pastePlainText(globalPosition, text, null);
    }

    private void handleMousePressed(MouseEvent event) {
        busy = true;
        ac.removeCurrentAC();
        if (event.isPrimaryButtonDown()) {
            msgForHandler("CONTEXT_MENU:HIDE");
        }

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_INSERT() {
        this.busy = true;

        int curPos = this.getCaretPosition();

        demarkCharOVERWRITE(curPos);
        isOverwriteMode = !isOverwriteMode;
        markCharOVERWRITE(curPos);

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_ENTER(KeyEvent event) {
        // Add new paragraph
        this.busy = true;
        event.consume();

        int selectedTextParagraphsCount = RTWText.countParagraphs(this.getSelectedText()) - 1;
        if (this.getParagraphs().size() - selectedTextParagraphsCount >= this.maxNumberOfParagraphs) {
            msgForHandler("PARAGRAPHS_LIMIT_EXCEEDED");
            this.busy = false;
            return;
        }

        ac.removeCurrentAC();
        actionBeforeChanges();

        deleteSelectedText();

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        int caretPos = this.getCaretPosition();
        int curPar = this.getCurrentParagraph();
        int parCol = this.getCaretColumn();
        String parText = getParagraphTextNoAC(curPar);

        demarkCharOVERWRITE(this.getCaretPosition());

        StyleSheetParagraph prevParCss = this.cssParagraphStyles.get(curPar).duplicate();
        StyleSheetParagraph newParCss = new StyleSheetParagraph();

        // If paragraph is last one or caret is at the end of the paragraph
        if (parText.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING) || parCol == parText.length()) {
            if (OBJECTS.SETTINGS.getvBOOLEAN("PreserveParagraphStyle")) {
                this.cssParagraphStyles.add(curPar + 1, prevParCss);
                this.insertText(curPar, parCol, "\n");
                this.cssStyles.add(caretPos, cssChar.duplicate());
                this.setParagraphStyle(curPar + 1, prevParCss.getCss());
            } else {
                this.cssParagraphStyles.add(curPar + 1, newParCss);
                this.insertText(curPar, parCol, "\n");
                this.cssStyles.add(caretPos, cssChar.duplicate());
                this.setParagraphStyle(curPar + 1, newParCss.getCss());
            }

            Platform.runLater(() -> {
                fixSafeCharInAllParagraphs();
                // fix will release permanent ignore
                Platform.runLater(() -> {
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
            });
            return;
        }
        
        // if caret is at the beginning of the paragraph
        if (parCol == 0 || parCol == 1) {
            this.cssParagraphStyles.add(curPar, newParCss);
            this.insertText(curPar, parCol, "\n");
            this.cssStyles.add(caretPos, cssChar.duplicate());
            this.setParagraphStyle(curPar, newParCss.getCss());
            this.setParagraphStyle(curPar + 1, prevParCss.getCss());

            Platform.runLater(() -> {
                fixSafeCharInAllParagraphs();
                // fix will release permanent ignore
                Platform.runLater(() -> {
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
            });
            return;
        }

        // If caret is in the middle of the paragraph
        this.cssParagraphStyles.add(curPar + 1, prevParCss);
        this.insertText(curPar, parCol, "\n");
        this.cssStyles.add(caretPos, cssChar.duplicate());
        this.setParagraphStyle(curPar + 1, prevParCss.getCss());

        Platform.runLater(() -> {
            fixSafeCharInAllParagraphs();
            // fix will release permanent ignore
            Platform.runLater(() -> {
                markCharOVERWRITE(this.getCaretPosition());
                this.stateChanged = true;
                this.busy = false;
            });
        });
        return;
    }

    private void handleKeyPressed_DELETE(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        actionBeforeChanges();

        if (!this.getSelectedText().isEmpty()) {
            if (event != null) event.consume();
            deleteSelectedText();
            Platform.runLater(() -> {
                showAC();
                this.stateChanged = true;
                this.busy = false;
            });
            return;
        }

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        
        if (event != null) event.consume();

        int caretPos = this.getCaretPosition();
        int curPar = this.getCurrentParagraph();
        int parTextLen = getParagraphTextNoAC(curPar).length();

        demarkCharOVERWRITE(caretPos);

        // If caret is at the end of the text then do nothing
        if (this.getCaretPosition() == this.cssStyles.size()) {
            Platform.runLater(() -> {
                showAC();
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                this.busy = false;
            });
            return;
        }
        
        // Check if deleted char is "\n"
        if (this.getTextNoAC().charAt(caretPos) == '\n') {
            // If paragraph is empty then remove it
            if (parTextLen == 1) {
                this.cssParagraphStyles.remove(curPar);
                this.cssStyles.remove(caretPos - 1);
                this.cssStyles.remove(caretPos - 1);
                this.deleteText(caretPos - 1, caretPos + 1);

                Platform.runLater(() -> {
                    showAC();
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(caretPos);
                    this.cssChar = getPredictedCssChar(caretPos - 1);
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
                return;
            }
            // If paragraph is not empty then remove "\n" and next paragraph zero width space
            else {
                this.cssParagraphStyles.remove(curPar + 1);
                this.cssStyles.remove(caretPos);
                this.cssStyles.remove(caretPos);
                this.deleteText(caretPos, caretPos + 2);

                Platform.runLater(() -> {
                    showAC();
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(caretPos);
                    this.cssChar = getPredictedCssChar(caretPos);
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
                return;
            }
        }

        // If deleted char is not "\n"

        this.cssStyles.remove(caretPos);
        this.deleteText(caretPos, caretPos + 1);

        Platform.runLater(() -> {
            showAC();
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            this.cssChar = getPredictedCssChar(caretPos);
            sendToHandlerCharAndParagraphCurrentStyle();
            markCharOVERWRITE(this.getCaretPosition());
            this.stateChanged = true;
            this.busy = false;
        });
    }

    private void handleKeyPressed_BACKSPACE(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        actionBeforeChanges();

        if (!this.getSelectedText().isEmpty()) {
            event.consume();
            deleteSelectedText();
            Platform.runLater(() -> {
                showAC();
                this.stateChanged = true;
                this.busy = false;
            });
            return;
        }

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        
        event.consume();

        int caretPos = this.getCaretPosition();
        int curPar = this.getCurrentParagraph();
        int prevParTextLen = -1;

        demarkCharOVERWRITE(caretPos);

        if (curPar > 0) {
            prevParTextLen = getParagraphTextNoAC(curPar - 1).length();
        }

        // If caret is at the beginning of the text then do nothing
        if (caretPos == 0 || caretPos == 1) {
            Platform.runLater(() -> {
                showAC();
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                this.busy = false;
            });
            return;
        }
        
        // Check if deleted char is zero width space
        if (this.getTextNoAC().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            // If previous paragraph is empty then remove it
            if (prevParTextLen == 1) {
                this.cssParagraphStyles.remove(curPar - 1);
                this.cssStyles.remove(caretPos - 3);
                this.cssStyles.remove(caretPos - 3);
                this.deleteText(caretPos - 3, caretPos - 1);
                this.moveTo(caretPos - 2);

                Platform.runLater(() -> {
                    showAC();
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(this.getCaretPosition());
                    this.cssChar = getPredictedCssChar(this.getCaretPosition());
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
                return;
            }
            // If previous paragraph is not empty then remove "\n" and current paragraph zero width space
            else {
                this.cssParagraphStyles.remove(curPar);
                this.cssStyles.remove(caretPos - 2);
                this.cssStyles.remove(caretPos - 2);
                this.deleteText(caretPos - 2, caretPos);
                this.moveTo(caretPos - 2);

                Platform.runLater(() -> {
                    showAC();
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(this.getCaretPosition());
                    this.cssChar = getPredictedCssChar(this.getCaretPosition());
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
                    this.stateChanged = true;
                    this.busy = false;
                });
                return;
            }
        }

        // If deleted char is not zero width space

        this.cssStyles.remove(caretPos - 1);
        this.deleteText(caretPos - 1, caretPos);
        this.moveTo(caretPos - 1);

        Platform.runLater(() -> {
            showAC();
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            fixCaretPosition(this.getCaretPosition());
            this.cssChar = getPredictedCssChar(this.getCaretPosition());
            sendToHandlerCharAndParagraphCurrentStyle();
            markCharOVERWRITE(this.getCaretPosition());
            this.stateChanged = true;
            this.busy = false;
        });
    }

    private void handleKeyPressed_UP(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();

        int caretPos = this.getCaretPosition();

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
            if (caretPos == this.getCaretPosition()) {
                if (this.getCurrentParagraph() > 0) {
                    this.moveTo(this.getCurrentParagraph() - 1, 1);
                }
            }
        });
    }

    private void handleKeyPressed_UP_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            this.selectRange(anchorPos, this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_UP_CTRL(KeyEvent event) {
        this.busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        event.consume();

        ac.prevAC();

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_DOWN(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_DOWN_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            this.selectRange(anchorPos, this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_DOWN_CTRL(KeyEvent event) {
        this.busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        event.consume();

        ac.nextAC();

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_LEFT(KeyEvent event) {
        this.busy = true;
        event.consume();
        
        int caretPos = this.getCaretPosition();

        if (caretPos == 0 || caretPos == 1) {
            this.busy = false;
            return;
        }

        ac.removeCurrentAC();

        demarkCharOVERWRITE(caretPos);
        
        if (this.getTextNoAC().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.moveTo(caretPos - 2);
        } else {
            this.moveTo(caretPos - 1);
        }

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_LEFT_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();
        
        int caretPos = this.getCaretPosition();
        int anchorPos = this.getAnchor();

        if (caretPos == 0 || caretPos == 1) {
            this.busy = false;
            return;
        }

        demarkCharOVERWRITE(caretPos);
        
        if (this.getTextNoAC().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.moveTo(caretPos - 2);
        } else {
            this.moveTo(caretPos - 1);
        }

        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_RIGHT(KeyEvent event) {
        this.busy = true;
        event.consume();
        
        int caretPos = this.getCaretPosition();

        if (caretPos == this.cssStyles.size()) {
            this.busy = false;
            return;
        }
        ac.removeCurrentAC();

        demarkCharOVERWRITE(caretPos);
        if (this.getTextNoAC().charAt(caretPos) == '\n') {
            this.moveTo(caretPos + 2);
        } else {
            this.moveTo(caretPos + 1);
        }

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
        
    }

    private void handleKeyPressed_RIGHT_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();
        
        int caretPos = this.getCaretPosition();
        int anchorPos = this.getAnchor();

        if (caretPos == this.cssStyles.size()) {
            this.busy = false;
            return;
        }

        demarkCharOVERWRITE(caretPos);
        if (this.getTextNoAC().charAt(caretPos) == '\n') {
            this.moveTo(caretPos + 2);
        } else {
            this.moveTo(caretPos + 1);
        }

        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_RIGHT_CTRL(KeyEvent event) {
        if (!ac.hasCurrentAC()) return;

        this.busy = true;
        event.consume();
        actionBeforeChanges();

        String acString = ac.getCurrentACClean();
        ac.removeCurrentAC();

        String strToAdd = null;

        if (acString.indexOf(" ", 1) > 0) {
            strToAdd = acString.substring(0, acString.indexOf(" ", 1) + 1);
        } else {
            strToAdd = acString;
        }

        pastePlainText(null, strToAdd);
        Platform.runLater(() -> {
            showAC();
            stateChanged = true;
            this.busy = false;
        });
    }

    private void handleKeyPressed_TAB(KeyEvent event) {
        event.consume();
        if (!ac.hasCurrentAC()) return;

        this.busy = true;
        actionBeforeChanges();

        String acString = ac.getCurrentACClean();
        ac.removeCurrentAC();

        pastePlainText(null, acString);
        Platform.runLater(() -> {
            showAC();
            stateChanged = true;
            this.busy = false;
        });
    }

    private void handleKeyPressed_ESC(KeyEvent event) {
        if (!ac.hasCurrentAC()) return;

        this.busy = true;
        event.consume();
        ac.removeCurrentAC();

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_PgUP(KeyEvent event) {
        this.busy = true;
        event.consume();

        if (this.getCurrentParagraph() == 0) {
            this.busy = false;
            return;
        }
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (getParagraphTextNoAC(i).length() == 1) {
                start = true;
            }
            if (start && getParagraphTextNoAC(i).length() > 1) {
                ignoreCaretPositionChange = true;
                this.moveTo(i, 1);
                this.cssChar = getPredictedCssChar(this.getPosGlobal(i, 1));
                this.cssParagraph = this.cssParagraphStyles.get(i).duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();

                Platform.runLater(() -> {
                    markCharOVERWRITE(this.getCaretPosition());
                    this.busy = false;
                });
                return;
            }
        }

        ignoreCaretPositionChange = true;
        this.moveTo(0, 1);
        this.cssChar = getPredictedCssChar(this.getPosGlobal(0, 1));
        this.cssParagraph = this.cssParagraphStyles.get(0).duplicate();
        sendToHandlerCharAndParagraphCurrentStyle();
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });

    }

    private void handleKeyPressed_PgUP_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        if (this.getCurrentParagraph() == 0) {
            this.busy = false;
            return;
        }

        int anchorPos = this.getAnchor();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (getParagraphTextNoAC(i).length() == 1) {
                start = true;
            }
            if (start && getParagraphTextNoAC(i).length() > 1) {
                this.moveTo(i, 1);
                int curPar = i;
                Platform.runLater(() -> {
                    this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
                    this.busy = false;
                });
                return;
            }
        }

        this.moveTo(0, 1);
        
        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getPosGlobal(0, 1));
            this.busy = false;
        });
    }

    private void handleKeyPressed_PgDOWN(KeyEvent event) {
        this.busy = true;
        event.consume();

        if (this.getCurrentParagraph() == this.getParagraphs().size() - 1) {
            this.busy = false;
            return;
        }

        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < this.getParagraphs().size(); i++) {
            if (getParagraphTextNoAC(i).length() == 1) {
                start = true;
            }
            if (start && getParagraphTextNoAC(i).length() > 1) {
                ignoreCaretPositionChange = true;
                this.moveTo(i, 1);
                this.cssChar = getPredictedCssChar(this.getPosGlobal(i, 1));
                this.cssParagraph = this.cssParagraphStyles.get(i).duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();

                Platform.runLater(() -> {
                    markCharOVERWRITE(this.getCaretPosition());
                    this.busy = false;
                });
                return;
            }
        }

        ignoreCaretPositionChange = true;
        this.moveTo(this.getParagraphs().size() - 1, 1);
        this.cssChar = getPredictedCssChar(this.getPosGlobal(this.getParagraphs().size() - 1, 1));
        this.cssParagraph = this.cssParagraphStyles.get(this.getParagraphs().size() - 1).duplicate();
        sendToHandlerCharAndParagraphCurrentStyle();
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_PgDOWN_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        if (this.getCurrentParagraph() == this.getParagraphs().size() - 1) {
            this.busy = false;
            return;
        }

        int anchorPos = this.getAnchor();
        
        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < this.getParagraphs().size(); i++) {
            if (getParagraphTextNoAC(i).length() == 1) {
                start = true;
            }
            if (start && getParagraphTextNoAC(i).length() > 1) {
                this.moveTo(i, 1);
                int curPar = i;

                Platform.runLater(() -> {
                    this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
                    this.busy = false;
                });
                return;
            }
        }

        this.moveTo(this.getParagraphs().size() - 1, 1);
        
        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getPosGlobal(this.getParagraphs().size() - 1, 1));
            this.busy = false;
        });
    }

    private void handleKeyPressed_HOME(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();

        this.moveTo(curPar, 1);
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_HOME_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();
        int anchorPos = this.getAnchor();

        this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
        
        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_HOME_CTRL(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.moveTo(0, 1);
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_END(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();

        this.moveTo(curPar, getParagraphTextNoAC(curPar).length());
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_END_SHIFT(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();
        int anchorPos = this.getAnchor();

        this.selectRange(anchorPos, this.getPosGlobal(curPar, getParagraphTextNoAC(curPar).length()));
        
        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_END_CTRL(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.moveTo(this.getParagraphs().size() - 1, getParagraphTextNoAC(this.getParagraphs().size() - 1).length());
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_CTRL_A(KeyEvent event) {
        this.busy = true;
        ac.removeCurrentAC();
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.selectRange(1, this.getTextNoAC().length());
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyTyped_CHARACTER(KeyEvent event) {
        if (event.getCharacter().isEmpty() || 
            event.getCharacter().equals("\n") || 
            event.getCharacter().equals("\t") ||
            event.getCharacter().equals("\r") ||
            event.isControlDown() ||
            event.isAltDown()) {

                event.consume();
                // this.busy = false;
                return;
        }

        ac.removeCurrentAC();
        actionBeforeChanges();

        if (this.getSelectedText().length() > 0) {
            int start = this.getSelection().getStart();
            int end = this.getSelection().getEnd();
            
            this.cssStyles.subList(start, end).clear();
            this.cssParagraphStyles.subList(getParIndex(start), getParIndex(end)).clear();
            
            this.deleteText(start, end);
            this.moveTo(start);
        }

        event.consume();
        
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        String charTyped = event.getCharacter();

        int curPos = this.getCaretPosition();
        if (curPos <= this.cssStyles.size() && curPos > 0 && !this.cssStyles.get(curPos - 1).equals(this.cssChar) && this.getTextNoAC().charAt(curPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.cssStyles.set(curPos - 1, this.cssChar.duplicate());
            this.setStyle(curPos - 1, curPos, this.cssChar.getCss());
        }

        demarkCharOVERWRITE(curPos);

        // Caret is at the end of the text
        if (curPos == this.cssStyles.size()) {
            this.insertText(curPos, charTyped);
            this.cssStyles.add(curPos, cssChar.duplicate());
            this.setStyle(curPos, curPos + 1, cssChar.getCss());
            Platform.runLater(() -> {
                showAC();
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                sendToHandlerCharAndParagraphCurrentStyle();
                this.stateChanged = true;
                // this.busy = false;
            });
            return;
        }

        // Caret is at end of the paragraph
        if (curPos < this.getTextNoAC().length() && this.getTextNoAC().charAt(curPos) == '\n') {
            this.insertText(curPos, charTyped);
            this.cssStyles.add(curPos, cssChar.duplicate());
            this.setStyle(curPos, curPos + 1, cssChar.getCss());
            Platform.runLater(() -> {
                showAC();
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                sendToHandlerCharAndParagraphCurrentStyle();
                this.stateChanged = true;
                // this.busy = false;
            });
            return;
        }

        // Caret is in the middle of the text
        if (isOverwriteMode) {
            this.deleteText(curPos, curPos + 1);
            this.cssStyles.remove(curPos);
        }
        this.insertText(curPos, charTyped);
        this.cssStyles.add(curPos, cssChar.duplicate());
        this.setStyle(curPos, curPos + 1, cssChar.getCss());
        Platform.runLater(() -> {
            showAC();
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            markCharOVERWRITE(this.getCaretPosition());
            this.cssChar = getPredictedCssChar(this.getCaretPosition());
            sendToHandlerCharAndParagraphCurrentStyle();
            this.stateChanged = true;
            // this.busy = false;
        });
    }

    private void handleTextChange(Object obs, String oldText, String newText) {
        if (this.ignoreTextChange || this.ignoreTextChangePERMANENT) {
            this.ignoreTextChange = false;
            return;
        }

        // Nothing to implement yet
    }

    private void handleCaretPositionChange(Object obs, Integer oldPos, Integer newPos) {
        if (this.ignoreCaretPositionChange || this.ignoreCaretPositionChangePERMANENT) {
            this.ignoreCaretPositionChange = false;
            return;
        }

        this.busy = true;

        demarkCharOVERWRITE(oldPos);
        markCharOVERWRITE(newPos);

        this.cssChar = getPredictedCssChar(newPos);
        this.cssParagraph = this.cssParagraphStyles.get(this.getParIndex(newPos)).duplicate();

        sendToHandlerCharAndParagraphCurrentStyle();

        Platform.runLater(() -> {
            this.busy = false;
        });

    }

    private void sendToHandlerCharAndParagraphCurrentStyle() {
        // Send current char style to Handler if it has changed
        if (this.cssCharPrev == null || !this.cssCharPrev.equals(this.cssChar)) {
            msgForHandler(this.cssChar);
            this.cssCharPrev = this.cssChar.duplicate();
        }
        
        // Send current paragraph style to Handler if it has changed
        if (this.cssParagraphPrev == null || !this.cssParagraphPrev.equals(this.cssParagraph)) {
            msgForHandler(this.cssParagraph);
            this.cssParagraphPrev = this.cssParagraph.duplicate();
        }
        
    }


}
