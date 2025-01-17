package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Definition;


public class DefinitionAddedEvent extends Event {

    public static final EventType<DefinitionAddedEvent> DEFINITION_ADDED_EVENT = new EventType<>(Event.ANY, "DEFINITION_ADDED_EVENT");
    
    private final Definition definition;

    // Constructor

    public DefinitionAddedEvent(Definition definition) {
        super(DEFINITION_ADDED_EVENT);
        this.definition = definition;
    }

    // Getters

    public Definition getDefinition() { return this.definition; }

}
