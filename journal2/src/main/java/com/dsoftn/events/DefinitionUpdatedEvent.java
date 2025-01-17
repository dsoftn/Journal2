package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Definition;


public class DefinitionUpdatedEvent extends Event {

    public static final EventType<DefinitionUpdatedEvent> DEFINITION_UPDATED_EVENT = new EventType<>(Event.ANY, "DEFINITION_UPDATED_EVENT");
    
    private final Definition newDefinition;
    private final Definition oldDefinition;

    // Constructor

    public DefinitionUpdatedEvent(Definition oldDefinition, Definition newDefinition) {
        super(DEFINITION_UPDATED_EVENT);
        this.newDefinition = newDefinition;
        this.oldDefinition = oldDefinition;
    }

    // Getters

    public Definition getNewDefinition() { return this.newDefinition; }
    
    public Definition getOldDefinition() { return this.oldDefinition; }

}
