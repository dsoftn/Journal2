package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Relation;


public class RelationAddedEvent extends Event {

    public static final EventType<RelationAddedEvent> RELATION_ADDED_EVENT = new EventType<>(Event.ANY, "RELATION_ADDED_EVENT");
    
    private final Relation relation;

    // Constructor

    public RelationAddedEvent(Relation relation) {
        super(RELATION_ADDED_EVENT);
        this.relation = relation;
    }

    // Getters

    public Relation getRelation() { return this.relation; }

}
