package com.dsoftn.enums.controllers;

public enum TextToolbarActionEnum {
    UNDO(1),
    REDO(2),
    CUT(3),
    COPY(4),
    PASTE(5),
    INSERT_IMAGE(6),
    INSERT_SMILEY(7),
    INSERT_DATA(8),
    FONT_NAME(9),
    FONT_SIZE(10),
    FOREGROUND_COLOR(11),
    BACKGROUND_COLOR(12),
    BOLD(13),
    ITALIC(14),
    UNDERLINE(15),
    STRIKETHROUGH(16),
    FIND_TEXT(17),
    FIND_MATCH_CASE(18),
    FIND_WHOLE_WORDS(19),
    FIND_UP(20),
    FIND_DOWN(21),
    REPLACE_TEXT(22),
    REPLACE_ONE(23),
    REPLACE_ALL(24),
    FOCUS_TO_TEXT(25);

    private final int value;

    TextToolbarActionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getInteger() {
        return value;
    }

    public static TextToolbarActionEnum fromInteger(int value) {
        for (TextToolbarActionEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    public static TextToolbarActionEnum fromName(String name) {
        for (TextToolbarActionEnum type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
