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
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.services.timer.ContinuousTimer;
import com.dsoftn.utils.UString;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private StyleSheetChar cssChar = null; // Current char style
    private StyleSheetChar cssCharPrev = null; // Previous char style that has been sent to handler
    private StyleSheetParagraph cssParagraph = new StyleSheetParagraph(); // Current paragraph style
    private StyleSheetParagraph cssParagraphPrev = null; // Previous paragraph style that has been sent to handler
    private Integer maxNumberOfParagraphs = null;
    private Integer maxTotalChars = null;
    private Integer maxCharsPerParagraph = null;
    private int minTextWidgetHeight = 24;
    public List<StyleSheetChar> cssStyles = new ArrayList<>();
    public List<StyleSheetParagraph> cssParagraphStyles = new ArrayList<>();
    private TextHandler textHandler = null;
    private boolean ignoreTextChange = false;
    public boolean ignoreCaretPositionChange = false;
    public boolean ignoreTextChangePERMANENT = false;
    private boolean ignoreCaretPositionChangePERMANENT = false;
    public boolean busy = false;
    boolean stateChanged = true; // Used with Undo/Redo
    ContinuousTimer timer = new ContinuousTimer(OBJECTS.SETTINGS.getvINTEGER("IntervalBetweenTextChangesMS"));

    public boolean isOverwriteMode = false;
   
    // Constructors
    public RTWidget(StyleSheetChar cssStyleSheet, Integer maxNumberOfParagraphs, Integer maxTotalChars, Integer maxCharsPerParagraph) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );
    
        this.cssChar = cssStyleSheet;
        this.maxNumberOfParagraphs = maxNumberOfParagraphs;
        this.maxTotalChars = maxTotalChars;
        this.maxCharsPerParagraph = maxCharsPerParagraph;

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
        this.timer.resetInterval();
        // If has selection
        if (!this.getSelectedText().isEmpty()) {
            int start = this.getSelection().getStart();
            if (start > 0 && this.getText().charAt(start - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
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
            return;
        }

        if (paragraphIndex == null) {
            paragraphIndex = this.getCurrentParagraph();
        }
        this.cssParagraph = styleSheet;
        this.cssParagraphPrev = styleSheet;
        this.cssParagraphStyles.set(paragraphIndex, styleSheet.duplicate());
        this.setParagraphStyle(paragraphIndex, styleSheet.getCss());
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

    public void setParagraphCss(StyleSheetParagraph css) {
        if (css == null) {
            this.cssParagraph = new StyleSheetParagraph();
        } else {
            this.cssParagraph = css.duplicate();
        }
        
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
        return this.getText().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");
    }

    /**
     * Sets plain text, if styled text is passed then it will set it as plain text
     */
    public void setTextPlain(String text) {
        text = RTWText.transformToTextWithZeroWidthSpace(text);

        busy = true;
        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        this.clear();
        this.insertText(0, text);

        this.cssStyles.clear();
        this.cssParagraphStyles.clear();
        
        for (int i = 0; i < this.getText().length(); i++) {
            this.cssStyles.add(this.cssChar.duplicate());
        }

        this.setStyle(0, this.getText().length(), this.cssChar.getCss());

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
            this.busy = false;
        });
    }

    /**
     * Returns RTWText object, if indexes are not specified then it will return entire text
     */
    public RTWText getRTWTextObject(Integer startIndex, Integer endIndex) {
        return new RTWText(this.getText(), this.cssStyles, this.cssParagraphStyles, startIndex, endIndex);
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

        rtwText.setDataToRTWidget(this);

        this.requestFollowCaret();
        this.requestFocus();
        this.moveTo(0, 1);

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
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

        // Mouse pressed
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            handleMousePressed(event);
        });

        
        // Key pressed
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            this.timer.resetInterval();
            if (this.busy) {
                System.out.println("Busy: KEY PRESSED");
                event.consume();
                return;
            }

            // INSERT
            if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_INSERT();
            }
            // ENTER
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_ENTER(event);
            }
            // DELETE
            else if (event.getCode() == KeyCode.DELETE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DELETE(event);
            }
            // BACKSPACE
            else if (event.getCode() == KeyCode.BACK_SPACE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_BACKSPACE(event);
            }
            // ARROW UP
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_UP(event);
            }
            // ARROW UP + SHIFT
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_UP_SHIFT(event);
            }
            // ARROW DOWN
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DOWN(event);
            }
            // ARROW DOWN + SHIFT
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DOWN_SHIFT(event);
            }
            // ARROW LEFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_LEFT(event);
            }
            // ARROW LEFT + SHIFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_LEFT_SHIFT(event);
            }
            // ARROW RIGHT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_RIGHT(event);
            }
            // ARROW RIGHT + SHIFT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_RIGHT_SHIFT(event);
            }
            // PAGE UP
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgUP(event);
            }
            // PAGE UP + SHIFT
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgUP_SHIFT(event);
            }
            // PAGE DOWN
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgDOWN(event);
            }
            // PAGE DOWN + SHIFT
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_PgDOWN_SHIFT(event);
            }
            // HOME
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_HOME(event);
            }
            // HOME + SHIFT
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_HOME_SHIFT(event);
            }
            // HOME + CTRL
            else if (event.getCode() == KeyCode.HOME && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_HOME_CTRL(event);
            }
            // END
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_END(event);
            }
            // END + SHIFT
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_END_SHIFT(event);
            }
            // END + CTRL
            else if (event.getCode() == KeyCode.END && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
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
                event.consume();
                pasteText();
            }
            // INSERT + SHIFT
            else if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                pasteText();
            }
            // CTRL + A
            else if (event.getCode() == KeyCode.A && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                this.selectAll();
            }
            
            
            else {
                System.out.println("Unknown key pressed: " + event.getCode());
                event.consume();
            }


            Platform.runLater(() -> {
                HELPER_info("KEY PRESSED - AFTER");
            });

        });

        // Key typed
        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            this.timer.resetInterval();
            if (this.busy) {
                System.out.println("Busy: KEY TYPED");
                event.consume();
                return;
            }

            handleKeyTyped_CHARACTER(event);
        });
        
        
        this.timer.play(() -> takeSnapshot());

        this.requestFocus();
    }

    public void copySelectedText() {
        if (this.getSelectedText().isEmpty()) {
            return;
        }

        busy = true;

        RTWText rtwCopiedText = RTWText.copy(this);
        OBJECTS.CLIP.setClipText(rtwCopiedText.getStyledText());

        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    public void pasteText() {
        if (!OBJECTS.CLIP.hasText()) {
            return;
        }

        busy = true;

        deleteSelectedText();

        Platform.runLater(() -> {
            if (RTWText.isStyledText(OBJECTS.CLIP.getClipText())) {
                RTWText.paste(this, OBJECTS.CLIP.getClipText());
            } else {
                pastePlainText(null, OBJECTS.CLIP.getClipText());
            }

            Platform.runLater(() -> {
                this.busy = false;
            });
        });
    }


    // Private methods

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

    private void HELPER_info(String msg) {
        System.out.println("--- " + msg + " ---");
        System.out.println("Text LENGTH: " + this.getText().length() + "  cssChar LENGTH: " + this.cssStyles.size() + "  cssParagraph LENGTH: " + this.cssParagraphStyles.size());
        System.out.println("Caret POSITION: " + this.getCaretPosition() + "  Paragraph: " + this.getCurrentParagraph() + "  Column: " + this.getCaretColumn());
        System.out.println("Selected LENGTH: " + this.getSelectedText().length() + "  Selected: From: " + this.getSelection().getStart() + " to " + this.getSelection().getEnd() + "  Selected TEXT: " + this.getSelectedText().replace("\n", "_"));
        System.out.println("-".repeat(msg.length() + 8));
    }

    private void fixSafeCharInAllParagraphs() {
        ignoreCaretPositionChangePERMANENT = true;
        ignoreTextChangePERMANENT = true;
        int curCaretPosition = this.getCaretPosition();

        for (int i = this.getParagraphs().size() - 1; i >= 0; i--) {
            if (this.getParagraph(i).getText().isEmpty() || !this.getParagraph(i).getText().startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                int positionToInsert = getPosGlobal(i, 0);
                this.cssStyles.add(positionToInsert, getPredictedCssChar(positionToInsert));
                if (curCaretPosition >= getPosGlobal(i, 0)) { curCaretPosition++; }
                this.insertText(positionToInsert, CONSTANTS.EMPTY_PARAGRAPH_STRING);
                this.setStyle(positionToInsert, positionToInsert + 1, this.cssStyles.get(positionToInsert).getCss());
            }
            boolean hasChanges = true;
            while (hasChanges) {
                hasChanges = false;
                if (this.getParagraph(i).getText().startsWith(CONSTANTS.EMPTY_PARAGRAPH_STRING.repeat(2))) {
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
    private int getParCol(int globalPosition) {
        return this.offsetToPosition(globalPosition, TwoDimensional.Bias.Forward).getMinor();
    }
    private void fixCaretPosition(int globalPosition) {
        if (this.getText().length() > globalPosition && this.getText().charAt(globalPosition) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
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
    private void fixHeight() {
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
        if (globalPosition == this.getText().length()) {
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
    }

    private void pastePlainText(Integer globalPosition, String text) {
        pastePlainText(globalPosition, text, null);
    }

    private void handleMousePressed(MouseEvent event) {
        busy = true;

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

        deleteSelectedText();

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        int caretPos = this.getCaretPosition();
        int curPar = this.getCurrentParagraph();
        int parCol = this.getCaretColumn();
        String parText = this.getParagraph(curPar).getText();

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
                this.busy = false;
            });
        });
        return;
    }

    private void handleKeyPressed_DELETE(KeyEvent event) {
        this.busy = true;

        if (!this.getSelectedText().isEmpty()) {
            event.consume();
            deleteSelectedText();
            Platform.runLater(() -> {
                this.busy = false;
            });
            return;
        }

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;
        
        event.consume();

        int caretPos = this.getCaretPosition();
        int curPar = this.getCurrentParagraph();
        int parTextLen = this.getParagraph(curPar).getText().length();

        demarkCharOVERWRITE(caretPos);

        // If caret is at the end of the text then do nothing
        if (this.getCaretPosition() == this.cssStyles.size()) {
            Platform.runLater(() -> {
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                this.busy = false;
            });
            return;
        }
        
        // Check if deleted char is "\n"
        if (this.getText().charAt(caretPos) == '\n') {
            // If paragraph is empty then remove it
            if (parTextLen == 1) {
                this.cssParagraphStyles.remove(curPar);
                this.cssStyles.remove(caretPos - 1);
                this.cssStyles.remove(caretPos - 1);
                this.deleteText(caretPos - 1, caretPos + 1);

                Platform.runLater(() -> {
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(caretPos);
                    this.cssChar = getPredictedCssChar(caretPos - 1);
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
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
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(caretPos);
                    this.cssChar = getPredictedCssChar(caretPos);
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
                    this.busy = false;
                });
                return;
            }
        }

        // If deleted char is not "\n"

        this.cssStyles.remove(caretPos);
        this.deleteText(caretPos, caretPos + 1);

        Platform.runLater(() -> {
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            this.cssChar = getPredictedCssChar(caretPos);
            sendToHandlerCharAndParagraphCurrentStyle();
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_BACKSPACE(KeyEvent event) {
        this.busy = true;

        if (!this.getSelectedText().isEmpty()) {
            event.consume();
            deleteSelectedText();
            Platform.runLater(() -> {
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
            prevParTextLen = this.getParagraph(curPar - 1).getText().length();
        }

        // If caret is at the beginning of the text then do nothing
        if (caretPos == 0 || caretPos == 1) {
            Platform.runLater(() -> {
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                this.busy = false;
            });
            return;
        }
        
        // Check if deleted char is zero width space
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            // If previous paragraph is empty then remove it
            if (prevParTextLen == 1) {
                this.cssParagraphStyles.remove(curPar - 1);
                this.cssStyles.remove(caretPos - 3);
                this.cssStyles.remove(caretPos - 3);
                this.deleteText(caretPos - 3, caretPos - 1);
                this.moveTo(caretPos - 2);

                Platform.runLater(() -> {
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(this.getCaretPosition());
                    this.cssChar = getPredictedCssChar(this.getCaretPosition());
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
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
                    ignoreTextChangePERMANENT = false;
                    ignoreCaretPositionChangePERMANENT = false;
                    fixCaretPosition(this.getCaretPosition());
                    this.cssChar = getPredictedCssChar(this.getCaretPosition());
                    sendToHandlerCharAndParagraphCurrentStyle();
                    markCharOVERWRITE(this.getCaretPosition());
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
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            fixCaretPosition(this.getCaretPosition());
            this.cssChar = getPredictedCssChar(this.getCaretPosition());
            sendToHandlerCharAndParagraphCurrentStyle();
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_UP(KeyEvent event) {
        this.busy = true;

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_UP_SHIFT(KeyEvent event) {
        this.busy = true;

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            this.selectRange(anchorPos, this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_DOWN(KeyEvent event) {
        this.busy = true;

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_DOWN_SHIFT(KeyEvent event) {
        this.busy = true;

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            fixCaretPosition(this.getCaretPosition());
            this.selectRange(anchorPos, this.getCaretPosition());
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

        demarkCharOVERWRITE(caretPos);
        
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
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
        event.consume();
        
        int caretPos = this.getCaretPosition();
        int anchorPos = this.getAnchor();

        if (caretPos == 0 || caretPos == 1) {
            this.busy = false;
            return;
        }

        demarkCharOVERWRITE(caretPos);
        
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
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

        demarkCharOVERWRITE(caretPos);
        if (this.getText().charAt(caretPos) == '\n') {
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
        event.consume();
        
        int caretPos = this.getCaretPosition();
        int anchorPos = this.getAnchor();

        if (caretPos == this.cssStyles.size()) {
            this.busy = false;
            return;
        }

        demarkCharOVERWRITE(caretPos);
        if (this.getText().charAt(caretPos) == '\n') {
            this.moveTo(caretPos + 2);
        } else {
            this.moveTo(caretPos + 1);
        }

        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getCaretPosition());
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

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (this.getParagraph(i).getText().length() == 1) {
                start = true;
            }
            if (start && this.getParagraph(i).getText().length() > 1) {
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
        event.consume();

        if (this.getCurrentParagraph() == 0) {
            this.busy = false;
            return;
        }

        int anchorPos = this.getAnchor();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (this.getParagraph(i).getText().length() == 1) {
                start = true;
            }
            if (start && this.getParagraph(i).getText().length() > 1) {
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

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < this.getParagraphs().size(); i++) {
            if (this.getParagraph(i).getText().length() == 1) {
                start = true;
            }
            if (start && this.getParagraph(i).getText().length() > 1) {
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
        event.consume();

        if (this.getCurrentParagraph() == this.getParagraphs().size() - 1) {
            this.busy = false;
            return;
        }

        int anchorPos = this.getAnchor();
        
        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < this.getParagraphs().size(); i++) {
            if (this.getParagraph(i).getText().length() == 1) {
                start = true;
            }
            if (start && this.getParagraph(i).getText().length() > 1) {
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
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();

        this.moveTo(curPar, this.getParagraph(curPar).getText().length());
        
        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            this.busy = false;
        });
    }

    private void handleKeyPressed_END_SHIFT(KeyEvent event) {
        this.busy = true;
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();
        int anchorPos = this.getAnchor();

        this.selectRange(anchorPos, this.getPosGlobal(curPar, this.getParagraph(curPar).getText().length()));
        
        Platform.runLater(() -> {
            this.busy = false;
        });
    }

    private void handleKeyPressed_END_CTRL(KeyEvent event) {
        this.busy = true;
        event.consume();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.moveTo(this.getParagraphs().size() - 1, this.getParagraph(this.getParagraphs().size() - 1).getText().length());
        
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
            event.isAltDown())
            { return; }

        this.busy = true;

        deleteSelectedText();

        ignoreTextChangePERMANENT = true;
        ignoreCaretPositionChangePERMANENT = true;

        event.consume();
        String charTyped = event.getCharacter();

        int curPos = this.getCaretPosition();
        if (curPos <= this.cssStyles.size() && curPos > 0 && !this.cssStyles.get(curPos - 1).equals(this.cssChar) && this.getText().charAt(curPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
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
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                sendToHandlerCharAndParagraphCurrentStyle();
                this.busy = false;
            });
            return;
        }

        // Caret is at end of the paragraph
        if (curPos < this.getText().length() && this.getText().charAt(curPos) == '\n') {
            this.insertText(curPos, charTyped);
            this.cssStyles.add(curPos, cssChar.duplicate());
            this.setStyle(curPos, curPos + 1, cssChar.getCss());
            Platform.runLater(() -> {
                ignoreTextChangePERMANENT = false;
                ignoreCaretPositionChangePERMANENT = false;
                markCharOVERWRITE(this.getCaretPosition());
                sendToHandlerCharAndParagraphCurrentStyle();
                this.busy = false;
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
            ignoreTextChangePERMANENT = false;
            ignoreCaretPositionChangePERMANENT = false;
            markCharOVERWRITE(this.getCaretPosition());
            this.cssChar = getPredictedCssChar(this.getCaretPosition());
            sendToHandlerCharAndParagraphCurrentStyle();
            this.busy = false;
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
        // System.out.println("caret position changed. old: " + oldPos + "  new: " + newPos);
        if (this.ignoreCaretPositionChange || this.ignoreCaretPositionChangePERMANENT) {
            this.ignoreCaretPositionChange = false;
            return;
        }

        this.busy = true;
        System.out.println("caret position changed. old: " + oldPos + "  new: " + newPos);

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
