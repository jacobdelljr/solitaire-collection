package com.jd.solitaireapp;

import java.util.Objects;

/** A playing card with fixed suit and rank and a mutable face orientation. */
public class Card {
    /** The card's fixed suit. */
    private final Suit suit;
    /** Rank from 1 (Ace) to 13 (King). */
    private final int rank;
    /** Whether the card's face is exposed. */
    private boolean faceUp;

    /**
     * Creates a face-down card.
     * @param suit the card's suit
     * @param rank rank from 1 (Ace) through 13 (King)
     * @throws NullPointerException if suit is null
     * @throws IllegalArgumentException if rank is outside 1–13
     */
    public Card(Suit suit, int rank) {
        this.suit = Objects.requireNonNull(suit, "Suit is required");
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("Rank must be 1 (Ace) through 13 (King)");
        }
        this.rank = rank;
    }

    /** @return the fixed suit */
    public Suit getSuit() { return suit; }
    /** @return the rank, with Ace equal to 1 and King equal to 13 */
    public int getRank() { return rank; }
    /** @return true if the card is face up */
    public boolean isFaceUp() { return faceUp; }
    /**
     * Sets orientation without validating any game rule.
     * @param faceUp true to expose the face, false to hide it
     */
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }
    /** @return true for Hearts or Diamonds, false for Clubs or Spades */
    public boolean isRed() { return suit == Suit.DIAMONDS || suit == Suit.HEARTS; }
}
