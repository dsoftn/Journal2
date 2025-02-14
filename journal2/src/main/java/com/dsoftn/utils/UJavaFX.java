package com.dsoftn.utils;

import com.dsoftn.OBJECTS;
import com.dsoftn.enums.models.TaskStateEnum;
import com.dsoftn.events.TaskStateEvent;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class UJavaFX {

    public static String getUniqueId() {
        return System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString();
    }

    public static void taskStart(Runnable runnableToExecute, String eventID) {
        // Start new task
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                runnableToExecute.run();
                return null;
            }
        };

        // When task is finished send event
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        eventID,
                        TaskStateEnum.COMPLETED,
                        ""
                ));
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                OBJECTS.EVENT_HANDLER.fireEvent(
                    new TaskStateEvent(
                        eventID,
                        TaskStateEnum.FAILED,
                        "Task.setOnFailed exception: " + e
                    )
                );
            });
        });

        // Start task
        new Thread(task).start();

    }
}
