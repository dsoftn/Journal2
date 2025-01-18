package com.dsoftn.models.enums;

import java.util.Arrays;


public enum ScopeEnum {

    NONE(1),
    ALL(2),
    BLOCK(4),
    DEFINITION(8),
    ATTACHMENT(16),
    CATEGORY(32),
    TAG(64),
    RELATION(128),
    DEF_VARIANT(256)
    ;

    private final int value;

    // Constructor
    
    ScopeEnum(int value) {
        this.value = value;
    }

    // Methods
    
    public int getValue() {
        return value;
    }

    /**
     * Combine multiple ScopeEnum values into one
     */
    public static int combineValues(ScopeEnum ... types) {
        // Convert types to integer values and use combineValues method that takes integer values
        int[] integerValues = new int[types.length];

        for (int i = 0; i < types.length; i++) {
            integerValues[i] = types[i].getValue();
        }

        // If types contains NONE, return ScopeEnum.NONE, If types contains ALL, return ScopeEnum.ALL
        for (ScopeEnum type : types) {
            if (type == ScopeEnum.NONE) {
                return ScopeEnum.NONE.getValue();
            }
            if (type == ScopeEnum.ALL) {
                return ScopeEnum.ALL.getValue();
            }
        }

        // Combine integer values
        return combineValues(integerValues);
    }

    /**
     * Combine multiple integer values into one
     */
    public static int combineValues(int ... values) {
        int combinedValue = 0;

        for (int value : values) {
            combinedValue |= value;
        }

        return combinedValue;
    }

    /**
     * Get ScopeEnum array from integer value
     */
    public static ScopeEnum[] getContent (int value) {
        return Arrays.stream(values())
                .filter(type -> (value & type.getValue()) != 0)
                .toArray(ScopeEnum[]::new);
    }

    /**
     * Get ScopeEnum object from integer value
     * <p>If value is not found, returns null</p>
     */
    public static ScopeEnum fromInteger(int value) {
        for (ScopeEnum type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get ScopeEnum object from name
     * <p>If name is not found, returns null</p>
     */
    public static ScopeEnum fromName(String name) {
        for (ScopeEnum type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    // Checkers

    /**
     * Check if this integer contains specified type
     * <p>Example: hasType(12, ScopeEnum.BLOCK) = true</p>
     */
    public static boolean hasType(int integerToExamine, ScopeEnum requiredType) {
        return (integerToExamine & requiredType.getValue()) != 0;
    }

}
