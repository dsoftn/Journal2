package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Actor;


public class ActorAddedEvent extends Event {

    public static final EventType<ActorAddedEvent> ACTOR_ADDED_EVENT = new EventType<>(Event.ANY, "ACTOR_ADDED_EVENT");
    
    private final Actor actor;

    // Constructor

    public ActorAddedEvent(Actor actor) {
        super(ACTOR_ADDED_EVENT);
        this.actor = actor;
    }

    // Getters

    public Actor getActor() { return this.actor; }

}
