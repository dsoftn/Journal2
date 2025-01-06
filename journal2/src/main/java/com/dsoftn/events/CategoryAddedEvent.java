package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Category;


public class CategoryAddedEvent extends Event {

    public static final EventType<CategoryAddedEvent> CATEGORY_ADDED_EVENT = new EventType<>(Event.ANY, "CATEGORY_ADDED_EVENT");

    private final Category category;

    // Constructor

    public CategoryAddedEvent(Category category) {
        super(CATEGORY_ADDED_EVENT);
        this.category = category;
    }

    // Getters

    public Category getCategory() { return this.category; }

}
