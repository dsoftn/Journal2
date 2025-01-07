package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Category;


public class CategoryUpdatedEvent extends Event {

    public static final EventType<CategoryUpdatedEvent> CATEGORY_UPDATED_EVENT = new EventType<>(Event.ANY, "CATEGORY_UPDATED_EVENT");
    
    private final Category oldCategory;
    private final Category newCategory;

    // Constructor

    public CategoryUpdatedEvent(Category oldCategory, Category newCategory) {
        super(CATEGORY_UPDATED_EVENT);
        this.oldCategory = oldCategory;
        this.newCategory = newCategory;
    }

    // Getters

    public Category getOldCategory() { return this.oldCategory; }

    public Category getNewCategory() { return this.newCategory; }

}
