package com.jd.solitaireapp;

/**
 * Placeholder for the UML snapshot design, reserved for Step 6 TDD.
 * No pile, orientation, move-count, or completed-run state is stored yet;
 * capture and restore always throw without inspecting or changing the game.
 */
public final class GameSnapshot {
    /** Prevents direct construction; future snapshots will use capture. */
    private GameSnapshot() { }

    /**
     * Declares the future snapshot capture operation; currently unsupported.
     * @param game intended source game, currently unused
     * @return never returns normally; no snapshot is created
     * @throws UnsupportedOperationException always, until Step 6
     */
    public static GameSnapshot capture(SolitaireGame game) {
        throw new UnsupportedOperationException("Snapshot capture is deferred to Step 6");
    }

    /**
     * Declares the future snapshot restoration operation; currently unsupported.
     * @param game intended destination game, currently unused
     * @throws UnsupportedOperationException always, until Step 6
     */
    public void restore(SolitaireGame game) {
        throw new UnsupportedOperationException("Snapshot restore is deferred to Step 6");
    }
}
