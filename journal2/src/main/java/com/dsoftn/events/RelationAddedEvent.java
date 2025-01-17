package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Relation;


public class RelationAddedEvent extends Event {

    public static final EventType<RelationAddedEvent> RELATION_ADDED_EVENT = new EventType<>(Event.ANY, "RELATION_ADDED_EVENT");
    
    private final Relation relation;
    private Boolean loopEvent;

    // Constructor

    public RelationAddedEvent(Relation relation, boolean loopEvent) {
        super(RELATION_ADDED_EVENT);
        this.relation = relation;
        this.loopEvent = loopEvent;
    }

    // Getters

    public Relation getRelation() { return this.relation; }

    public boolean isLoopEvent() { return this.loopEvent; }

}
