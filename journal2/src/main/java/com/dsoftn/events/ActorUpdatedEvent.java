package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Actor;


public class ActorUpdatedEvent extends Event {

    public static final EventType<ActorUpdatedEvent> ACTOR_UPDATED_EVENT = new EventType<>(Event.ANY, "ACTOR_UPDATED_EVENT");

    private final Actor OldBActor;
    private final Actor NewActor;

    // Constructor
    public ActorUpdatedEvent(Actor oldActor, Actor newActor) {
        super(ACTOR_UPDATED_EVENT);
        this.OldBActor = oldActor;
        this.NewActor = newActor;
    }

    // Getters
    
    public Actor getOldBActor() { return this.OldBActor; }

    public Actor getNewActor() { return this.NewActor; }

}
