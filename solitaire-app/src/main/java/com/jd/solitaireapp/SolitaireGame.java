package com.jd.solitaireapp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * JavaFX-independent base for tableau games, shared transfers, and move counting.
 * The implicit constructor creates empty shared state; concrete games deal their
 * own layouts. Clients should use game operations: exposed piles and cards remain
 * mutable, and direct edits bypass rules, counters and undo recording.
 */
public abstract class SolitaireGame {
    /** Ordered tableau columns owned by the current game. */
    protected final List<CardPile> tableau = new ArrayList<>();
    /** Positions before successful actions, with the most recent on top. */
    private final Deque<GameSnapshot> undoHistory = new ArrayDeque<>();
    /** Successful player actions since the last reset. */
    private int moveCount;

    /**
     * Clears tableau references, undo history, and the move count.
     * Subclasses call this reset before rebuilding their game-specific state;
     * this base implementation does not create or deal any cards.
     */
    public void startNewGame() {
        tableau.clear();
        undoHistory.clear();
        moveCount = 0;
    }

    /**
     * Exposes the current tableau in column order.
     * @return an unmodifiable live list of the mutable tableau piles
     */
    public List<CardPile> getTableau() { return Collections.unmodifiableList(tableau); }

    /**
     * Transfers a validated top segment in order, exposes the source tableau's
     * remaining top card, counts one move, then invokes {@link #afterMove()}.
     * Each successful transfer saves one undo entry including automatic effects.
     * Rejected moves leave piles, orientations, counters, and history unchanged.
     * @param from source pile owned by this game
     * @param to destination pile owned by this game
     * @param count positive number of top cards to transfer
     * @return true if moved; false if {@link #canMove(CardPile, CardPile, int)} rejects the request
     */
    public boolean moveCards(CardPile from, CardPile to, int count) {
        if (!canMove(from, to, count)) { return false; }
        saveUndoState();
        to.addCards(from.removeTopCards(count));
        if (tableau.contains(from) && !from.isEmpty()) {
            from.peek().setFaceUp(true);
        }
        recordMove();
        afterMove();
        return true;
    }

    /** Counts one successful player action; automatic flips/removals do not count. */
    protected void recordMove() { moveCount++; }
    /** Saves one position after validation and before a successful action mutates state. */
    protected void saveUndoState() { undoHistory.push(GameSnapshot.capture(this)); }
    /**
     * Restores the counter without recording an action; used only by snapshots.
     * @param moveCount saved successful-action count
     */
    void restoreMoveCount(int moveCount) { this.moveCount = moveCount; }
    /** Hook after a successful counted transfer; the base implementation does nothing. */
    protected void afterMove() { }
    /**
     * Checks a proposed top-segment transfer without modifying game state.
     * Implementations reject null, foreign, or identical piles, nonpositive or
     * excessive counts, face-down moved cards, and game-specific rule violations.
     * @param from proposed source pile
     * @param to proposed destination pile
     * @param count proposed number of top cards
     * @return true if the transfer is legal in the current position
     */
    public abstract boolean canMove(CardPile from, CardPile to, int count);
    /**
     * Checks pickup restrictions without requiring a destination or mutating state.
     * Subclasses opt into pickup support by overriding this method.
     * @param from source pile owned by this game
     * @param count number of top cards to pick up
     * @return whether the source segment may be picked up; false in this base implementation
     */
    public boolean canPickUp(CardPile from, int count) { return false; }
    /**
     * Restores the position before the latest successful action, reversing its
     * transfer or stock operation and any automatic flips and removals. Does not count a move or create new history.
     * @return true if restored; false without mutation if history is empty
     */
    public boolean undo() {
        if (undoHistory.isEmpty()) { return false; }
        undoHistory.peek().restore(this);
        undoHistory.pop();
        return true;
    }
    /**
     * Counts successful player actions still represented in the current position.
     * Undo restores the prior count; automatic effects do not count separately.
     * @return successful transfers, stock draws, recycles, and row deals since
     *         the last reset, excluding automatic flips and run removals
     */
    public int getMoveCount() { return moveCount; }
    /**
     * Checks the game's completion condition without modifying state.
     * Assumes piles were maintained through legal game operations.
     * @return true if the game's implemented win condition is satisfied
     */
    public abstract boolean isWon();
}
