package com.dsoftn.enums.models;

public enum SourceTypeEnum {

    UNDEFINED(1),
    LOCAL(2),
    LOCAL_NETWORK(3),
    URL(4);

    private final int value;
    
    SourceTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SourceTypeEnum fromString(String value) {
        for (SourceTypeEnum type : SourceTypeEnum.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return SourceTypeEnum.UNDEFINED;
    }

    public static SourceTypeEnum fromInteger(int value) {
        for (SourceTypeEnum type : SourceTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return SourceTypeEnum.UNDEFINED;
    }
    
}
