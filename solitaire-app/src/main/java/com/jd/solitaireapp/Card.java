package com.jd.solitaireapp;

import java.util.Objects;

public class Card {
    private final Suit suit;
    private final int rank;
    private boolean faceUp;

    public Card(Suit suit, int rank) {
        this.suit = Objects.requireNonNull(suit, "Suit is required");
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("Rank must be 1 (Ace) through 13 (King)");
        }
        this.rank = rank;
    }

    public Suit getSuit() { return suit; }
    public int getRank() { return rank; }
    public boolean isFaceUp() { return faceUp; }
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }
    public boolean isRed() { return suit == Suit.DIAMONDS || suit == Suit.HEARTS; }
}
