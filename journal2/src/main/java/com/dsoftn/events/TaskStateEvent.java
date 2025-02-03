package com.dsoftn.events;

import com.dsoftn.enums.models.ModelEnum;
import com.dsoftn.enums.models.TaskStateEnum;

import javafx.event.Event;
import javafx.event.EventType;


public class TaskStateEvent extends Event {

    public static final EventType<TaskStateEvent> TASK_STATE_EVENT = new EventType<>(Event.ANY, "TASK_STATE_EVENT");
    
    private final String id;
    private final ModelEnum model;
    private final TaskStateEnum state;
    private final Integer progressPercent;
    private final Integer currentRecord;
    private final Integer totalRecords;
    private final String message;

    // Constructors

    // FOR MODELS

    /**
     * Model and state only
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
        this.id = null;
    }

    /**
     * Model and state + message
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state, String message) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
        this.id = null;
    }

    /**
     * Model and state + progressPercent(0-100)
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state, Integer progressPercent) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
        this.id = null;
    }

    /**
     * Model and state + progressPercent(0-100) + message
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state, Integer progressPercent, String message) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
        this.id = null;
    }

    /**
     * Model and state + currentRecord + totalRecords
     * ProgressPercent is calculated automatically
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state, Integer currentRecord, Integer totalRecords) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        
        if (currentRecord != null && totalRecords != null && totalRecords != 0) {
            this.progressPercent = (int) (((double) currentRecord / (double) totalRecords) * 100.0);
        }
        else {
            this.progressPercent = null;
        }
        
        this.currentRecord = currentRecord;
        this.totalRecords = totalRecords;
        this.message = null;
        this.id = null;
    }

    /**
     * Model and state + currentRecord + totalRecords + message
     * ProgressPercent is calculated automatically
     */
    public TaskStateEvent(ModelEnum model, TaskStateEnum state, Integer currentRecord, Integer totalRecords, String message) {
        super(TASK_STATE_EVENT);
        this.model = model;
        this.state = state;
        
        if (currentRecord != null && totalRecords != null && totalRecords != 0) {
            this.progressPercent = (int) (((double) currentRecord / (double) totalRecords) * 100.0);
        }
        else {
            this.progressPercent = null;
        }
        
        this.currentRecord = currentRecord;
        this.totalRecords = totalRecords;
        this.message = message;
        this.id = null;
    }

    // FOR OTHER TASKS

    /**
     * ID and State only
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     */
    public TaskStateEvent(String id, TaskStateEnum state) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
        this.id = id;
    }

    /**
     * ID and State + message
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     */
    public TaskStateEvent(String id, TaskStateEnum state, String message) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
        this.id = id;
    }

    /**
     * ID and State + progressPercent(0-100)
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     */
    public TaskStateEvent(String id, TaskStateEnum state, Integer progressPercent) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
        this.id = id;
    }

    /**
     * ID and State + progressPercent(0-100) + message
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     */
    public TaskStateEvent(String id, TaskStateEnum state, Integer progressPercent, String message) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
        this.id = id;
    }

    /**
     * ID and State + currentRecord + totalRecords
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     * ProgressPercent is calculated automatically
     */
    public TaskStateEvent(String id, TaskStateEnum state, Integer currentRecord, Integer totalRecords) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        
        if (currentRecord != null && totalRecords != null && totalRecords != 0) {
            this.progressPercent = (int) (((double) currentRecord / (double) totalRecords) * 100.0);
        }
        else {
            this.progressPercent = null;
        }
        
        this.currentRecord = currentRecord;
        this.totalRecords = totalRecords;
        this.message = null;
        this.id = id;
    }

    /**
     * ID and State + currentRecord + totalRecords + message
     * For ID use String.valueOf(System.identityHashCode(classObject)) + task name
     * ProgressPercent is calculated automatically
     */
    public TaskStateEvent(String id, TaskStateEnum state, Integer currentRecord, Integer totalRecords, String message) {
        super(TASK_STATE_EVENT);
        this.model = ModelEnum.NONE;
        this.state = state;
        
        if (currentRecord != null && totalRecords != null && totalRecords != 0) {
            this.progressPercent = (int) (((double) currentRecord / (double) totalRecords) * 100.0);
        }
        else {
            this.progressPercent = null;
        }
        
        this.currentRecord = currentRecord;
        this.totalRecords = totalRecords;
        this.message = message;
        this.id = id;
    }


    // Getters

    public ModelEnum getModel() { return this.model; }

    public TaskStateEnum getState() { return this.state; }

    public Integer getProgressPercent() { return this.progressPercent; }
    
    public String getProgressString() { 
        if (this.progressPercent == null) {
            return "";
        }
        return String.valueOf(this.progressPercent) + " %";
    }

    public Integer getCurrentRecord() { return this.currentRecord; }

    public Integer getTotalRecords() { return this.totalRecords; }

    public String getMessage() { return this.message; }

    public String getID() { return this.id; }
}
