package com.dsoftn.events;

import javafx.event.Event;
import javafx.event.EventType;

import com.dsoftn.models.block_types.BlockDiary;


public class BlockTypeUpdatedEvent extends Event {

    public static final EventType<BlockTypeUpdatedEvent> BLOCK_TYPE_UPDATED_EVENT = new EventType<>(Event.ANY, "BLOCK_TYPE_UPDATED_EVENT");

    private final BlockDiary OldBlockDiary;
    private final BlockDiary NewBlockDiary;

    // Constructor
    public BlockTypeUpdatedEvent(BlockDiary oldBlockDiary, BlockDiary newBlockDiary) {
        super(BLOCK_TYPE_UPDATED_EVENT);
        this.OldBlockDiary = oldBlockDiary;
        this.NewBlockDiary = newBlockDiary;
    }

    // Getters
    
    public BlockDiary getOldBlockDiary() { return this.OldBlockDiary; }
    public BlockDiary getNewBlockDiary() { return this.NewBlockDiary; }

}
