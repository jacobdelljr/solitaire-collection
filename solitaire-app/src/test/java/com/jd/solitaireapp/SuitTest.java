package com.jd.solitaireapp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuitTest {
    @Test void containsExactlyTheFourDocumentedSuitsInDeclarationOrder() {
        assertArrayEquals(new Suit[]{Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES}, Suit.values());
    }

    @Test void namesRoundTripToTheirSuit() {
        for (Suit suit : Suit.values()) { assertSame(suit, Suit.valueOf(suit.name())); }
    }
}
