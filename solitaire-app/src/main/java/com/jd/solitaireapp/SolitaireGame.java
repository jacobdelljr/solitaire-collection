package com.jd.solitaireapp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public abstract class SolitaireGame {
    protected final List<CardPile> tableau = new ArrayList<>();
    // Reserved for Step 6; no snapshots are captured yet.
    private final Deque<GameSnapshot> undoHistory = new ArrayDeque<>();
    private int moveCount;

    public void startNewGame() {
        tableau.clear();
        undoHistory.clear();
        moveCount = 0;
    }

    public List<CardPile> getTableau() { return Collections.unmodifiableList(tableau); }

    public boolean moveCards(CardPile from, CardPile to, int count) {
        if (!canMove(from, to, count)) { return false; }
        to.addCards(from.removeTopCards(count));
        if (tableau.contains(from) && !from.isEmpty()) {
            from.peek().setFaceUp(true);
        }
        recordMove();
        afterMove();
        return true;
    }

    protected void recordMove() { moveCount++; }
    protected void afterMove() { }
    public abstract boolean canMove(CardPile from, CardPile to, int count);
    public boolean undo() {
        throw new UnsupportedOperationException("Undo using GameSnapshot is deferred to Step 6");
    }
    public int getMoveCount() { return moveCount; }
    public abstract boolean isWon();
}
