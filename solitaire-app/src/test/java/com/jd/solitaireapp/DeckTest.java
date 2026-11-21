package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private List<Card> drain(Deck deck) {
        List<Card> cards = new ArrayList<>();
        while (deck.size() > 0) { cards.add(deck.draw()); }
        return cards;
    }

    @Test void standardDeckHasOneOfEveryRankAndSuit() {
        Deck deck = Deck.createStandardDeck();
        assertEquals(52, deck.size());
        List<Card> cards = drain(deck);
        assertEquals(52, new HashSet<>(cards).size());
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                int expectedRank = rank;
                assertEquals(1, cards.stream().filter(c -> c.getSuit() == suit && c.getRank() == expectedRank).count());
            }
        }
        assertTrue(cards.stream().noneMatch(Card::isFaceUp));
    }

    @Test void unshuffledStandardDeckDrawsInReverseCreationOrder() {
        Deck deck = Deck.createStandardDeck();
        Suit[] suits = Suit.values();
        for (int i = suits.length - 1; i >= 0; i--) {
            for (int rank = 13; rank >= 1; rank--) {
                int size = deck.size();
                Card card = deck.draw();
                assertSame(suits[i], card.getSuit());
                assertEquals(rank, card.getRank());
                assertEquals(size - 1, deck.size());
            }
        }
    }

    @Test void spiderDeckHasEightDistinctSpadesOfEveryRank() {
        Deck deck = Deck.createSpiderDeck(1);
        assertEquals(104, deck.size());
        List<Card> cards = drain(deck);
        assertEquals(104, new HashSet<>(cards).size());
        assertTrue(cards.stream().allMatch(c -> c.getSuit() == Suit.SPADES && !c.isFaceUp()));
        for (int rank = 1; rank <= 13; rank++) {
            int expectedRank = rank;
            assertEquals(8, cards.stream().filter(c -> c.getRank() == expectedRank).count());
        }
    }

    @Test void shufflePreservesCompositionWithoutRequiringAnOrderChange() {
        Deck deck = Deck.createStandardDeck();
        deck.shuffle();
        List<Card> cards = drain(deck);
        assertEquals(52, cards.size());
        assertEquals(52, new HashSet<>(cards).size());
        assertEquals(52, cards.stream().map(c -> c.getSuit() + ":" + c.getRank()).distinct().count());
        assertTrue(cards.stream().noneMatch(Card::isFaceUp));
    }

    @Test void shuffleOnlyIncludesRemainingCardsAndHandlesEmptyDeck() {
        Deck deck = Deck.createStandardDeck();
        Card removed = deck.draw();
        deck.shuffle();
        List<Card> remaining = drain(deck);
        assertEquals(51, remaining.size());
        assertFalse(remaining.contains(removed));
        assertFalse(remaining.stream().anyMatch(c -> c.getSuit() == removed.getSuit() && c.getRank() == removed.getRank()));
        assertDoesNotThrow(deck::shuffle);
        assertEquals(0, deck.size());
        assertThrows(NoSuchElementException.class, deck::draw);
        assertEquals(0, deck.size());
    }

    @Test void separateFactoriesCreateIndependentCards() {
        Card first = Deck.createStandardDeck().draw();
        Card second = Deck.createStandardDeck().draw();
        assertNotSame(first, second);
        first.setFaceUp(true);
        assertFalse(second.isFaceUp());
    }

    @Test void invalidSpiderSuitCountsAreRejected() {
        for (int count : new int[]{-1, 0, 3, 5, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> Deck.createSpiderDeck(count));
        }
    }

    @Test void multiSuitSpiderDecksUseTheConfiguredSuits() {
        for (int count : new int[]{2, 4}) {
            List<Card> cards = drain(Deck.createSpiderDeck(count));
            assertEquals(104, cards.size());
            for (Suit suit : Suit.values()) {
                int expected = count == 4 ? 26
                        : (suit == Suit.SPADES || suit == Suit.HEARTS ? 52 : 0);
                assertEquals(expected, cards.stream().filter(c -> c.getSuit() == suit).count());
            }
        }
    }
}
