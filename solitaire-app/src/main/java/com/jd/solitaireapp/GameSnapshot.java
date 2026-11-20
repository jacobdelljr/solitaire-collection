package com.jd.solitaireapp;

/** UML placeholder only. Snapshot storage and undo are reserved for Step 6 TDD. */
public final class GameSnapshot {
    private GameSnapshot() { }

    public static GameSnapshot capture(SolitaireGame game) {
        throw new UnsupportedOperationException("Snapshot capture is deferred to Step 6");
    }

    public void restore(SolitaireGame game) {
        throw new UnsupportedOperationException("Snapshot restore is deferred to Step 6");
    }
}
