package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.Block;


public class BlockAddedEvent extends Event {

    public static final EventType<BlockAddedEvent> BLOCK_ADDED_EVENT = new EventType<>(Event.ANY, "BLOCK_ADDED_EVENT");
    
    private final Block block;

    // Constructor

    public BlockAddedEvent(Block block) {
        super(BLOCK_ADDED_EVENT);
        this.block = block;
    }

    // Getters

    public Block getBlock() { return this.block; }

}
