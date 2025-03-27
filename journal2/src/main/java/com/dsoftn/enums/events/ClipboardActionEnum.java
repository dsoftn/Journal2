package com.dsoftn.enums.events;

public enum ClipboardActionEnum {

    COPY(1),
    ADD(2),
    CUT(3),
    PASTE(4),
    REMOVE_ITEM(5),
    DELETE_ALL(6),
    DELETE_MODEL(7),
    SYSTEM_CLIPBOARD_CHANGED(8);

    private final int value;

    ClipboardActionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getInteger() {
        return value;
    }

    public static ClipboardActionEnum fromInteger(int value) {
        for (ClipboardActionEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

}
