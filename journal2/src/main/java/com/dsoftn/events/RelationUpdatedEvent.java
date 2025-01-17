package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Relation;


public class RelationUpdatedEvent extends Event {

    public static final EventType<RelationUpdatedEvent> RELATION_UPDATED_EVENT = new EventType<>(Event.ANY, "RELATION_UPDATED_EVENT");
    
    private final Relation oldRelation;
    private final Relation newRelation;
    private Boolean loopEvent;

    // Constructor

    public RelationUpdatedEvent(Relation oldRelation, Relation newRelation, boolean loopEvent) {
        super(RELATION_UPDATED_EVENT);
        this.oldRelation = oldRelation;
        this.newRelation = newRelation;
        this.loopEvent = loopEvent;
    }

    // Getters

    public Relation getOldRelation() { return this.oldRelation; }

    public Relation getNewRelation() { return this.newRelation; }

    public Boolean isLoopEvent() { return this.loopEvent; }

}
