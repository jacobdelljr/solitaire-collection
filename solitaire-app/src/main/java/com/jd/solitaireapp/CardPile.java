package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cards are stored bottom to top; the final element is the exposed top. */
public class CardPile {
    private final List<Card> cards = new ArrayList<>();

    public Card peek() { return isEmpty() ? null : cards.getLast(); }
    public int size() { return cards.size(); }
    public boolean isEmpty() { return cards.isEmpty(); }
    public List<Card> getCards() { return Collections.unmodifiableList(cards); }

    public void addCards(List<Card> cards) {
        // Validate and copy the whole input before changing the pile.
        this.cards.addAll(List.copyOf(cards));
    }

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
