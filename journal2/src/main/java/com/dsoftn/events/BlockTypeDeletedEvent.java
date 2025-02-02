package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.block_types.BlockDiary;


public class BlockTypeDeletedEvent extends Event {

    public static final EventType<BlockTypeDeletedEvent> BLOCK_TYPE_DELETED = new EventType<>(Event.ANY, "BLOCK_TYPE_DELETED");

    private final BlockDiary blockDiary;

    // Constructor
    public BlockTypeDeletedEvent(BlockDiary blockDiary) {
        super(BLOCK_TYPE_DELETED);
        this.blockDiary = blockDiary;
    }

    // Getters
    
    public BlockDiary getBlockDiary() {
        return blockDiary;
    }

}
