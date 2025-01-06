package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Category;


public class CategoryUpdatedEvent extends Event {

    public static final EventType<CategoryUpdatedEvent> CATEGORY_UPDATED_EVENT = new EventType<>(Event.ANY, "CATEGORY_UPDATED_EVENT");
    
    private final Category category;

    // Constructor

    public CategoryUpdatedEvent(Category category) {
        super(CATEGORY_UPDATED_EVENT);
        this.category = category;
    }

    // Getters

    public Category getCategory() { return this.category; }

}
