package com.dsoftn.enums.models;

public enum BlockTypeEnum {

    UNDEFINED(1),
    DIARY(2),
    BUDGET(3),
    CONTACT(4),
    COMPANY(5),
    MOVIE(6),
    MUSIC(7),
    BOOK(8),
    GAME(9),
    APP(10),
    WEBSITE(11),
    VIDEO(12),
    REMAINDER(13),
    QUOTE(14),
    HEALTH(15),
    RECIPE(16),
    TRAVEL(17),
    EVENT(18),
    WORKOUT(19),
    PASSWORD(20)
    ;

    private final int value;
    
    BlockTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static BlockTypeEnum fromInteger(int value) {
        for (BlockTypeEnum type : BlockTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
    
    public static BlockTypeEnum fromName(String name) {
        for (BlockTypeEnum type : BlockTypeEnum.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
