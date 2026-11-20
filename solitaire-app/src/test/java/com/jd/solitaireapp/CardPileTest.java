package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class CardPileTest {
    @Test void newPileIsEmptyWithNullTop() {
        CardPile pile = new CardPile();
        assertTrue(pile.isEmpty());
        assertEquals(0, pile.size());
        assertNull(pile.peek());
        assertTrue(pile.getCards().isEmpty());
    }

    @Test void appendCopiesInputListButKeepsCardReferencesAndOrder() {
        CardPile pile = new CardPile();
        Card bottom = new Card(Suit.CLUBS, 1), top = up(2);
        List<Card> input = new ArrayList<>(List.of(bottom, top));
        pile.addCards(input);
        input.clear();
        assertEquals(List.of(bottom, top), pile.getCards());
        assertSame(top, pile.peek());
        assertEquals(2, pile.size());
        assertFalse(pile.isEmpty());
        assertFalse(bottom.isFaceUp());
        assertTrue(top.isFaceUp());
    }

    @Test void viewIsUnmodifiableLiveAndShallow() {
        CardPile pile = new CardPile();
        List<Card> view = pile.getCards();
        Card card = up(1);
        pile.addCards(List.of(card));
        assertEquals(List.of(card), view);
        assertThrows(UnsupportedOperationException.class, () -> view.add(up(2)));
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> view.set(0, up(3)));
        view.getFirst().setFaceUp(false);
        assertFalse(pile.peek().isFaceUp());
        pile.removeTopCards(1);
        assertTrue(view.isEmpty());
    }

    @Test void removesTopSegmentInOrderWithoutFlippingCards() {
        CardPile pile = new CardPile();
        Card a = up(3), b = new Card(Suit.SPADES, 2), c = up(1);
        set(pile, a, b, c);
        List<Card> removed = pile.removeTopCards(2);
        assertEquals(List.of(b, c), removed);
        assertEquals(List.of(a), pile.getCards());
        assertFalse(b.isFaceUp());
        assertTrue(c.isFaceUp());
        removed.clear();
        assertEquals(List.of(a), pile.getCards());
    }

    @Test void removingAllCardsEmptiesPile() {
        CardPile pile = new CardPile();
        Card card = up(1);
        set(pile, card);
        assertEquals(List.of(card), pile.removeTopCards(1));
        assertTrue(pile.isEmpty());
        assertNull(pile.peek());
    }

    @Test void zeroRemovalAndEmptyAppendAreNoOps() {
        CardPile pile = new CardPile();
        assertTrue(pile.removeTopCards(0).isEmpty());
        Card card = up(1);
        set(pile, card);
        pile.addCards(List.of());
        assertTrue(pile.removeTopCards(0).isEmpty());
        assertEquals(List.of(card), pile.getCards());
    }

    @Test void invalidRemovalCountsPreserveState() {
        CardPile pile = new CardPile();
        Card card = up(1);
        set(pile, card);
        for (int count : new int[]{Integer.MIN_VALUE, -1, 2, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> pile.removeTopCards(count));
            assertEquals(List.of(card), pile.getCards());
            assertTrue(card.isFaceUp());
        }
    }

    @Test void nullListOrElementIsRejectedBeforeAnyAppend() {
        CardPile pile = new CardPile();
        Card card = up(1);
        set(pile, card);
        assertThrows(NullPointerException.class, () -> pile.addCards(null));
        assertThrows(NullPointerException.class, () -> pile.addCards(Arrays.asList(up(2), null)));
        assertEquals(List.of(card), pile.getCards());
    }
}
