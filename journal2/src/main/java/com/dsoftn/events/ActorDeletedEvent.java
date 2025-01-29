package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Actor;


public class ActorDeletedEvent extends Event {

    public static final EventType<ActorDeletedEvent> ACTOR_DELETED_EVENT = new EventType<>(Event.ANY, "ACTOR_DELETED_EVENT");

    private final Actor actor;

    // Constructor
    public ActorDeletedEvent(Actor actor) {
        super(ACTOR_DELETED_EVENT);
        this.actor = actor;
    }

    // Getters
    public Actor getActor() { return this.actor; }

}
