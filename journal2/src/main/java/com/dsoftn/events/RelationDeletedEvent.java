package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Relation;


public class RelationDeletedEvent extends Event {

    public static final EventType<RelationDeletedEvent> RELATION_DELETED_EVENT = new EventType<>(Event.ANY, "RELATION_DELETED_EVENT");
    
    private final Relation relation;
    private Boolean loopEvent;

    // Constructor

    public RelationDeletedEvent(Relation relation, boolean loopEvent) {
        super(RELATION_DELETED_EVENT);
        this.relation = relation;
        this.loopEvent = loopEvent;
    }

    // Getters

    public Relation getRelation() { return this.relation; }

    public boolean isLoopEvent() { return this.loopEvent; }

}
