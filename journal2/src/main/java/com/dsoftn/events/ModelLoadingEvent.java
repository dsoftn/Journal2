package com.dsoftn.events;

import com.dsoftn.models.enums.ScopeEnum;
import com.dsoftn.models.enums.ModelLoadingState;

import javafx.event.Event;
import javafx.event.EventType;


public class ModelLoadingEvent extends Event {

    public static final EventType<ModelLoadingEvent> MODEL_LOADING_EVENT = new EventType<>(Event.ANY, "MODEL_LOADING_EVENT");
    
    private final ScopeEnum model;
    private final ModelLoadingState state;
    private final Integer progressPercent;
    private final Integer currentRecord;
    private final Integer totalRecords;
    private final String message;

    // Constructors

    /**
     * Model and state only
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state) {
        super(MODEL_LOADING_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
    }

    /**
     * Model and state + message
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state, String message) {
        super(MODEL_LOADING_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = null;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
    }

    /**
     * Model and state + progressPercent(0-100)
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state, Integer progressPercent) {
        super(MODEL_LOADING_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = null;
    }

    /**
     * Model and state + progressPercent(0-100) + message
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state, Integer progressPercent, String message) {
        super(MODEL_LOADING_EVENT);
        this.model = model;
        this.state = state;
        this.progressPercent = progressPercent;
        this.currentRecord = null;
        this.totalRecords = null;
        this.message = message;
    }

    /**
     * Model and state + currentRecord + totalRecords
     * ProgressPercent is calculated automatically
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state, Integer currentRecord, Integer totalRecords) {
        super(MODEL_LOADING_EVENT);
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
    }

    /**
     * Model and state + currentRecord + totalRecords + message
     * ProgressPercent is calculated automatically
     */
    public ModelLoadingEvent(ScopeEnum model, ModelLoadingState state, Integer currentRecord, Integer totalRecords, String message) {
        super(MODEL_LOADING_EVENT);
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
    }

    // Getters

    public ScopeEnum getModel() { return this.model; }

    public ModelLoadingState getState() { return this.state; }

    public Integer getProgressPercent() { return this.progressPercent; }

    public Integer getCurrentRecord() { return this.currentRecord; }

    public Integer getTotalRecords() { return this.totalRecords; }

    public String getMessage() { return this.message; }
}
