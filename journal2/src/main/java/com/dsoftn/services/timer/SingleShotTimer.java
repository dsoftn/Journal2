package com.dsoftn.services.timer;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ITimer;

public class SingleShotTimer implements ITimer {
    // Variables
    private long startTimeMS;
    private long endTimeMS;
    private long durationMS;
    private Runnable action;
    private boolean active = false;

    // Constructor
    public SingleShotTimer(long durationMS) {
        this.durationMS = durationMS;
    }

    // Interface ITimer methods
    @Override
    public void play(Runnable action) {
        this.action = action;
        active = true;
        OBJECTS.GLOBAL_TIMER.registerTimer(this);
    }

    @Override
    public void stop() {
        active = false;
        OBJECTS.GLOBAL_TIMER.unregisterTimer(this);
    }

    @Override
    public void startMS(long ms) {
        startTimeMS = ms;
        endTimeMS = startTimeMS + durationMS;
    }

    @Override
    public void nowMS(long ms) {
        if (ms >= endTimeMS && active) {
            stop();
            action.run();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

}