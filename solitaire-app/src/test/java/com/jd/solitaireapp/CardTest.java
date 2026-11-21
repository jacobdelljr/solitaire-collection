package com.jd.solitaireapp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    @Test void allSuitsAndRanksCreateFaceDownCardsWithCorrectColor() {
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                Card card = new Card(suit, rank);
                assertSame(suit, card.getSuit());
                assertEquals(rank, card.getRank());
                assertFalse(card.isFaceUp());
                assertEquals(suit == Suit.HEARTS || suit == Suit.DIAMONDS, card.isRed());
            }
        }
    }

    @Test void orientationCanBeSetRepeatedlyAndReversedWithoutChangingIdentity() {
        Card card = new Card(Suit.CLUBS, 13);
        card.setFaceUp(true);
        card.setFaceUp(true);
        assertTrue(card.isFaceUp());
        card.setFaceUp(false);
        assertFalse(card.isFaceUp());
        assertEquals(13, card.getRank());
        assertSame(Suit.CLUBS, card.getSuit());
    }

    @Test void rejectsOutOfRangeRanks() {
        for (int rank : new int[]{Integer.MIN_VALUE, -1, 0, 14, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new Card(Suit.SPADES, rank));
        }
    }

    @Test void rejectsNullSuit() {
        assertThrows(NullPointerException.class, () -> new Card(null, 1));
    }
}
