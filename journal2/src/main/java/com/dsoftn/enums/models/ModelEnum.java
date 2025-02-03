package com.dsoftn.enums.models;

import java.util.Arrays;


public enum ModelEnum {

    NONE(1),
    ALL(2),
    BLOCK(4),
    DEFINITION(8),
    ATTACHMENT(16),
    CATEGORY(32),
    TAG(64),
    RELATION(128),
    DEF_VARIANT(256),
    ACTOR(512),
    BLOCK_TYPE(1024)
    ;

    private final int value;

    // Constructor
    
    ModelEnum(int value) {
        this.value = value;
    }

    // Methods
    
    public int getValue() {
        return value;
    }

    /**
     * Combine multiple ScopeEnum values into one
     */
    public static int combineValues(ModelEnum ... types) {
        // Convert types to integer values and use combineValues method that takes integer values
        int[] integerValues = new int[types.length];

        for (int i = 0; i < types.length; i++) {
            integerValues[i] = types[i].getValue();
        }

        // If types contains NONE, return ScopeEnum.NONE, If types contains ALL, return ScopeEnum.ALL
        for (ModelEnum type : types) {
            if (type == ModelEnum.NONE) {
                return ModelEnum.NONE.getValue();
            }
            if (type == ModelEnum.ALL) {
                return ModelEnum.ALL.getValue();
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
    public static ModelEnum[] getContent (int value) {
        return Arrays.stream(values())
                .filter(type -> (value & type.getValue()) != 0)
                .toArray(ModelEnum[]::new);
    }

    /**
     * Get ScopeEnum object from integer value
     * <p>If value is not found, returns null</p>
     */
    public static ModelEnum fromInteger(int value) {
        for (ModelEnum type : values()) {
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
    public static ModelEnum fromName(String name) {
        for (ModelEnum type : values()) {
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
    public static boolean hasType(int integerToExamine, ModelEnum requiredType) {
        return (integerToExamine & requiredType.getValue()) != 0;
    }

}
