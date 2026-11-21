package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

/** Deterministic fixtures and independent observations; no production snapshots. */
final class GameTestSupport {
    private GameTestSupport() { }

    static Card up(Suit suit, int rank) {
        Card card = new Card(suit, rank);
        card.setFaceUp(true);
        return card;
    }

    static Card up(int rank) { return up(Suit.SPADES, rank); }

    static void set(CardPile pile, Card... cards) {
        pile.removeTopCards(pile.size());
        pile.addCards(List.of(cards));
    }

    static List<CardPile> piles(SolitaireGame game) {
        List<CardPile> piles = new ArrayList<>(game.getTableau());
        if (game instanceof KlondikeGame k) {
            piles.add(k.getStock());
            piles.add(k.getWaste());
            piles.addAll(k.getFoundations());
        } else if (game instanceof SpiderGame s) {
            piles.add(s.getStock());
        }
        return piles;
    }

    // Direct pile edits are limited to arranging positions before an action.
    static void clear(SolitaireGame game) { piles(game).forEach(p -> set(p)); }

    record Observation(List<CardPile> piles, List<List<Card>> cards,
                       List<List<Boolean>> faceUp, int moves, int runs, boolean won) { }

    static Observation observe(SolitaireGame game) {
        List<CardPile> piles = piles(game);
        return new Observation(List.copyOf(piles),
                piles.stream().map(p -> List.copyOf(p.getCards())).toList(),
                piles.stream().map(p -> p.getCards().stream().map(Card::isFaceUp).toList()).toList(),
                game.getMoveCount(), game instanceof SpiderGame s ? s.getCompletedRuns() : 0,
                game.isWon());
    }

    static void rejected(SolitaireGame game, BooleanSupplier action) {
        Observation before = observe(game);
        assertFalse(action.getAsBoolean());
        assertEquals(before, observe(game), "Rejected action must preserve all game state");
    }

    static void rejectedMove(SolitaireGame game, CardPile from, CardPile to, int count) {
        rejected(game, () -> game.canMove(from, to, count));
        rejected(game, () -> game.moveCards(from, to, count));
    }

    static void legalMove(SolitaireGame game, CardPile from, CardPile to, int count) {
        Observation before = observe(game);
        assertTrue(game.canMove(from, to, count));
        assertEquals(before, observe(game), "Validation must be read-only");
        assertTrue(game.moveCards(from, to, count));
        assertEquals(before.moves() + 1, game.getMoveCount());
    }

    static List<Card> run() {
        List<Card> cards = new ArrayList<>();
        for (int rank = 13; rank >= 1; rank--) { cards.add(up(rank)); }
        return cards;
    }

    static void assertInitialTableau(SolitaireGame game, int... sizes) {
        assertEquals(sizes.length, game.getTableau().size());
        for (int i = 0; i < sizes.length; i++) {
            List<Card> cards = game.getTableau().get(i).getCards();
            assertEquals(sizes[i], cards.size());
            for (int j = 0; j < cards.size(); j++) {
                assertEquals(j == cards.size() - 1, cards.get(j).isFaceUp());
            }
        }
    }
}
