package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Category;


public class CategoryDeletedEvent extends Event {

    public static final EventType<CategoryDeletedEvent> CATEGORY_DELETED_EVENT = new EventType<>(Event.ANY, "CATEGORY_DELETED_EVENT");

    private final Category category;

    // Constructor

    public CategoryDeletedEvent(Category category) {
        super(CATEGORY_DELETED_EVENT);
        this.category = category;    
    }

    // Getters

    public Category getCategory() { return category; }

}
