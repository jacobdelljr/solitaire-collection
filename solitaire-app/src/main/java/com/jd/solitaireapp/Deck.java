package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    private Deck() { }

    public static Deck createStandardDeck() {
        Deck deck = new Deck();
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                deck.cards.add(new Card(suit, rank));
            }
        }
        return deck;
    }

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

    public void shuffle() { Collections.shuffle(cards); }
    public Card draw() {
        if (cards.isEmpty()) { throw new NoSuchElementException("Deck is empty"); }
        return cards.removeLast();
    }
    public int size() { return cards.size(); }
}
