package com.dsoftn.enums.models;

public enum DefaultAttachmentShowPolicy {

    DEFAULT(1),
    DISABLED(2),
    ENABLED_LEFT(3),
    ENABLED_RIGHT(4);

    private final int value;
    
    DefaultAttachmentShowPolicy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int getInteger() {
        return value;
    }

    public String getName() {
        return name();
    }

    public static DefaultAttachmentShowPolicy fromInteger(int value) {
        for (DefaultAttachmentShowPolicy type : DefaultAttachmentShowPolicy.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
    
    public static DefaultAttachmentShowPolicy fromName(String name) {
        for (DefaultAttachmentShowPolicy type : DefaultAttachmentShowPolicy.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}
