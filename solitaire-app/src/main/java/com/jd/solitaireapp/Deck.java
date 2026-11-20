package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/** A deck created by a factory, optionally shuffled, and consumed by drawing. */
public class Deck {
    /** Remaining cards; drawing removes the last element. */
    private final List<Card> cards = new ArrayList<>();

    /** Creates empty storage for use by the deck factories. */
    private Deck() { }

    /**
     * Creates one face-down card of each rank in each suit, without shuffling.
     * Cards are stored in suit declaration order, then ascending rank.
     * @return a new 52-card deck
     */
    public static Deck createStandardDeck() {
        Deck deck = new Deck();
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.cards.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    /**
     * Creates an unshuffled, face-down one-suit Spider deck: eight distinct
     * cards of each rank, all Spades. Multi-suit decks are deferred to Step 6.
     * @param suitCount number of suits; only 1 is implemented
     * @return a new 104-card one-suit deck
     * @throws UnsupportedOperationException if suitCount is 2 or 4
     * @throws IllegalArgumentException if suitCount is not 1, 2, or 4
     */
    public static Deck createSpiderDeck(int suitCount) {
        if (suitCount == 2 || suitCount == 4) {
            throw new UnsupportedOperationException("Multi-suit Spider is deferred to Step 6");
        }
        if (suitCount != 1) {
            throw new IllegalArgumentException("Spider suit count must be 1, 2, or 4");
        }
        Deck deck = new Deck();
        for (int copy = 0; copy < 8; copy++) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.cards.add(new Card(Suit.SPADES, rank));
            }
        }
        return deck;
    }

    /** Randomizes remaining card order without changing face orientations. */
    public void shuffle() { Collections.shuffle(cards); }
    /**
     * Removes the last remaining card without changing its orientation.
     * @return the removed card
     * @throws NoSuchElementException if the deck is empty
     */
    public Card draw() {
        if (cards.isEmpty()) { throw new NoSuchElementException("Deck is empty"); }
        return cards.removeLast();
    }
    /** @return the number of cards still in the deck */
    public int size() { return cards.size(); }
}
