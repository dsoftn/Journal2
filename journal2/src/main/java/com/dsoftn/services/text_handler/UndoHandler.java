package com.dsoftn.services.text_handler;

import java.util.ArrayList;
import java.util.List;

import com.dsoftn.services.RTWText;
import com.dsoftn.services.RTWidget;

public class UndoHandler {
    private record Snapshot(
        String styledText,
        int selectionStart,
        int selectionEnd,
        int caretPosition
    ) {}

    // Variables
    List<Snapshot> undoSnapshots = new ArrayList<>();
    List<Snapshot> redoSnapshots = new ArrayList<>();

    // Constructor
    public UndoHandler() {}

    // Public methods
    public void addSnapshot(RTWidget rtwWidget) {
        if (!rtwWidget.isStateChanged()) {
            return;
        }

        RTWText rtwText = new RTWText(rtwWidget);

        if (undoSnapshots.size() > 0) {
            if (rtwText.getStyledText().equals(undoSnapshots.get(undoSnapshots.size() - 1).styledText)) {
                rtwWidget.setStateChanged(false);
                return;
            }
        }

        Snapshot snapshot = new Snapshot(
            rtwText.getStyledText(),
            rtwWidget.getSelection().getStart(),
            rtwWidget.getSelection().getEnd(),
            rtwWidget.getCaretPosition()
        );

        undoSnapshots.add(snapshot);
        redoSnapshots.clear();
        rtwWidget.setStateChanged(false);
    }

    public void undo(RTWidget rtwWidget) {
        if (undoSnapshots.size() == 0) {
            return;
        }

        Snapshot snapshotToRestore = undoSnapshots.get(undoSnapshots.size() - 1);

        undoSnapshots.remove(undoSnapshots.size() - 1);
        redoSnapshots.add(snapshotToRestore);

        if (undoSnapshots.size() == 0) {
            return;
        }

        snapshotToRestore = undoSnapshots.get(undoSnapshots.size() - 1);

        RTWText rtwText = new RTWText(snapshotToRestore.styledText);
        
        rtwWidget.setRTWTextObject(rtwText);

        rtwWidget.moveTo(snapshotToRestore.caretPosition);
        if (snapshotToRestore.selectionStart != snapshotToRestore.selectionEnd) {
            rtwWidget.selectRange(snapshotToRestore.selectionStart, snapshotToRestore.selectionEnd);
        }
        rtwWidget.setCurrentCaretPos(snapshotToRestore.caretPosition);
        rtwWidget.requestFocus();
    }

    public void redo(RTWidget rtwWidget) {
        if (redoSnapshots.size() == 0) {
            return;
        }

        Snapshot snapshotToRestore = redoSnapshots.get(redoSnapshots.size() - 1);

        redoSnapshots.remove(redoSnapshots.size() - 1);
        undoSnapshots.add(snapshotToRestore);

        RTWText rtwText = new RTWText(snapshotToRestore.styledText);

        rtwWidget.setRTWTextObject(rtwText);
        
        rtwWidget.moveTo(snapshotToRestore.caretPosition);
        if (snapshotToRestore.selectionStart != snapshotToRestore.selectionEnd) {
            rtwWidget.selectRange(snapshotToRestore.selectionStart, snapshotToRestore.selectionEnd);
        }
        rtwWidget.setCurrentCaretPos(snapshotToRestore.caretPosition);
        rtwWidget.requestFocus();
    }

    public boolean canUndo() {
        return undoSnapshots.size() > 1;
    }

    public boolean canRedo() {
        return redoSnapshots.size() > 0;
    }

}
