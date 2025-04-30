package com.dsoftn.enums.models;

public enum TaskStateEnum {

    UNDEFINED(1),
    STARTED(2),
    EXECUTING(3),
    COMPLETED(4),
    FAILED(5),
    CANCELED(6)
    ;

    private final int value;

    TaskStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskStateEnum fromInteger(int value) {
        for (TaskStateEnum type : TaskStateEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return TaskStateEnum.UNDEFINED;
    }

}
