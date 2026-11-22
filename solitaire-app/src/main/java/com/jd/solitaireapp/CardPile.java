package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A mutable pile storing cards bottom-to-top, with the last element on top.
 * The implicit public constructor creates an empty pile. Pile operations do
 * not enforce game rules, card ownership, uniqueness, or face orientation.
 */
public class CardPile {
    /** Card references in bottom-to-top order. */
    private final List<Card> cards = new ArrayList<>();

    /**
     * Inspects the last card in bottom-to-top order without removing it.
     * @return the top card without removal, or null if empty
     */
    public Card peek() { return isEmpty() ? null : cards.getLast(); }
    /**
     * Counts the card references currently stored in this pile.
     * @return the number of cards in the pile
     */
    public int size() { return cards.size(); }
    /**
     * Checks whether the pile has any cards.
     * @return true if the pile contains no cards
     */
    public boolean isEmpty() { return cards.isEmpty(); }
    /**
     * Returns a live view; later pile changes are visible through it.
     * @return an unmodifiable bottom-to-top list containing mutable card references
     */
    public List<Card> getCards() { return Collections.unmodifiableList(cards); }

    /**
     * Appends card references in the supplied order, with the last card on top.
     * An empty list has no effect; all input is validated before mutation.
     * @param cards cards to append, in bottom-to-top order
     * @throws NullPointerException if the list or any element is null
     */
    public void addCards(List<Card> cards) {
        this.cards.addAll(List.copyOf(cards));
    }

    /**
     * Removes a top segment without changing card orientation or relative order.
     * @param count number to remove; zero leaves the pile unchanged
     * @return a new mutable list of removed card references, bottom-to-top
     * @throws IllegalArgumentException if count is negative or exceeds the pile
     *         size; the pile remains unchanged
     */
    public List<Card> removeTopCards(int count) {
        if (count < 0 || count > size()) {
            throw new IllegalArgumentException("Count must be between zero and pile size");
        }
        int start = size() - count;
        List<Card> removed = new ArrayList<>(cards.subList(start, size()));
        cards.subList(start, size()).clear();
        return removed;
    }
}
