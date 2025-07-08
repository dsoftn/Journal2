package com.dsoftn.services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.fxmisc.richtext.Caret.CaretVisibility;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.TwoDimensional;

import com.dsoftn.CONSTANTS;
import com.dsoftn.OBJECTS;
import com.dsoftn.enums.controllers.TextToolbarActionEnum;
import com.dsoftn.services.text_handler.ACHandler;
import com.dsoftn.services.text_handler.RTWidgetAction;
import com.dsoftn.services.text_handler.RTWidgetAction.ActionType;
import com.dsoftn.services.text_handler.StyleSheetChar;
import com.dsoftn.services.text_handler.StyleSheetParagraph;
import com.dsoftn.services.text_handler.StyledParagraph;
import com.dsoftn.services.text_handler.TextHandler;
import com.dsoftn.services.timer.ContinuousTimer;
import com.dsoftn.utils.UError;
import com.dsoftn.utils.USettings;
import com.dsoftn.utils.UString;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class RTWidget extends StyledTextArea<String, String> {
    // Variables
    private StyleSheetChar cssChar = null; // Current char style
    private StyleSheetParagraph cssParagraph = new StyleSheetParagraph(); // Current paragraph style
    private Integer maxNumberOfParagraphs = Integer.MAX_VALUE;
    private boolean readOnly = false;
    private int minTextWidgetHeight = 24;
    private List<StyledParagraph> styledParagraphs = new ArrayList<>();
    private List<RTWidgetAction> actionsStack = new ArrayList<>();
    private boolean busy = false;
    private boolean stateChanged = true; // Used with Undo/Redo
    private int curCaretPos = 0; // Current caret position
    private boolean hasSelection = false;
    private boolean ignoreCaretPositionChangePERMANENT = false;

    private TextHandler textHandler = null;
    private ACHandler ac = null;

    private ContinuousTimer timerBetweenTextChanges = new ContinuousTimer(OBJECTS.SETTINGS.getvINTEGER("IntervalBetweenTextChangesMS"));
    private PauseTransition pauseAC = null; // Used to delay showing AutoComplete

    private ScrollPane scrollPane = null;
    private VBox scrollPaneContainer = null;
    private VBox scrollPaneContent = null;
    private double extraRightPadding = 0; // Used to add padding to the right when vertical scrollbar is visible

    private boolean isOverwriteMode = false;
   
    // Constructors
    public RTWidget(VBox scrollPaneContainer, StyleSheetChar cssStyleSheet, Integer maxNumberOfParagraphs) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        setupScrollPane(scrollPaneContainer);
    
        setPadding(null, null, null, null);

        this.cssChar = cssStyleSheet;
        
        if (maxNumberOfParagraphs == null) {
            maxNumberOfParagraphs = Integer.MAX_VALUE;
        } else if (maxNumberOfParagraphs < 1) {
            maxNumberOfParagraphs = Integer.MAX_VALUE;
        } else {
            this.maxNumberOfParagraphs = maxNumberOfParagraphs;
        }

        setInitialParagraph();
    }
    
    public RTWidget(VBox scrollPaneContainer, StyleSheetChar cssStyleSheet) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        setupScrollPane(scrollPaneContainer);

        setPadding(null, null, null, null);

        this.cssChar = cssStyleSheet;

        setInitialParagraph();
    }

    public RTWidget(VBox scrollPaneContainer) {
        super(
            "",  // initialText
            (text, style) -> text.setStyle(style), // Apply style to characters
            "",  // initialParagraphStyle
            (paragraph, style) -> paragraph.setStyle(style), // Apply style to paragraphs
            false
        );

        setupScrollPane(scrollPaneContainer);

        setPadding(null, null, null, null);

        this.cssChar = new StyleSheetChar();

        setInitialParagraph();
    }

    // Public methods
    
    public void setPadding(Double top, Double right, Double bottom, Double left, TextHandler.Behavior behavior) {
        if (behavior == null) {
            if (textHandler == null) return;
            behavior = textHandler.getBehavior();
        }

        if (top == null) top = (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingTop", behavior, null, 5).getValueINT();
        if (right == null) right = (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingRight", behavior, null, 5).getValueINT() + extraRightPadding;
        if (bottom == null) bottom = (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingBottom", behavior, null, 5).getValueINT();
        if (left == null) left = (double) USettings.getAppOrUserSettingsItem("RTWidgetPaddingLeft", behavior, null, 5).getValueINT();
        this.setPadding(new javafx.geometry.Insets(top, right, bottom, left));
        fixHeight();
    }

    public void setPadding(Double top, Double right, Double bottom, Double left) {
        setPadding(top, right, bottom, left, null);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            this.setShowCaret(CaretVisibility.OFF);
            timerBetweenTextChanges.stop();
        } else {
            this.setShowCaret(CaretVisibility.ON);
            timerBetweenTextChanges.play(() -> takeSnapshot());
        }
        
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setMaxNumberOfParagraphs(Integer maxNumberOfParagraphs) {
        if (maxNumberOfParagraphs == null || maxNumberOfParagraphs < 1) {
            maxNumberOfParagraphs = Integer.MAX_VALUE;
        }

        this.maxNumberOfParagraphs = maxNumberOfParagraphs;
    }

    public boolean canBeClosed() {
        if (ac.hasCurrentAC()) {
            ac.removeCurrentAC();
            return false;
        }

        if (this.busy) {
            return false;
        }

        if (actionsStack.size() > 0) {
            executeActions(actionsStack);
        }

        return true;
    }

    public void msgFromHandler(String messageSTRING) {
        // Process information from handler
        if (messageSTRING.equals(ActionType.CUT.name())) {
            actionsStack.add(new RTWidgetAction(ActionType.CUT));
        }
        else if (messageSTRING.equals("COPY")) {
            actionsStack.add(new RTWidgetAction(ActionType.COPY));
        }
        else if (messageSTRING.equals("PASTE")) {
            actionsStack.add(new RTWidgetAction(ActionType.PASTE));
        }
        else if (messageSTRING.equals("SELECT_ALL")) {
            actionsStack.add(new RTWidgetAction(ActionType.SELECT_ALL));
        }

        if (actionsStack.size() > 0) {
            executeActions(actionsStack);
        }
    }

    public void msgFromHandler(StyleSheetChar styleSheet) {
        busy = true;
        styleSheet = styleSheet.duplicate();
        this.cssChar = styleSheet;
        this.timerBetweenTextChanges.resetInterval();

        // If has selection
        if (!this.getSelectedText().isEmpty()) {
            applyStringStyle(this.getSelection().getStart(), this.getSelection().getEnd(), styleSheet);
            this.stateChanged = true;
        } else {
            if (styledParagraphs.get(getParIndex(curCaretPos)).getPlainText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
                styledParagraphs.get(getParIndex(curCaretPos)).getStyledStringList().get(0).setCssCharStyleObject(this.cssChar);
                writeParagraph(getParIndex(curCaretPos));
            }
        }

        fixHeight();
        markCharOVERWRITE(curCaretPos);
        busy = false;
    }

    public void msgFromHandler(StyleSheetParagraph styleSheet, Integer paragraphIndex) {
        busy = true;
        styleSheet = styleSheet.duplicate();
        this.cssParagraph = styleSheet;
        this.timerBetweenTextChanges.resetInterval();
        
        // If has selection
        if (!this.getSelectedText().isEmpty()) {
            applyParagraphStyle(this.getSelection().getStart(), this.getSelection().getEnd(), styleSheet);
            this.stateChanged = true;
            fixHeight();
            markCharOVERWRITE(curCaretPos);
            busy = false;
            return;
        } else {
            if (paragraphIndex != null) {
                moveTo(paragraphIndex, 1);
                curCaretPos = this.getCaretPosition();
            }
            applyParagraphStyle(curCaretPos, curCaretPos, styleSheet);
        }

        this.stateChanged = true;
        fixHeight();
        markCharOVERWRITE(curCaretPos);
        busy = false;
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

        int currentPar = getParIndex(curCaretPos);
        if (styledParagraphs.get(currentPar).getPlainText().equals(CONSTANTS.EMPTY_PARAGRAPH_STRING)) {
            styledParagraphs.get(currentPar).getStyledStringList().get(0).setCssCharStyleObject(this.cssChar);
            writeParagraph(currentPar);
        }

        // if (!this.getSelectedText().isEmpty()) {
        //     applyStringStyle(this.getSelection().getStart(), this.getSelection().getEnd(), this.cssChar);
        //     this.stateChanged = true;
        // }

        sendToHandlerCharAndParagraphCurrentStyle();
    }

    public StyleSheetChar getCssChar() {
        return this.cssChar;
    }

    public StyleSheetParagraph getCssParagraph() {
        return this.cssParagraph;
    }

    public void setParagraphCss(StyleSheetParagraph cssParagraph) {
        busy = true;

        if (cssParagraph == null) {
            this.cssParagraph = new StyleSheetParagraph();
        } else {
            this.cssParagraph = cssParagraph.duplicate();
        }
        
        applyParagraphStyle(curCaretPos, curCaretPos, cssParagraph);
        this.stateChanged = true;
        busy = false;

        sendToHandlerCharAndParagraphCurrentStyle();
    }

    public StyleSheetParagraph getParagraphCss() {
        return this.cssParagraph;
    }

    public void setMinTextWidgetHeight(int minTextWidgetHeight) {
        this.minTextWidgetHeight = minTextWidgetHeight;
        this.setMinHeight(minTextWidgetHeight + this.getPadding().getTop() + this.getPadding().getBottom());
    }

    public void setTextHandler(TextHandler textHandler) {
        this.textHandler = textHandler;
        this.setPadding(null, null, null, null);
    }

    /**
     * Returns text without any formatting and without zero width space characters
     */
    public String getTextPlain() {
        ac.removeCurrentAC();
        return this.getText().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "");
    }

    public List<StyledParagraph> getStyledParagraphs() {
        return styledParagraphs;
    }

    /**
     * Sets plain text, if styled text is passed then it will set it as plain text
     */
    public void setTextPlain(String text) {
        busy = true;

        if (ac != null) {
            ac.removeCurrentAC();
        }

        text = RTWText.transformToPlainText(text);

        closeFindReplace();

        this.clear();
        styledParagraphs.clear();

        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            StyledParagraph paragraph = new StyledParagraph(this.cssParagraph, this.cssChar);
            paragraph.insertPlainText(0, line, this.cssChar);
            styledParagraphs.add(paragraph);
            writeParagraph(styledParagraphs.size() - 1);
            if (i != lines.length - 1) {
                this.insertText(this.getCaretPosition(), "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
            }
        }

        if (styledParagraphs.size() == 0) {
            styledParagraphs.add(new StyledParagraph(this.cssParagraph, this.cssChar));
        }

        setInitialParagraph();

        this.moveTo(0, 1);
        curCaretPos = 1;

        markCharOVERWRITE(curCaretPos);
        this.stateChanged = true;
        fixHeight();
        this.setFocused(true);
        busy = false;
    }

    /**
     * Returns RTWText object, if indexes are not specified then it will return entire text
     */
    public RTWText getRTWTextObject(Integer globalStartPosition, Integer globalEndPosition) {
        return new RTWText(styledParagraphs, globalStartPosition, globalEndPosition);
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

        if (ac != null) {
            ac.removeCurrentAC();
        }

        closeFindReplace();

        styledParagraphs.clear();
        this.clear();
        styledParagraphs = rtwText.getStyledParagraphs();
        for (int i = 0; i < styledParagraphs.size(); i++) {
            writeParagraph(i);
            if (i != styledParagraphs.size() - 1) {
                this.insertText(this.getCaretPosition(), "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
            }
        }

        setInitialParagraph();

        this.moveTo(0, 1);
        curCaretPos = 1;

        this.stateChanged = true;
        fixHeight();
        this.setFocused(true);
        this.busy = false;
    }

    /**
     * Sets styled text, if plain text is passed then it will be set as plain text
     */
    public void setTextStyled(String text) {
        busy = true;

        if (text == null) {
            text = "";
        }

        if (RTWText.isStyledText(text)) {
            RTWText rtwText = new RTWText(text);
            setRTWTextObject(rtwText);
        } else {
            setTextPlain(text);
        }

        moveTo(0, 1);
        curCaretPos = 1;
        this.stateChanged = true;

        fixHeight();
        this.setFocused(true);
        busy = false;
    }

    public void setupWidget() {
        this.setMaxWidth(Double.MAX_VALUE);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setWrapText(true);
        this.getStyleClass().add("rich-text");
        this.setMinHeight(minTextWidgetHeight + this.getPadding().getTop() + this.getPadding().getBottom());

        sendToHandlerCharAndParagraphCurrentStyle();

        //  Selected text
        this.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                if (this.hasSelection) {
                    this.hasSelection = false;
                    msgForHandler("SELECTED: False");
                }
                return;
            }
            if (!this.hasSelection) {
                this.hasSelection = true;
                msgForHandler("SELECTED: True");
            }
            cssChar = styledParagraphs.get(this.getParIndex(this.getSelection().getStart())).getCssCharStyleObject(getParCol(this.getSelection().getStart())).duplicate();
            cssParagraph = styledParagraphs.get(this.getParIndex(this.getSelection().getStart())).getCssParagraphStyleObject().duplicate();
            sendToHandlerCharAndParagraphCurrentStyle();
        });
                
        // Text change
        // this.textProperty().addListener((obs, oldText, newText) -> {
        //     handleTextChange(obs, oldText, newText);
        // });

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
            this.timerBetweenTextChanges.resetInterval();

            msgForHandler("CONTEXT_MENU:HIDE");

            // INSERT
            if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.INSERT_PRESSED));
                executeActions(actionsStack);
            }
            // ENTER
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.ENTER_PRESSED));
                executeActions(actionsStack);
            }
            // DELETE
            else if (event.getCode() == KeyCode.DELETE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.DELETE));
                executeActions(actionsStack);
            }
            // BACKSPACE
            else if (event.getCode() == KeyCode.BACK_SPACE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.BACKSPACE_PRESSED));
                executeActions(actionsStack);
            }
            // ARROW UP
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_UP();
            }
            // ARROW UP + SHIFT
            else if (event.getCode() == KeyCode.UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_UP_SHIFT();
            }
            // ARROW UP + CTRL
            else if (event.getCode() == KeyCode.UP && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_UP_CTRL();
            }
            // ARROW DOWN
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_DOWN();
            }
            // ARROW DOWN + SHIFT
            else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                handleKeyPressed_DOWN_SHIFT();
            }
            // ARROW DOWN + CTRL
            else if (event.getCode() == KeyCode.DOWN && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                if (this.readOnly) { event.consume(); return; }
                handleKeyPressed_DOWN_CTRL();
            }
            // ARROW LEFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) {return; }
                handleKeyPressed_LEFT();
            }
            // ARROW LEFT + SHIFT
            else if (event.getCode() == KeyCode.LEFT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_LEFT_SHIFT();
            }
            // ARROW RIGHT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_RIGHT();
            }
            // ARROW RIGHT + SHIFT
            else if (event.getCode() == KeyCode.RIGHT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_RIGHT_SHIFT();
            }
            // ARROW RIGHT + CTRL
            else if (event.getCode() == KeyCode.RIGHT && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_RIGHT_CTRL();
            }
            // PAGE UP
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_PgUP();
            }
            // PAGE UP + SHIFT
            else if (event.getCode() == KeyCode.PAGE_UP && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_PgUP_SHIFT();
            }
            // PAGE DOWN
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_PgDOWN();
            }
            // PAGE DOWN + SHIFT
            else if (event.getCode() == KeyCode.PAGE_DOWN && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_PgDOWN_SHIFT();
            }
            // HOME
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_HOME();
            }
            // HOME + SHIFT
            else if (event.getCode() == KeyCode.HOME && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_HOME_SHIFT();
            }
            // HOME + CTRL
            else if (event.getCode() == KeyCode.HOME && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_HOME_CTRL();
            }
            // END
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_END();
            }
            // END + SHIFT
            else if (event.getCode() == KeyCode.END && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                handleKeyPressed_END_SHIFT();
            }
            // END + CTRL
            else if (event.getCode() == KeyCode.END && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_END_CTRL();
            }
            // CTRL + C
            else if (event.getCode() == KeyCode.C && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.COPY));
                executeActions(actionsStack);
            }
            // INSERT + CTRL
            else if (event.getCode() == KeyCode.INSERT && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.COPY));
                executeActions(actionsStack);
            }
            // CTRL + V
            else if (event.getCode() == KeyCode.V && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.PASTE));
                executeActions(actionsStack);
            }
            // INSERT + SHIFT
            else if (event.getCode() == KeyCode.INSERT && !event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.PASTE));
                executeActions(actionsStack);
            }
            // TAB
            else if (event.getCode() == KeyCode.TAB && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_TAB();
            }
            // ESC
            else if (event.getCode() == KeyCode.ESCAPE && !event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                handleKeyPressed_ESC();
            }
            // CTRL + A
            else if (event.getCode() == KeyCode.A && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.SELECT_ALL));
                executeActions(actionsStack);
            }
            // CTRL + F
            else if (event.getCode() == KeyCode.F && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
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
                event.consume();
                if (this.readOnly) { return; }
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
                event.consume();
                if (this.readOnly) { return; }
                msgForHandler(TextToolbarActionEnum.UNDO.name());
            }
            // CTRL + Y
            else if (event.getCode() == KeyCode.Y && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                msgForHandler(TextToolbarActionEnum.REDO.name());
            }
            // CTRL + S
            else if (event.getCode() == KeyCode.S && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                msgForHandler("SAVE: Ctrl+S");
            }
            // CTRL + SHIFT + S
            else if (event.getCode() == KeyCode.S && event.isControlDown() && event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                msgForHandler("SAVE: Ctrl+Shift+S");
            }
            // ALT + ENTER
            else if (event.getCode() == KeyCode.ENTER && !event.isControlDown() && !event.isShiftDown() && event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                msgForHandler("SAVE: Alt+ENTER");
            }
            // CTRL + SPACE
            else if (event.getCode() == KeyCode.SPACE && event.isControlDown() && !event.isShiftDown() && !event.isAltDown()) {
                event.consume();
                if (this.readOnly) { return; }
                showAC();
            }

            
            
            else {
                event.consume();
                if (this.readOnly) { return; }
            }
        });

        // Key typed
        this.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            event.consume();
            actionsStack.add(new RTWidgetAction(RTWidgetAction.ActionType.TYPED_CHAR, event));
            executeActions(actionsStack);
        });
        
        
        if (!this.readOnly) {
            this.timerBetweenTextChanges.play(() -> takeSnapshot());
            this.timerBetweenTextChanges.resetInterval();
        } else {
            this.timerBetweenTextChanges.stop();
        }

        // this.requestFocus();
    }

    public void copySelectedText() {
        if (this.getSelectedText().isEmpty()) {
            return;
        }

        RTWText rtwCopiedText = RTWText.copy(this);
        OBJECTS.CLIP.setStyledText(rtwCopiedText.getStyledText());
    }

    public void pasteText(Integer globalPosition, String text) {
        if (globalPosition == null) {
            globalPosition = curCaretPos;
        }

        curCaretPos = globalPosition;
        if (curCaretPos < this.getText().length() && this.getText().charAt(curCaretPos) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            curCaretPos++;
        }
        this.moveTo(curCaretPos);

        int pasteParagraphsCount = RTWText.countParagraphs(text);
        int selectedTextParagraphsCount = RTWText.countParagraphs(this.getSelectedText()) - 1;
        if ((styledParagraphs.size() - selectedTextParagraphsCount) + (pasteParagraphsCount - 1) > this.maxNumberOfParagraphs) {
            msgForHandler("PARAGRAPHS_LIMIT_EXCEEDED");
            return;
        }

        ac.removeCurrentAC();
        closeFindReplace();

        deleteSelectedText();

        RTWText rtwText = new RTWText();
        if (!RTWText.isStyledText(text)) {
            rtwText.setDefaultCharCss(this.cssChar.duplicate());
            rtwText.setDefaultParagraphCss(this.cssParagraph.duplicate());
        };
        rtwText.loadStyledOrPlainText(text);

        moveTo(curCaretPos);
        int curParIndex = getParagraphsIndexes(curCaretPos, curCaretPos).get(0);
        int curParCol = getParCol(curCaretPos, curParIndex);

        ignoreCaretPositionChangePERMANENT = true;

        // Case when there is only one paragraph text
        if (pasteParagraphsCount == 1) {
            StyledParagraph newPar = styledParagraphs.get(curParIndex).splitOnIndex(curParCol);
            StyledParagraph oldPar = styledParagraphs.get(curParIndex);
            oldPar.insertStyledStrings(curParCol, rtwText.getStyledParagraphs().get(0).getStyledStringList());
            oldPar.insertStyledStrings(curParCol, newPar.getStyledStringList());
            this.insertText(curCaretPos, rtwText.getStyledParagraphs().get(0).getPlainText().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, ""));
            this.moveTo(curCaretPos + rtwText.getStyledParagraphs().get(0).getPlainText().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "").length());
            curCaretPos = this.getCaretPosition();
            ensureGoodCaretPosition();
            this.stateChanged = true;
            fixHeight();
            ignoreCaretPositionChangePERMANENT = false;
            refreshWidget();
            return;
        }

        if (pasteParagraphsCount < 1) {
            UError.error("RTWidget.pasteText: Paste text contains less than one paragraph.", "Error");
            return;
        }

        // Case when there is multiple paragraphs text
        StyledParagraph newPar = styledParagraphs.get(curParIndex).splitOnIndex(curParCol);
        StyledParagraph oldPar = styledParagraphs.get(curParIndex);
        oldPar.insertStyledStrings(curParCol, rtwText.getStyledParagraphs().get(0).getStyledStringList());
        logHelper("After paste");
        this.insertText(curCaretPos, rtwText.getStyledParagraphs().get(0).getPlainText().replace(CONSTANTS.EMPTY_PARAGRAPH_STRING, "") + "\n");
        logHelper("After paste");
        for (int i = 1; i < rtwText.getStyledParagraphs().size(); i++) {
            styledParagraphs.add(curParIndex + i, rtwText.getStyledParagraphs().get(i));
            this.insertText(this.getCaretPosition(), rtwText.getStyledParagraphs().get(i).getPlainText());
            if (i == pasteParagraphsCount - 1) {
                // Add remaining text from split paragraph to last pasted paragraph
                styledParagraphs.get(curParIndex + i).insertStyledStrings(0, newPar.getStyledStringList());
            }
            // this.moveTo(curCaretPos + rtwText.getStyledParagraphs().get(i - 1).getPlainText().length());
            if (i != pasteParagraphsCount - 1) {
                this.insertText(this.getCaretPosition(), "\n");
            }
            logHelper("After paste");
            // writeParagraph(curParIndex + i);
        }

        logHelper("After paste");

        this.moveTo(curCaretPos + rtwText.getPlainText().length() + rtwText.countParagraphs() - 1);
        curCaretPos = this.getCaretPosition();
        ensureGoodCaretPosition();
        this.stateChanged = true;
        fixHeight();
        ignoreCaretPositionChangePERMANENT = false;
        refreshWidget();
    }

    public void refreshWidget() {
        for (int i = 0; i < styledParagraphs.size(); i++) {
            writeParagraph(i);
        }
        fixHeight();
    }

    public void pasteTextFromClipboard() {
        if (!OBJECTS.CLIP.hasText()) {
            return;
        }

        pasteText(null, OBJECTS.CLIP.getStyledText());
    }

    public void addToContainerVBox(Integer index) {
        if (scrollPaneContainer == null) {
            return;
        }
        if (scrollPane == null) {
            return;
        }
        if (index == null) {
            scrollPaneContainer.getChildren().add(this.scrollPane);
            return;
        }
        scrollPaneContainer.getChildren().add(index, this.scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    ScrollBar vBar = findVerticalScrollBar(scrollPane);
                    if (vBar != null) {
                        vBar.visibleProperty().addListener((vObs, wasVisible, isVisible) -> {
                            if (isVisible) {
                                extraRightPadding = 16;
                                this.setPadding(null, null, null, null);
                            } else {
                                extraRightPadding = 0;
                                this.setPadding(null, null, null, null);
                            }
                        });

                        if (vBar.isVisible()) {
                            extraRightPadding = 16;
                            this.setPadding(null, null, null, null);
                        }
                    }
                });
            }
        });        
    }

    public void removeFromContainerVBox() {
        if (scrollPaneContainer == null) {
            return;
        }
        if (scrollPane == null) {
            return;
        }
        scrollPaneContainer.getChildren().remove(this.scrollPane);
    }

    public void updateStyledStrings(int startGlobalPos, int endGlobalPos, String updateWithCss) {
        List<Integer> paragraphsIndexes = getParagraphsIndexes(startGlobalPos, endGlobalPos);
        for (int i = 0; i <= paragraphsIndexes.size(); i++) {
            styledParagraphs.get(i).updateCss(getParCol(startGlobalPos), getParCol(endGlobalPos), updateWithCss);
        }
    }

    public String getParagraphText(int paragraphIndex) {
        return styledParagraphs.get(paragraphIndex).getPlainText();
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean getBusy() {
        return this.busy;
    }

    public void setACHandler(ACHandler ac) {
        this.ac = ac;
    }

    public ACHandler getAC() {
        return this.ac;
    }

    public TextHandler getTextHandler() {
        return this.textHandler;
    }

    public boolean isStateChanged() {
        return this.stateChanged;
    }

    public void setStateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }

    public void refreshParagraph(Integer paragraphIndex) {
        if (paragraphIndex == null) {
            for (int i = 0; i < styledParagraphs.size(); i++) {
                writeParagraph(i);
            }
        } else {
            writeParagraph(paragraphIndex);
        }
        
        this.stateChanged = true;
        fixHeight();
        Platform.runLater(() -> {
            trackCursorInScrollPane();
            curCaretPos = this.getCaretPosition();
        });
    }

    public void replaceFoundItem(int startGlobalPos, int endGlobalPos, String replaceWith) {
        int parIndex = getParagraphsIndexes(startGlobalPos, endGlobalPos).get(0);
        int parCol = getParCol(startGlobalPos);
        int parColEnd = getParCol(endGlobalPos);
        this.styledParagraphs.get(parIndex).delete(parCol, parColEnd);
        this.styledParagraphs.get(parIndex).insertPlainText(parCol, replaceWith, this.cssChar);
        this.writeParagraph(parIndex);
        this.stateChanged = true;
        fixHeight();
        Platform.runLater(() -> {
            trackCursorInScrollPane();
            curCaretPos = this.getCaretPosition();
        });
    }

    public void setCurrentCaretPos(int curCaretPos) {
        this.curCaretPos = curCaretPos;
    }

    // Private methods

    private void setInitialParagraph() {
        if (styledParagraphs.size() == 0) {
            styledParagraphs.add(new StyledParagraph(this.cssParagraph, this.cssChar));
            writeParagraph(0);
            this.moveTo(0, 1);
            curCaretPos = 1;
            this.requestFocus();
        }
    }

    private void applyStringStyle(int startGlobalPos, int endGlobalPos, StyleSheetChar styleSheet) {
        List<Integer> paragraphsIndexes = getParagraphsIndexes(startGlobalPos, endGlobalPos);
        int caretPos = curCaretPos;

        for (Integer parIndex : paragraphsIndexes) {
            styledParagraphs.get(parIndex).updateCss(getParCol(startGlobalPos, parIndex), getParCol(endGlobalPos, parIndex), styleSheet.getCss());
            writeParagraph(parIndex);
        }
        this.moveTo(caretPos);
        curCaretPos = this.getCaretPosition();
    }

    private void writeParagraph(int styledParagraphsIndex) {
        if (styledParagraphsIndex >= this.styledParagraphs.size()) {
            return;
        }
        StyledParagraph paragraph = this.styledParagraphs.get(styledParagraphsIndex);

        // this.moveTo(styledParagraphsIndex, 0);
        ignoreCaretPositionChangePERMANENT = true;
        this.moveTo(styledParagraphsIndex, 0);
        curCaretPos = this.getCaretPosition();
        int length = this.getParagraphLength(styledParagraphsIndex);

        this.replaceText(curCaretPos, curCaretPos + length, paragraph.getPlainText());
        int pos = curCaretPos;
        for (int i = 0; i < paragraph.getStyledStringList().size(); i++) {
            this.setStyle(pos, pos + paragraph.getStyledStringList().get(i).getText().length(), paragraph.getStyledStringList().get(i).getCssCharStyle());
            pos += paragraph.getStyledStringList().get(i).getText().length();
        }

        ignoreCaretPositionChangePERMANENT = false;
        this.moveTo(styledParagraphsIndex, paragraph.getPlainText().length());
        curCaretPos = this.getCaretPosition();

        this.setParagraphStyle(styledParagraphsIndex, paragraph.getCssParagraphStyle());
        msgForHandler("QUICK_MARK_PARAGRAPH_NUMBER:" + styledParagraphsIndex);
    }

    private void applyParagraphStyle(int startGlobalPos, int endGlobalPos, StyleSheetParagraph styleSheet) {
        List<Integer> paragraphsAffected = getParagraphsIndexes(startGlobalPos, endGlobalPos);
        for (int i = 0; i < paragraphsAffected.size(); i++) {
            this.styledParagraphs.get(paragraphsAffected.get(i)).setCssParagraphStyleObject(styleSheet);
            this.setParagraphStyle(paragraphsAffected.get(i), this.styledParagraphs.get(paragraphsAffected.get(i)).getCssParagraphStyle());
        }
    }

    private Integer getParCol(Integer globalPosition) {
        if (globalPosition == null) {
            return null;
        }
        return this.offsetToPosition(globalPosition, TwoDimensional.Bias.Forward).getMinor();
    }

    private Integer getParCol(Integer globalPosition, int paragraphIndex) {
        if (getParIndex(globalPosition) != paragraphIndex) {
            return null;
        }
        return getParCol(globalPosition);
    }

    private List<Integer> getParagraphsIndexes(int startGlobalPos, int endGlobalPos) {
        List<Integer> indexes = new ArrayList<>();
        int startParIndex = getParIndex(startGlobalPos);
        int endParIndex = getParIndex(endGlobalPos - 1);
        if (endParIndex < startParIndex) {
            endParIndex = startParIndex;
        }
        for (int i = startParIndex; i <= endParIndex; i++) {
            indexes.add(i);
        }
        return indexes;
    }

    private void executeActions(List<RTWidgetAction> actions) {
        busy = true;
        while (actions.size() > 0) {
            RTWidgetAction action = actions.remove(0);
            switch (action.getActionType()) {
                case TYPED_CHAR:
                    processKeyEvent(action.getKeyEvent());
                    break;
                case PASTE:
                    actionPASTE();
                    break;
                case DELETE:
                    handleKeyPressed_DELETE();
                    break;
                case COPY:
                    actionCOPY();
                    break;
                case CUT:
                    actionCUT();
                    break;
                case SELECT_ALL:
                    actionSELECT_ALL();
                    break;
                case UNDO:
                    // TODO: Implement
                    break;
                case REDO:
                    // TODO: Implement
                    break;
                case SET_FUTURE_CHAR_STYLE:
                    // TODO: Implement
                    break;
                case FORMAT_STRING_SELECTION:
                    // TODO: Implement
                    break;
                case FORMAT_PARAGRAPH_SELECTION:
                    // TODO: Implement
                    break;
                case INSERT_PRESSED:
                    handleKeyPressed_INSERT();
                    break;
                case ENTER_PRESSED:
                    handleKeyPressed_ENTER();
                    break;
                case BACKSPACE_PRESSED:
                    handleKeyPressed_BACKSPACE();
                    break;
            }
        }
        busy = false;
    }

    private ScrollBar findVerticalScrollBar(ScrollPane scrollPane) {
        for (Node node : scrollPane.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    private void setupScrollPane(VBox scrollPaneContainer) {
        if (scrollPaneContainer == null) {
            return;
        }

        this.scrollPaneContainer = scrollPaneContainer;
        this.scrollPane = new ScrollPane();
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setFitToHeight(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.scrollPane.setPannable(true);
        // this.scrollPane.setHvalue(1.0);
        // this.scrollPane.setVvalue(1.0);
        this.scrollPane.setMinHeight(16);
        this.scrollPane.setMinWidth(20);
        // set calculated height
        this.scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.scrollPane.setPrefWidth(0);
        this.scrollPane.setMaxHeight(Double.MAX_VALUE);
        this.scrollPane.setMaxWidth(Double.MAX_VALUE);
        // this.scrollPaneContent = new VBox();
        // this.scrollPaneContent.setMinHeight(0);
        // this.scrollPaneContent.setMinWidth(0);
        // this.scrollPaneContent.setPrefHeight(0);
        // this.scrollPaneContent.setPrefWidth(0);
        // this.scrollPaneContent.setMaxHeight(Double.MAX_VALUE);
        // this.scrollPaneContent.setMaxWidth(Double.MAX_VALUE);
        // this.scrollPaneContent.getChildren().add(this);
        this.scrollPane.setContent(this);

        addToContainerVBox(null);
    }

    private void actionCUT() {
        if (!this.getSelectedText().isEmpty()) {
            copySelectedText();
            handleKeyPressed_DELETE();
        }
    }

    private void actionCOPY() {
        if (!this.getSelectedText().isEmpty()) {
            copySelectedText();
        }
    }

    private void actionPASTE() {
        pasteTextFromClipboard();
    }


    

    private void processKeyEvent(KeyEvent event) {
            if (this.readOnly) {
                return;
            }

            // msgForHandler("CONTEXT_MENU:HIDE");
            if (event.getCharacter().length() == 0 || 
                event.getCharacter().equals("\n") || 
                event.getCharacter().equals("\t") ||
                event.getCharacter().equals("\r") ||
                event.getCharacter().charAt(0) < 32 ||
                event.getCharacter().charAt(0) > 126 ||
                event.isControlDown() ||
                event.isAltDown() ||
                event.getCharacter().equals("")) {
                return;
            }

            this.timerBetweenTextChanges.resetInterval();

            try {
                handleKeyTyped_CHARACTER(event);
            } catch (Exception e) {
                UError.exception("handleKeyTyped_CHARACTER", e);
            }
    }

    private void closeFindReplace() {
        msgForHandler(TextToolbarActionEnum.FIND_CLOSE.name());
    }

    /**
     * Sends request to Handler to take snapshot that can be used in Undo/Redo
     */
    private void takeSnapshot() {
        if (this.busy) {
            return;
        }
        if (!stateChanged) {
            return;
        }

        textHandler.msgFromWidget("TAKE_SNAPSHOT");
        Platform.runLater(() -> {
            stateChanged = false;
        });
    }

    private void showAC() {
        if (ac == null) return;

        if (pauseAC != null) {
            pauseAC.playFromStart();
            return;
        }
        
        pauseAC = new PauseTransition(javafx.util.Duration.millis(ac.getAutoCompleteDelay()));
        pauseAC.setOnFinished(e -> {
            pauseAC = null;
            Platform.runLater(() -> {
                ac.showAC(curCaretPos, styledParagraphs.get(this.getCurrentParagraph()).getPlainText());
                moveTo(curCaretPos);
                if (!ac.hasCurrentAC()) {
                    textHandler.markText(null);
                }
            });
        });
        pauseAC.play();
    }

    // Caret positions
    private int getPosGlobal(int paragraphIndex, int column) {
        return this.position(paragraphIndex, column).toOffset();
    }
    public int getParIndex(int globalPosition) {
        if (globalPosition < 0) {
            return 0;
        }
        return this.offsetToPosition(globalPosition, TwoDimensional.Bias.Forward).getMajor();
    }
    private void ensureGoodCaretPosition() {
        int parIndex = getParIndex(curCaretPos);
        Integer parCol = getParCol(curCaretPos, parIndex);
        if (parCol == null || parCol == 0) {
            moveTo(parIndex, 1);
            curCaretPos = this.getCaretPosition();
        }
    }
    
    // Overwrite mode
    private void markCharOVERWRITE(int globalPosition) {
        this.requestFollowCaret();
        if (!isOverwriteMode) {
            return;
        }
        if (this.getText().length() > globalPosition) {
            StyleSheetChar newStylesheet = styledParagraphs.get(getParIndex(globalPosition)).getPredictedCssChar(getParCol(globalPosition), isOverwriteMode);
            newStylesheet.setBgColor("#dfdfdf");
            newStylesheet.setFgColor("#202020");
            this.setStyle(globalPosition, globalPosition + 1, newStylesheet.getCss());
        }
    }
    private void demarkCharOVERWRITE(int globalPosition) {
        if (!isOverwriteMode) {
            return;
        }
        if (this.getText().length() > globalPosition) {
            this.setStyle(globalPosition, globalPosition + 1, styledParagraphs.get(getParIndex(globalPosition)).getPredictedCssChar(getParCol(globalPosition), isOverwriteMode).getCss());
        }
    }

    // Height
    public void fixHeight() {
        // Platform.runLater(() -> {
            PauseTransition pause = new PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> {
                Double height = this.totalHeightEstimateProperty().getValue();
                if (height != null) {
                    height = height + 2 + this.getPadding().getTop() + this.getPadding().getBottom();
                    if (height < minTextWidgetHeight + this.getPadding().getTop() + this.getPadding().getBottom()) {
                        height = (double) minTextWidgetHeight + this.getPadding().getTop() + this.getPadding().getBottom();
                    }
                    this.setPrefHeight(height);
                    this.setMinHeight(height);
                    this.setMaxHeight(height);
                }
            });
            pause.play();
        // });
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

        if (start == 0) {
            start = 1;
        }
        // if (this.getText().charAt(start - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR && start != 1 && UString.Count(this.getText().substring(start, end), "\n") > 0) {
        //     start--;
        // }
        else if (this.getText().charAt(start) == CONSTANTS.EMPTY_PARAGRAPH_CHAR && UString.Count(this.getText().substring(start, end), "\n") == 0) {
            start++;
        }
        if (this.getText().charAt(end - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            end--;
        }

        if (start >= end) {
            return false;
        }
        
        List<Integer> emptyParagraphsToDelete = new ArrayList<>();
        logHelper("Before deleted selection");

        boolean join1and2 = UString.Count(this.getText().substring(start, end), "\n") != 0;

        if (this.getText().charAt(end - 1) == '\n' && end < this.getText().length()) {
            end++;
        }

        StyledParagraph join1 = styledParagraphs.get(getParagraphsIndexes(start, start).get(0));
        StyledParagraph join2 = styledParagraphs.get(getParagraphsIndexes(end - 1, end - 1).get(0));

        for (int i : getParagraphsIndexes(start, end - 1)) {
            styledParagraphs.get(i).delete(getParCol(start, i), getParCol(end, i));
            if (styledParagraphs.get(i) != join1) {
                emptyParagraphsToDelete.add(0, i);
            }
        }

        for (int i : emptyParagraphsToDelete) {
            styledParagraphs.remove(i);
        }

        if (join1and2) {
            join1.insertStyledStrings(join1.getPlainText().length(), join2.getStyledStringList());
            styledParagraphs.remove(join2);
        }

        this.deleteText(start, end);
        this.moveTo(start);
        curCaretPos = this.getCaretPosition();
        logHelper("After deleted selection");
        return true;
    }

    private void logHelper(String message) {
        System.out.println("***** Log helper *****  " + message);
        System.out.println("Text: LEN=" + this.getText().length() + "\n" + this.getText());
        System.out.println("Caret position: " + this.getCaretPosition());
        System.out.println("-- Paragraphs: LEN=" + styledParagraphs.size());
        for (int i = 0; i < styledParagraphs.size(); i++) {
            System.out.println("Index " + i + ": LEN=" + styledParagraphs.get(i).getPlainText().length() + "  TEXT:" + styledParagraphs.get(i).getPlainText());
        }

        System.out.println("***** Log helper");
    }

    private void handleMousePressed(MouseEvent event) {
        ac.removeCurrentAC();
        demarkCharOVERWRITE(this.getCaretPosition());
        int index = this.hit(event.getX(), event.getY()).getInsertionIndex();
        if (event.isPrimaryButtonDown()) {
            msgForHandler("CONTEXT_MENU:HIDE");
        } else if (event.isSecondaryButtonDown() && this.getSelectedText().isEmpty()) {
            this.moveTo(index);
        }
        curCaretPos = index;

        // Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            if (event.isSecondaryButtonDown()) {
                textHandler.showRTWidgetContextMenu(event);
            }
        // });
    }

    private void handleKeyPressed_INSERT() {
        int curPos = curCaretPos;

        demarkCharOVERWRITE(curPos);
        isOverwriteMode = !isOverwriteMode;
        markCharOVERWRITE(curPos);
        msgForHandler("INSERT_MODE:" + CONSTANTS.EMPTY_PARAGRAPH_STRING + isOverwriteMode);
    }

    private void handleKeyPressed_ENTER() {
        // Add new paragraph

        int selectedTextParagraphsCount = RTWText.countParagraphs(this.getSelectedText()) - 1;
        if (this.getParagraphs().size() - selectedTextParagraphsCount >= this.maxNumberOfParagraphs) {
            msgForHandler("PARAGRAPHS_LIMIT_EXCEEDED");
            return;
        }

        ac.removeCurrentAC();
        closeFindReplace();

        deleteSelectedText();

        int curPar = getParagraphsIndexes(curCaretPos, curCaretPos).get(0);
        int parCol = getParCol(curCaretPos);
        String parText = styledParagraphs.get(curPar).getPlainText();

        demarkCharOVERWRITE(this.getCaretPosition());

        StyleSheetParagraph prevParCss = styledParagraphs.get(curPar).getCssParagraphStyleObject();
        StyleSheetParagraph newParCss = new StyleSheetParagraph();
        // newParCss.setCss(USettings.getAppOrUserSettingsItem("CssDefaultParagraphStyle", textHandler.getBehavior()).getValueSTRING());

        // If paragraph is last one or caret is at the end of the paragraph
        if (parText.equals(CONSTANTS.EMPTY_PARAGRAPH_STRING) || parCol == parText.length()) {
            if (OBJECTS.SETTINGS.getvBOOLEAN("PreserveParagraphStyle")) {
                styledParagraphs.add(curPar + 1, new StyledParagraph(prevParCss, cssChar));
                this.insertText(curCaretPos, "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
                writeParagraph(curPar + 1);
                moveTo(curPar + 1, 1);
                curCaretPos = this.getCaretPosition();
            } else {
                styledParagraphs.add(curPar + 1, new StyledParagraph(newParCss, cssChar));
                this.insertText(curCaretPos, "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
                writeParagraph(curPar + 1);
                moveTo(curPar + 1, 1);
                curCaretPos = this.getCaretPosition();
           }

            markCharOVERWRITE(curCaretPos);
            this.stateChanged = true;
            trackCursorInScrollPane();
            return;
        }
        
        // if caret is at the beginning of the paragraph
        if (parCol == 0 || parCol == 1) {
            if (parCol == 0) {
                parCol = 1;
                curCaretPos++;
                this.moveTo(curCaretPos);
            }

            styledParagraphs.add(curPar, new StyledParagraph(newParCss, cssChar));
            this.insertText(curCaretPos, "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
            writeParagraph(curPar);
            writeParagraph(curPar + 1);
            moveTo(curPar + 1, 1);
            curCaretPos = this.getCaretPosition();

            markCharOVERWRITE(this.getCaretPosition());
            this.stateChanged = true;
            trackCursorInScrollPane();
            return;
        }

        // If caret is in the middle of the paragraph
        StyledParagraph newPar = styledParagraphs.get(curPar).splitOnIndex(parCol);
        styledParagraphs.add(curPar + 1, newPar);
        this.insertText(curCaretPos, "\n" + CONSTANTS.EMPTY_PARAGRAPH_STRING);
        writeParagraph(curPar);
        writeParagraph(curPar + 1);
        moveTo(curPar + 1, 1);
        curCaretPos = this.getCaretPosition();

        markCharOVERWRITE(this.getCaretPosition());
        this.stateChanged = true;
        trackCursorInScrollPane();
        return;
    }

    private void handleKeyPressed_DELETE() {
        ac.removeCurrentAC();
        closeFindReplace();

        if (!this.getSelectedText().isEmpty()) {
            deleteSelectedText();
            this.stateChanged = true;
            return;
        }

        int caretPos = curCaretPos;
        int curPar = getParagraphsIndexes(caretPos, caretPos).get(0);
        int parTextLen = styledParagraphs.get(curPar).getPlainText().length();

        logHelper("Before delete");

        demarkCharOVERWRITE(caretPos);

        // If caret is at the end of the text then do nothing
        if (caretPos == this.getText().length()) { return; }
        
        // Check if deleted char is "\n"
        if (this.getText().charAt(caretPos) == '\n') {
            // If paragraph is empty then remove it
            if (parTextLen == 1) {
                styledParagraphs.remove(curPar);
                this.deleteText(caretPos - 1, caretPos + 1);
                moveTo(curPar, 1);
                curCaretPos = this.getCaretPosition();
                this.cssChar = styledParagraphs.get(curPar).getPredictedCssChar(styledParagraphs.get(curPar).getPlainText().length(), false);
                sendToHandlerCharAndParagraphCurrentStyle();
                markCharOVERWRITE(curCaretPos);
                this.stateChanged = true;
                logHelper("After delete");
                showAC();
                return;
            }
            // If paragraph is not empty then remove "\n" and next paragraph zero width space
            else {
                styledParagraphs.get(curPar).insertStyledStrings(parTextLen, styledParagraphs.get(curPar + 1).getStyledStringList());
                styledParagraphs.remove(curPar + 1);
                this.deleteText(caretPos, caretPos + 2);
                writeParagraph(curPar);
                moveTo(curPar, parTextLen);
                curCaretPos = this.getCaretPosition();
                this.cssChar = styledParagraphs.get(curPar).getPredictedCssChar(parTextLen, false);
                
                sendToHandlerCharAndParagraphCurrentStyle();
                markCharOVERWRITE(this.getCaretPosition());
                this.stateChanged = true;
                logHelper("After delete");
                showAC();
                return;
            }
        }

        // If deleted char is not "\n"

        styledParagraphs.get(curPar).delete(caretPos, caretPos + 1);
        this.deleteText(caretPos, caretPos + 1);
        writeParagraph(curPar);
        moveTo(caretPos);
        curCaretPos = this.getCaretPosition();

        sendToHandlerCharAndParagraphCurrentStyle();
        markCharOVERWRITE(this.getCaretPosition());
        this.stateChanged = true;
        logHelper("After delete");
        showAC();
    }

    private void handleKeyPressed_BACKSPACE() {
        ac.removeCurrentAC();
        closeFindReplace();

        if (!this.getSelectedText().isEmpty()) {
            deleteSelectedText();
            this.stateChanged = true;
            return;
        }

        int caretPos = curCaretPos;
        int curPar = getParagraphsIndexes(caretPos, caretPos).get(0);
        if (getParCol(caretPos, curPar) == 0) {
            moveTo(curPar, 1);
            curCaretPos = this.getCaretPosition();
            caretPos = curCaretPos;
        }
        int prevParTextLen = -1;

        demarkCharOVERWRITE(caretPos);
        logHelper("Before backspace");

        if (curPar > 0) {
            prevParTextLen = styledParagraphs.get(curPar - 1).getPlainText().length();
        }

        // If caret is at the beginning of the text then do nothing
        if (caretPos == 0 || caretPos == 1) {
            markCharOVERWRITE(caretPos);
            return;
        }
        
        // Check if deleted char is zero width space
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            // If previous paragraph is empty then remove it
            if (prevParTextLen == 1) {
                styledParagraphs.remove(curPar - 1);
                this.deleteText(caretPos - 3, caretPos - 1);
                this.moveTo(caretPos - 2);
                curCaretPos = this.getCaretPosition();
                this.cssChar = styledParagraphs.get(curPar - 1).getPredictedCssChar(styledParagraphs.get(curPar - 1).getPlainText().length(), isOverwriteMode);
                sendToHandlerCharAndParagraphCurrentStyle();
                markCharOVERWRITE(this.getCaretPosition());
                this.stateChanged = true;
                logHelper("After backspace");
                showAC();
                return;
            }
            // If previous paragraph is not empty then remove "\n" and current paragraph zero width space
            else {
                styledParagraphs.get(curPar - 1).insertStyledStrings(prevParTextLen, styledParagraphs.get(curPar).getStyledStringList());
                styledParagraphs.remove(curPar);
                this.deleteText(caretPos - 2, caretPos);
                writeParagraph(curPar - 1);
                this.moveTo(caretPos - 2);
                curCaretPos = this.getCaretPosition();

                this.cssChar = styledParagraphs.get(curPar - 1).getPredictedCssChar(prevParTextLen, isOverwriteMode);
                this.cssParagraph = styledParagraphs.get(curPar - 1).getCssParagraphStyleObject().duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();
                markCharOVERWRITE(this.getCaretPosition());
                this.stateChanged = true;
                logHelper("After backspace");
                showAC();
                return;
            }
        }

        // If deleted char is not zero width space

        styledParagraphs.get(curPar).delete(getParCol(caretPos - 1, curPar), getParCol(caretPos, curPar));
        this.deleteText(caretPos - 1, caretPos);
        this.moveTo(caretPos - 1);
        curCaretPos = this.getCaretPosition();

        this.cssChar = styledParagraphs.get(curPar).getPredictedCssChar(getParCol(curCaretPos), isOverwriteMode);
        sendToHandlerCharAndParagraphCurrentStyle();
        markCharOVERWRITE(this.getCaretPosition());
        this.stateChanged = true;
        logHelper("After backspace");
        showAC();
    }

    private void handleKeyPressed_UP() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            ensureGoodCaretPosition();
        });
    }

    private void handleKeyPressed_UP_SHIFT() {
        ac.removeCurrentAC();

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getCaretPosition());
            curCaretPos = this.getCaretPosition();
        });
    }

    private void handleKeyPressed_UP_CTRL() {
        ac.prevAC();

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            curCaretPos = this.getCaretPosition();
        });
    }

    private void handleKeyPressed_DOWN() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            ensureGoodCaretPosition();
        });
    }

    private void handleKeyPressed_DOWN_SHIFT() {
        ac.removeCurrentAC();

        int anchorPos = this.getAnchor();
        demarkCharOVERWRITE(this.getCaretPosition());

        Platform.runLater(() -> {
            this.selectRange(anchorPos, this.getCaretPosition());
            curCaretPos = this.getCaretPosition();
        });
    }

    private void handleKeyPressed_DOWN_CTRL() {
        ac.nextAC();

        Platform.runLater(() -> {
            markCharOVERWRITE(this.getCaretPosition());
            curCaretPos = this.getCaretPosition();
        });
    }

    private void handleKeyPressed_LEFT() {
        int caretPos = curCaretPos;

        if (caretPos == 0 || caretPos == 1) {
            return;
        }

        ac.removeCurrentAC();

        demarkCharOVERWRITE(caretPos);
        
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.moveTo(caretPos - 2);
        } else {
            this.moveTo(caretPos - 1);
        }
        curCaretPos = this.getCaretPosition();

        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_LEFT_SHIFT() {
        ac.removeCurrentAC();
        
        int caretPos = curCaretPos;
        int anchorPos = this.getAnchor();

        if (caretPos == 0 || caretPos == 1) {
            return;
        }

        demarkCharOVERWRITE(caretPos);
        
        if (this.getText().charAt(caretPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
            this.moveTo(caretPos - 2);
        } else {
            this.moveTo(caretPos - 1);
        }
        curCaretPos = this.getCaretPosition();

        this.selectRange(anchorPos, this.getCaretPosition());
    }

    private void handleKeyPressed_RIGHT() {
        int caretPos = curCaretPos;

        ac.removeCurrentAC();

        if (caretPos == this.getText().length()) {
            return;
        }

        demarkCharOVERWRITE(caretPos);
        if (this.getText().charAt(caretPos) == '\n') {
            this.moveTo(caretPos + 2);
        } else {
            this.moveTo(caretPos + 1);
        }
        curCaretPos = this.getCaretPosition();

        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_RIGHT_SHIFT() {
        ac.removeCurrentAC();
        
        int caretPos = curCaretPos;
        int anchorPos = this.getAnchor();

        if (caretPos == this.getText().length()) {
            return;
        }

        demarkCharOVERWRITE(caretPos);
        if (this.getText().charAt(caretPos) == '\n') {
            this.moveTo(caretPos + 2);
        } else {
            this.moveTo(caretPos + 1);
        }
        curCaretPos = this.getCaretPosition();

        this.selectRange(anchorPos, this.getCaretPosition());
    }

    private void handleKeyPressed_RIGHT_CTRL() {
        if (!ac.hasCurrentAC()) return;

        closeFindReplace();

        String acString = ac.getCurrentACClean();
        ac.removeCurrentAC();

        String strToAdd = null;

        if (acString.indexOf(" ", 1) > 0) {
            strToAdd = acString.substring(0, acString.indexOf(" ", 1) + 1);
        } else {
            strToAdd = acString;
        }

        pasteText(null, strToAdd);
        showAC();
        stateChanged = true;
    }

    private void handleKeyPressed_TAB() {
        if (!ac.hasCurrentAC()) return;

        closeFindReplace();

        String acString = ac.getCurrentACClean();
        ac.removeCurrentAC();

        pasteText(curCaretPos, acString);
        showAC();
        stateChanged = true;
    }

    private void handleKeyPressed_ESC() {
        if (!ac.hasCurrentAC()) return;

        ac.removeCurrentAC();
    }

    private void handleKeyPressed_PgUP() {
        if (this.getCurrentParagraph() == 0) {
            return;
        }
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (styledParagraphs.get(i).getPlainText().length() == 1) {
                start = true;
            }
            if (start && styledParagraphs.get(i).getPlainText().length() > 1) {
                this.moveTo(i, 1);
                curCaretPos = this.getCaretPosition();
                this.cssChar = styledParagraphs.get(i).getPredictedCssChar(1, isOverwriteMode);
                this.cssParagraph = styledParagraphs.get(i).getCssParagraphStyleObject().duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();

                markCharOVERWRITE(this.getCaretPosition());
                return;
            }
        }

        this.moveTo(0, 1);
        curCaretPos = this.getCaretPosition();
        this.cssChar = styledParagraphs.get(0).getPredictedCssChar(1, isOverwriteMode);
        this.cssParagraph = styledParagraphs.get(0).getCssParagraphStyleObject().duplicate();
        sendToHandlerCharAndParagraphCurrentStyle();
        
        markCharOVERWRITE(this.getCaretPosition());

    }

    private void handleKeyPressed_PgUP_SHIFT() {
        ac.removeCurrentAC();

        if (this.getCurrentParagraph() == 0) {
            return;
        }

        int anchorPos = this.getAnchor();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() - 1; i >= 0; i--) {
            if (styledParagraphs.get(i).getPlainText().length() == 1) {
                start = true;
            }
            if (start && styledParagraphs.get(i).getPlainText().length() > 1) {
                this.moveTo(i, 1);
                curCaretPos = this.getCaretPosition();
                int curPar = i;
                this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
                return;
            }
        }

        this.moveTo(0, 1);
        curCaretPos = this.getCaretPosition();
        
        this.selectRange(anchorPos, this.getPosGlobal(0, 1));
    }

    private void handleKeyPressed_PgDOWN() {
        if (this.getCurrentParagraph() == styledParagraphs.size() - 1) {
            return;
        }

        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < styledParagraphs.size(); i++) {
            if (styledParagraphs.get(i).getPlainText().length() == 1) {
                start = true;
            }
            if (start && styledParagraphs.get(i).getPlainText().length() > 1) {
                this.moveTo(i, 1);
                curCaretPos = this.getCaretPosition();
                this.cssChar = styledParagraphs.get(i).getPredictedCssChar(1, isOverwriteMode);
                this.cssParagraph = styledParagraphs.get(i).getCssParagraphStyleObject().duplicate();
                sendToHandlerCharAndParagraphCurrentStyle();

                markCharOVERWRITE(this.getCaretPosition());
                return;
            }
        }

        this.moveTo(this.getParagraphs().size() - 1, 1);
        curCaretPos = this.getCaretPosition();
        this.cssChar = styledParagraphs.get(this.getParagraphs().size() - 1).getPredictedCssChar(1, isOverwriteMode);
        this.cssParagraph = styledParagraphs.get(this.getParagraphs().size() - 1).getCssParagraphStyleObject().duplicate();
        sendToHandlerCharAndParagraphCurrentStyle();
        
        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_PgDOWN_SHIFT() {
        ac.removeCurrentAC();

        if (this.getCurrentParagraph() == styledParagraphs.size() - 1) {
            return;
        }

        int anchorPos = this.getAnchor();
        
        demarkCharOVERWRITE(this.getCaretPosition());

        boolean start = false;

        for (int i = this.getCurrentParagraph() + 1; i < styledParagraphs.size(); i++) {
            if (styledParagraphs.get(i).getPlainText().length() == 1) {
                start = true;
            }
            if (start && styledParagraphs.get(i).getPlainText().length() > 1) {
                this.moveTo(i, 1);
                curCaretPos = this.getCaretPosition();
                int curPar = i;

                this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
                return;
            }
        }

        this.moveTo(styledParagraphs.size() - 1, 1);
        curCaretPos = this.getCaretPosition();
        
        this.selectRange(anchorPos, this.getPosGlobal(styledParagraphs.size() - 1, 1));
    }

    private void handleKeyPressed_HOME() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();

        this.moveTo(curPar, 1);
        curCaretPos = this.getCaretPosition();
        
        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_HOME_SHIFT() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();
        int anchorPos = this.getAnchor();

        this.selectRange(anchorPos, this.getPosGlobal(curPar, 1));
    }

    private void handleKeyPressed_HOME_CTRL() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.moveTo(0, 1);
        curCaretPos = this.getCaretPosition();
        
        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_END() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();

        this.moveTo(curPar, styledParagraphs.get(curPar).getPlainText().length());
        curCaretPos = this.getCaretPosition();
        
        markCharOVERWRITE(this.getCaretPosition());
    }

    private void handleKeyPressed_END_SHIFT() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        int curPar = this.getCurrentParagraph();
        int anchorPos = this.getAnchor();

        this.selectRange(anchorPos, this.getPosGlobal(curPar, styledParagraphs.get(curPar).getPlainText().length()));
    }

    private void handleKeyPressed_END_CTRL() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(this.getCaretPosition());

        this.moveTo(styledParagraphs.size() - 1, styledParagraphs.get(styledParagraphs.size() - 1).getPlainText().length());
        curCaretPos = this.getCaretPosition();
        
        markCharOVERWRITE(this.getCaretPosition());
    }

    private void actionSELECT_ALL() {
        ac.removeCurrentAC();

        demarkCharOVERWRITE(curCaretPos);

        this.selectRange(1, this.getText().length());
        
        markCharOVERWRITE(curCaretPos);
    }

    private void handleKeyTyped_CHARACTER(KeyEvent event) {
        ac.removeCurrentAC();
        closeFindReplace();

        if (this.getSelectedText().length() > 0) {
            deleteSelectedText();
        }

        String charTyped = event.getCharacter();

        int curPos = curCaretPos;
        int currentParagraph = this.getParagraphsIndexes(curPos, curPos).get(0);
        int parCol = this.getParCol(curPos);
        if (parCol == 0) {
            parCol = 1;
            moveTo(currentParagraph, 1);
            curCaretPos = this.getCaretPosition();
            curPos = curCaretPos;
        }
        demarkCharOVERWRITE(curPos);

        StyleSheetChar typedCharCss = styledParagraphs.get(currentParagraph).getPredictedCssChar(parCol, false);

        styledParagraphs.get(currentParagraph).insertPlainText(parCol, charTyped, typedCharCss);
        insertText(curPos, charTyped);
        setStyle(curPos, curPos + 1, typedCharCss.getCss());
        moveTo(currentParagraph, parCol + 1);
        curCaretPos = this.getCaretPosition();
        this.cssChar = styledParagraphs.get(currentParagraph).getPredictedCssChar(parCol + 1, isOverwriteMode);

        markCharOVERWRITE(curCaretPos);
        sendToHandlerCharAndParagraphCurrentStyle();
        this.stateChanged = true;
        showAC();
        


        // if (curPos <= this.cssStyles.size() && curPos > 0 && !this.cssStyles.get(curPos - 1).equals(this.cssChar) && this.getTextNoAC().charAt(curPos - 1) == CONSTANTS.EMPTY_PARAGRAPH_CHAR) {
        //     this.cssStyles.set(curPos - 1, this.cssChar.duplicate());
        //     this.setStyle(curPos - 1, curPos, this.cssChar.getCss());
        // }

        // demarkCharOVERWRITE(curPos);

        // // Caret is at the end of the text
        // if (curPos == this.cssStyles.size()) {
        //     this.insertText(curPos, charTyped);
        //     this.cssStyles.add(curPos, cssChar.duplicate());
        //     this.setStyle(curPos, curPos + 1, cssChar.getCss());
        //     Platform.runLater(() -> {
        //         showAC();
        //         ignoreTextChangePERMANENT = false;
        //         ignoreCaretPositionChangePERMANENT = false;
        //         markCharOVERWRITE(this.getCaretPosition());
        //         sendToHandlerCharAndParagraphCurrentStyle();
        //         this.stateChanged = true;
        //         // this.busy = false;
        //     });
        //     return;
        // }

        // // Caret is at end of the paragraph
        // if (curPos < this.getTextNoAC().length() && this.getTextNoAC().charAt(curPos) == '\n') {
        //     this.insertText(curPos, charTyped);
        //     this.cssStyles.add(curPos, cssChar.duplicate());
        //     this.setStyle(curPos, curPos + 1, cssChar.getCss());
        //     Platform.runLater(() -> {
        //         showAC();
        //         ignoreTextChangePERMANENT = false;
        //         ignoreCaretPositionChangePERMANENT = false;
        //         markCharOVERWRITE(this.getCaretPosition());
        //         sendToHandlerCharAndParagraphCurrentStyle();
        //         this.stateChanged = true;
        //         // this.busy = false;
        //     });
        //     return;
        // }

        // // Caret is in the middle of the text
        // if (isOverwriteMode) {
        //     this.deleteText(curPos, curPos + 1);
        //     this.cssStyles.remove(curPos);
        // }
        // this.insertText(curPos, charTyped);
        // this.cssStyles.add(curPos, cssChar.duplicate());
        // this.setStyle(curPos, curPos + 1, cssChar.getCss());
        // Platform.runLater(() -> {
        //     showAC();
        //     ignoreTextChangePERMANENT = false;
        //     ignoreCaretPositionChangePERMANENT = false;
        //     markCharOVERWRITE(this.getCaretPosition());
        //     this.cssChar = getPredictedCssChar(this.getCaretPosition());
        //     sendToHandlerCharAndParagraphCurrentStyle();
        //     this.stateChanged = true;
        //     // this.busy = false;
        // });
    }

    private void handleCaretPositionChange(Object obs, Integer oldPos, Integer newPos) {
        if (styledParagraphs.size() == 0 || ignoreCaretPositionChangePERMANENT) {
            return;
        }

        curCaretPos = newPos;

        demarkCharOVERWRITE(oldPos);
        markCharOVERWRITE(newPos);

        trackCursorInScrollPane();

        int paragraphIndex = getParagraphsIndexes(curCaretPos, curCaretPos).get(0);
        this.cssChar = styledParagraphs.get(paragraphIndex).getPredictedCssChar(getParCol(curCaretPos), isOverwriteMode).duplicate();
        this.cssParagraph = styledParagraphs.get(paragraphIndex).getCssParagraphStyleObject();

        sendToHandlerCharAndParagraphCurrentStyle();

        Platform.runLater(() -> {
            trackCursorInScrollPane();
        });

    }

    private void trackCursorInScrollPane() {
        Platform.runLater(() -> {
            if (scrollPane == null) {
                return;
            }
            
            double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
            double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();
            
            // If scrollPane is not set yet or content is smaller than scrollPane then do nothing
            if (contentHeight <= scrollPaneHeight) {
                return;
            }

            Optional<Bounds> optBounds = this.getCaretBounds();
            Bounds boundsRTW = this.localToScreen(this.getBoundsInLocal());
            if (optBounds.isPresent()) {
                
                Bounds caretBounds = optBounds.get();

                double caretTop = caretBounds.getMinY() - boundsRTW.getMinY() - this.getPadding().getTop();
                double caretBottom = caretBounds.getMaxY() - boundsRTW.getMinY() + this.getPadding().getBottom();
                double contentVisibleTop = (contentHeight - scrollPaneHeight) * scrollPane.getVvalue();
                double contentVisibleBottom = contentVisibleTop + scrollPaneHeight;

                if (caretTop >= contentVisibleTop && caretBottom <= contentVisibleBottom) {
                    return;
                }

                double barPos = 0;
                if (caretTop < contentVisibleTop && caretBottom <= contentVisibleBottom) {
                    barPos = (caretTop / (contentHeight - scrollPaneHeight));
                }

                if (caretBottom > contentVisibleBottom && caretTop >= contentVisibleTop) {
                    barPos = (caretBottom - scrollPaneHeight) / (contentHeight - scrollPaneHeight);
                }

                if (barPos < 0) {
                    barPos = 0;
                } else if (barPos > 1) {
                    barPos = 1;
                }
                
                scrollPane.setVvalue(barPos);
            }
            
        });
    }
        
    private void sendToHandlerCharAndParagraphCurrentStyle() {
        msgForHandler(this.cssChar);
        msgForHandler(this.cssParagraph);
    }


}
