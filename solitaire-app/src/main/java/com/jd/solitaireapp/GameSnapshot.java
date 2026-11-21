package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Saved position for one game and its current pile layout. Ordered card lists
 * and face orientations are copied independently of the mutable game. Original
 * card and pile identities are retained so restoration updates existing views;
 * card suits and ranks are immutable and need no separate copies.
 * Undo history itself is managed by {@link SolitaireGame}, not by snapshots.
 */
public final class GameSnapshot {
    /** Game that owns the saved pile and card references. */
    private final SolitaireGame owner;
    /** Immutable ordered pile states, including empty piles. */
    private final List<PileState> piles;
    /** Successful actions at capture time. */
    private final int moveCount;
    /** Removed Spider runs at capture time; zero for other games. */
    private final int completedRuns;

    /** Card identity and an independent copy of its mutable orientation. */
    private record CardState(Card card, boolean faceUp) { }
    /** Pile identity and immutable bottom-to-top card states. */
    private record PileState(CardPile pile, List<CardState> cards) { }

    /** Captures values without modifying the game or its undo history. */
    private GameSnapshot(SolitaireGame game) {
        owner = game;
        piles = gamePiles(game).stream().map(pile -> new PileState(pile,
                pile.getCards().stream().map(card -> new CardState(card, card.isFaceUp()))
                        .toList())).toList();
        moveCount = game.getMoveCount();
        completedRuns = game instanceof SpiderGame spider ? spider.getCompletedRuns() : 0;
    }

    /** Returns the game's piles in a stable order, without retaining a live list. */
    private static List<CardPile> gamePiles(SolitaireGame game) {
        List<CardPile> result = new ArrayList<>(game.getTableau());
        if (game instanceof KlondikeGame klondike) {
            result.add(klondike.getStock());
            result.add(klondike.getWaste());
            result.addAll(klondike.getFoundations());
        } else if (game instanceof SpiderGame spider) {
            result.add(spider.getStock());
        }
        return result;
    }

    /**
     * Saves all piles, orientations and counters without recording an undo entry.
     * @param game source game
     * @return an independent saved position
     */
    public static GameSnapshot capture(SolitaireGame game) {
        return new GameSnapshot(game);
    }

    /**
     * Restores the saved position in place without counting a move, running
     * automatic effects, or changing undo history. The snapshot remains reusable.
     * Configuration fields are immutable; win state is derived from restored
     * piles and counters. A snapshot cannot be applied to another game or after
     * a reset has replaced its piles.
     * @param game original source game with the same pile layout
     * @throws IllegalArgumentException if the game or its pile layout has changed
     */
    public void restore(SolitaireGame game) {
        if (game != owner || !gamePiles(game).equals(piles.stream().map(PileState::pile).toList())) {
            throw new IllegalArgumentException("Snapshot requires its original game and pile layout");
        }
        for (PileState state : piles) {
            state.pile().removeTopCards(state.pile().size());
            state.pile().addCards(state.cards().stream().map(CardState::card).toList());
            state.cards().forEach(card -> card.card().setFaceUp(card.faceUp()));
        }
        game.restoreMoveCount(moveCount);
        if (game instanceof SpiderGame spider) { spider.restoreCompletedRuns(completedRuns); }
    }
}
