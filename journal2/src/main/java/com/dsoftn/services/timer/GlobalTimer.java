package com.dsoftn.services.timer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dsoftn.OBJECTS;
import com.dsoftn.Interfaces.ITimer;
import com.dsoftn.utils.UError;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GlobalTimer {
    // Variables
    private Timeline timeline;
    private List<WeakReference<ITimer>> timers = new ArrayList<>();

    // Constructor
    public GlobalTimer() {
        timeline = new Timeline(new KeyFrame(Duration.millis(OBJECTS.SETTINGS.getvINTEGER("GlobalTimerTickIntervalMS")), e -> onTick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // Public methods
    public void registerTimer(ITimer timer) {
        timer.startMS(System.currentTimeMillis());
        WeakReference<ITimer> newTimerToAdd = new WeakReference<>(timer);
        if (!timers.contains(newTimerToAdd)) {
            timers.add(new WeakReference<>(timer));
        }
    }

    public void unregisterTimer(ITimer timer) {
        try {
            timers.removeIf(t -> t.get() == timer);
        } catch (Exception e) {
            UError.exception("GlobalTimer.unregisterTimer: Failed to unregister timer", e);
        }
    }

    public long getCurrentTimeMS() {
        return System.currentTimeMillis();
    }

    // Private methods
    private void onTick() {
        Iterator<WeakReference<ITimer>> iterator = timers.iterator();
        while (iterator.hasNext()) {
            ITimer t = iterator.next().get();
            if (t != null) {
                if (t.isActive()) {
                    t.nowMS(System.currentTimeMillis());
                }
            } else {
                iterator.remove();
            }
        }
    }
}
