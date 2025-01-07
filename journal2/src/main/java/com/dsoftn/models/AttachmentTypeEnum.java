package com.dsoftn.models;


public enum AttachmentTypeEnum {

    UNDEFINED(1),
    IMAGE(2),
    VIDEO(3),
    AUDIO(4),
    TEXT(5),
    PDF(6),
    HTML(7),
    XML(8),
    JSON(9),
    CSV(10),
    EXCEL(11),
    WORD(12);

    private final int value;

    // Constructor
    
    AttachmentTypeEnum(int value) {
        this.value = value;
    }

    // Methods
    
    public int getValue() {
        return value;
    }

    public Integer getInteger() {
        return value;
    }

    public static AttachmentTypeEnum fromInteger(int value) {
        for (AttachmentTypeEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

}
