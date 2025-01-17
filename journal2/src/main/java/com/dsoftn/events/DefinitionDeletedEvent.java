package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Definition;


public class DefinitionDeletedEvent extends Event {

    public static final EventType<DefinitionDeletedEvent> DEFINITION_DELETED_EVENT = new EventType<>(Event.ANY, "DEFINITION_DELETED_EVENT");
    
    private final Definition definition;

    // Constructor

    public DefinitionDeletedEvent(Definition definition) {
        super(DEFINITION_DELETED_EVENT);
        this.definition = definition;
    }

    // Getters

    public Definition getDefinition() { return this.definition; }

}
