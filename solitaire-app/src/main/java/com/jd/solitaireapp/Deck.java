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
     * Creates an unshuffled, face-down Spider deck with 104 distinct cards.
     * One suit uses eight copies of each Spade; two suits use four copies of
     * each Spade and Heart; four suits use two copies of each rank in every suit.
     * @param suitCount number of suits: 1, 2, or 4
     * @return a new 104-card Spider deck
     * @throws IllegalArgumentException if suitCount is not 1, 2, or 4
     */
    public static Deck createSpiderDeck(int suitCount) {
        Suit[] suits = switch (suitCount) {
            case 1 -> new Suit[]{Suit.SPADES};
            case 2 -> new Suit[]{Suit.SPADES, Suit.HEARTS};
            case 4 -> Suit.values();
            default -> throw new IllegalArgumentException("Spider suit count must be 1, 2, or 4");
        };
        Deck deck = new Deck();
        for (int copy = 0; copy < 8 / suitCount; copy++) {
            for (Suit suit : suits) {
                for (int rank = 1; rank <= 13; rank++) {
                    deck.cards.add(new Card(suit, rank));
                }
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
