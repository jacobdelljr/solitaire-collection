package com.jd.solitaireapp;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class KlondikeTurnThreeTest {
    private KlondikeGame newGame() {
        return assertDoesNotThrow(() -> new KlondikeGame(3),
                "Turn 3 must be supported rather than deferred to Step 6");
    }

    private KlondikeGame emptyGame() {
        KlondikeGame game = newGame();
        clear(game);
        return game;
    }

    private void assertInitialGame(KlondikeGame game) {
        assertInitialTableau(game, 1, 2, 3, 4, 5, 6, 7);
        assertEquals(24, game.getStock().size());
        assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        assertTrue(game.getWaste().isEmpty());
        assertEquals(4, game.getFoundations().size());
        assertTrue(game.getFoundations().stream().allMatch(CardPile::isEmpty));
        List<Card> cards = piles(game).stream().flatMap(p -> p.getCards().stream()).toList();
        assertEquals(52, cards.size());
        assertEquals(52, new HashSet<>(cards).size());
        assertEquals(52, cards.stream().map(c -> c.getSuit() + ":" + c.getRank()).distinct().count());
        assertEquals(0, game.getMoveCount());
        assertFalse(game.isWon());
    }

    @Test void constructorDealsCompleteTurnThreeGame() {
        assertInitialGame(newGame());
    }

    @Test void drawTurnsThreeCardsOverInStockOrderAndCountsOneMove() {
        KlondikeGame game = emptyGame();
        Card first = new Card(Suit.CLUBS, 4), second = new Card(Suit.HEARTS, 5);
        Card third = new Card(Suit.SPADES, 6), remaining = new Card(Suit.DIAMONDS, 7);
        Card oldWaste = up(8);
        // Piles are bottom-to-top: first is drawn first, third becomes playable.
        set(game.getStock(), remaining, third, second, first);
        set(game.getWaste(), oldWaste);
        assertTrue(game.drawFromStock());
        assertEquals(List.of(remaining), game.getStock().getCards());
        assertEquals(List.of(oldWaste, first, second, third), game.getWaste().getCards());
        assertSame(third, game.getWaste().peek());
        assertTrue(game.getWaste().getCards().stream().allMatch(Card::isFaceUp));
        assertFalse(remaining.isFaceUp());
        assertEquals(1, game.getMoveCount());
    }

    @Test void drawWithOneCardRemainingDrawsItAndCountsOnce() {
        assertShortDraw(1);
    }

    @Test void drawWithTwoCardsRemainingDrawsBothInOrderAndCountsOnce() {
        assertShortDraw(2);
    }

    private void assertShortDraw(int count) {
        KlondikeGame game = emptyGame();
        Card first = new Card(Suit.CLUBS, 4), second = new Card(Suit.HEARTS, 5);
        Card oldWaste = up(9);
        set(game.getStock(), count == 1 ? new Card[]{first} : new Card[]{second, first});
        set(game.getWaste(), oldWaste);
        assertTrue(game.drawFromStock());
        assertTrue(game.getStock().isEmpty());
        assertEquals(count == 1 ? List.of(oldWaste, first) : List.of(oldWaste, first, second),
                game.getWaste().getCards());
        assertTrue(game.getWaste().getCards().stream().allMatch(Card::isFaceUp));
        assertEquals(1, game.getMoveCount());
        rejected(game, game::drawFromStock);
    }

    @Test void onlyWasteTopCanPlayToFoundationAndRemovingItExposesNextCard() {
        KlondikeGame game = emptyGame();
        Card ace = new Card(Suit.HEARTS, 1), two = new Card(Suit.HEARTS, 2);
        Card king = new Card(Suit.CLUBS, 13);
        set(game.getStock(), king, ace, two);
        assertTrue(game.drawFromStock());
        CardPile foundation = game.getFoundations().get(0);
        // The buried Ace cannot play until the King above it is moved.
        rejectedMove(game, game.getWaste(), foundation, 1);
        legalMove(game, game.getWaste(), game.getTableau().get(0), 1);
        assertSame(ace, game.getWaste().peek());
        legalMove(game, game.getWaste(), foundation, 1);
        assertSame(two, game.getWaste().peek());
        legalMove(game, game.getWaste(), foundation, 1);
        assertEquals(List.of(ace, two), foundation.getCards());
        assertTrue(game.getWaste().isEmpty());
        assertEquals(4, game.getMoveCount());
    }

    @Test void wasteCannotMoveAsASequenceEvenWhenTheSequenceFitsTableau() {
        KlondikeGame game = emptyGame();
        Card queen = new Card(Suit.HEARTS, 12), jack = new Card(Suit.CLUBS, 11);
        Card ten = new Card(Suit.DIAMONDS, 10);
        set(game.getStock(), ten, jack, queen);
        set(game.getTableau().get(0), up(Suit.SPADES, 13));
        set(game.getTableau().get(1), up(Suit.SPADES, 11));
        set(game.getTableau().get(2), up(Suit.DIAMONDS, 12));
        assertTrue(game.drawFromStock());
        rejectedMove(game, game.getWaste(), game.getTableau().get(0), 3);
        rejectedMove(game, game.getWaste(), game.getTableau().get(2), 2);
        rejectedMove(game, game.getWaste(), game.getTableau().get(0), 1);
        legalMove(game, game.getWaste(), game.getTableau().get(1), 1);
        assertEquals(List.of(queen, jack), game.getWaste().getCards());
        assertSame(ten, game.getTableau().get(1).peek());
    }

    @Test void emptyStockDrawDoesNotAutomaticallyRecycleOrCountAMove() {
        KlondikeGame game = emptyGame();
        rejected(game, game::drawFromStock);
        set(game.getStock(), new Card(Suit.CLUBS, 6), new Card(Suit.HEARTS, 5),
                new Card(Suit.SPADES, 4));
        assertTrue(game.drawFromStock());
        assertTrue(game.getStock().isEmpty());
        assertEquals(3, game.getWaste().size());
        rejected(game, game::drawFromStock);
        rejected(game, game::drawFromStock);
        assertEquals(1, game.getMoveCount());
    }

    @Test void recyclingRequiresEmptyStockAndNonemptyWaste() {
        KlondikeGame game = emptyGame();
        rejected(game, game::recycleStock);
        set(game.getStock(), new Card(Suit.CLUBS, 4));
        rejected(game, game::recycleStock);
        set(game.getWaste(), up(5));
        rejected(game, game::recycleStock);
    }

    @Test void repeatedRecyclingPreservesFullDrawOrderAndCountsEachActionOnce() {
        KlondikeGame game = emptyGame();
        Card a = new Card(Suit.CLUBS, 4), b = new Card(Suit.HEARTS, 5);
        Card c = new Card(Suit.SPADES, 6), d = new Card(Suit.DIAMONDS, 7);
        Card e = new Card(Suit.CLUBS, 8), f = new Card(Suit.HEARTS, 9);
        Card g = new Card(Suit.SPADES, 10);
        List<Card> drawOrder = List.of(a, b, c, d, e, f, g);
        set(game.getStock(), g, f, e, d, c, b, a);
        int moves = 0;
        for (int cycle = 0; cycle < 5; cycle++) {
            for (int drawn : new int[]{3, 6, 7}) {
                assertTrue(game.drawFromStock());
                assertEquals(drawOrder.subList(0, drawn), game.getWaste().getCards());
                assertEquals(List.of(g, f, e, d, c, b, a).subList(0, 7 - drawn),
                        game.getStock().getCards());
                assertTrue(game.getWaste().getCards().stream().allMatch(Card::isFaceUp));
                assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
                assertEquals(++moves, game.getMoveCount());
            }
            rejected(game, game::drawFromStock);
            assertTrue(game.recycleStock());
            assertEquals(++moves, game.getMoveCount());
            assertTrue(game.getWaste().isEmpty());
            assertEquals(List.of(g, f, e, d, c, b, a), game.getStock().getCards());
            assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
            rejected(game, game::recycleStock);
        }
    }

    @Test void recyclingExcludesPlayedCardsAndRegroupsRemainingCardsInOrder() {
        KlondikeGame game = emptyGame();
        Card a = new Card(Suit.CLUBS, 4), b = new Card(Suit.HEARTS, 5);
        Card ace = new Card(Suit.SPADES, 1), d = new Card(Suit.DIAMONDS, 7);
        set(game.getStock(), d, ace, b, a);
        assertTrue(game.drawFromStock());
        legalMove(game, game.getWaste(), game.getFoundations().get(0), 1);
        assertTrue(game.drawFromStock());
        assertTrue(game.recycleStock());
        assertEquals(List.of(d, b, a), game.getStock().getCards());
        assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        assertTrue(game.getWaste().isEmpty());
        assertTrue(game.drawFromStock());
        assertEquals(List.of(a, b, d), game.getWaste().getCards());
        assertTrue(game.getStock().isEmpty());
        assertEquals(List.of(ace), game.getFoundations().get(0).getCards());
        assertTrue(ace.isFaceUp());
        assertEquals(5, game.getMoveCount());
    }

    @Test void resetRestoresInitialStateAndRetainsTurnThreeDrawing() {
        KlondikeGame game = emptyGame();
        Card ace = new Card(Suit.SPADES, 1);
        set(game.getStock(), ace, new Card(Suit.HEARTS, 5), new Card(Suit.CLUBS, 4));
        assertTrue(game.drawFromStock());
        legalMove(game, game.getWaste(), game.getFoundations().get(0), 1);
        assertTrue(game.recycleStock());
        assertTrue(game.drawFromStock());
        game.startNewGame();
        assertInitialGame(game);
        // Check the retained mode using known cards, independently of the new shuffle.
        clear(game);
        Card first = new Card(Suit.CLUBS, 6), second = new Card(Suit.HEARTS, 7);
        Card third = new Card(Suit.SPADES, 8);
        set(game.getStock(), third, second, first);
        assertTrue(game.drawFromStock());
        assertEquals(List.of(first, second, third), game.getWaste().getCards());
        assertTrue(game.getStock().isEmpty());
        assertEquals(1, game.getMoveCount());
    }
}
