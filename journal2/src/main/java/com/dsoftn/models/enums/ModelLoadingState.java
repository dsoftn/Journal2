package com.dsoftn.models.enums;

public enum ModelLoadingState {

    UNDEFINED(1),
    LOADING_STARTED(2),
    LOADING_IN_PROGRESS(3),
    LOADING_COMPLETED(4),
    LOADING_FAILED(5);

    private final int value;

    ModelLoadingState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ModelLoadingState fromInteger(int value) {
        for (ModelLoadingState type : ModelLoadingState.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return ModelLoadingState.UNDEFINED;
    }

}
