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
 * mutable, and direct edits bypass rules and counters. Undo is deferred to Step 6.
 */
public abstract class SolitaireGame {
    /** Ordered tableau columns owned by the current game. */
    protected final List<CardPile> tableau = new ArrayList<>();
    /** Reserved Step 6 history; currently cleared on reset but never populated. */
    private final Deque<GameSnapshot> undoHistory = new ArrayDeque<>();
    /** Successful player actions since the last reset. */
    private int moveCount;

    /**
     * Clears tableau references, reserved undo history, and the move count.
     * Subclasses call this reset before rebuilding their game-specific state;
     * this base implementation does not create or deal any cards.
     */
    public void startNewGame() {
        tableau.clear();
        undoHistory.clear();
        moveCount = 0;
    }

    /** @return an unmodifiable live list of the mutable tableau piles */
    public List<CardPile> getTableau() { return Collections.unmodifiableList(tableau); }

    /**
     * Transfers a validated top segment in order, exposes the source tableau's
     * remaining top card, counts one move, then invokes {@link #afterMove()}.
     * Rejected moves change neither piles, orientations, nor the move count.
     * @param from source pile owned by this game
     * @param to destination pile owned by this game
     * @param count positive number of top cards to transfer
     * @return true if moved; false if {@link #canMove} rejects the request
     */
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

    /** Counts one successful player action; automatic flips/removals do not count. */
    protected void recordMove() { moveCount++; }
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
     * Declares undo, which is reserved for Step 6 and does not change state.
     * @return never returns normally; undo is not implemented
     * @throws UnsupportedOperationException always, until Step 6
     */
    public boolean undo() {
        throw new UnsupportedOperationException("Undo using GameSnapshot is deferred to Step 6");
    }
    /**
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
