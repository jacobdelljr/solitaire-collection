package com.jd.solitaireapp;

import java.util.List;

/**
 * One-, two-, or four-suit Spider with 104 cards, ten tableau columns, and stock row deals.
 * Exposed descending same-suit segments can move; completed King-to-Ace runs
 * are removed automatically after transfers and row deals. Eight removed runs
 * wins. Inherited undo remains deferred.
 */
public class SpiderGame extends SolitaireGame {
    /** Configured suit count: 1, 2, or 4; preserved across resets. */
    private final int suitCount;
    /** Remaining face-down cards, dealt ten at a time. */
    private final CardPile stock = new CardPile();
    /** Number of complete 13-card runs removed since the last reset. */
    private int completedRuns;

    /**
     * Creates and deals a shuffled Spider game with the configured suits.
     * @param suitCount number of suits: 1, 2, or 4
     * @throws IllegalArgumentException if suitCount is not 1, 2, or 4
     */
    public SpiderGame(int suitCount) {
        if (suitCount != 1 && suitCount != 2 && suitCount != 4) {
            throw new IllegalArgumentException("Spider suit count must be 1, 2, or 4");
        }
        this.suitCount = suitCount;
        startNewGame();
    }

    /**
     * Resets shared state and completed runs, then deals a shuffled 104-card deck.
     * Creates ten new columns: six cards in each of the first four, five in each
     * remaining column, with only their tops face up. The other 50 cards form
     * the face-down stock. Previous tableau pile objects no longer belong to this game.
     */
    @Override
    public void startNewGame() {
        super.startNewGame();
        stock.removeTopCards(stock.size());
        completedRuns = 0;
        Deck deck = Deck.createSpiderDeck(suitCount);
        deck.shuffle();
        for (int column = 0; column < 10; column++) { tableau.add(new CardPile()); }
        for (int index = 0; index < 54; index++) {
            tableau.get(index % 10).addCards(List.of(deck.draw()));
        }
        tableau.forEach(pile -> pile.peek().setFaceUp(true));
        while (deck.size() > 0) { stock.addCards(List.of(deck.draw())); }
    }

    /** @return the mutable stock pile; use {@link #dealNextRow()} for legal dealing */
    public CardPile getStock() { return stock; }
    /** @return the number of complete King-to-Ace runs removed since reset */
    public int getCompletedRuns() { return completedRuns; }

    /**
     * Deals one face-up stock card to each column in tableau order, counts one
     * move, and removes any completed runs. Every column must be nonempty.
     * @return true if dealt; false without mutation if fewer than ten stock
     *         cards remain or any tableau column is empty
     */
    public boolean dealNextRow() {
        if (stock.size() < 10 || tableau.stream().anyMatch(CardPile::isEmpty)) { return false; }
        for (CardPile pile : tableau) {
            List<Card> dealt = stock.removeTopCards(1);
            dealt.getFirst().setFaceUp(true);
            pile.addCards(dealt);
        }
        recordMove();
        removeCompletedRuns();
        return true;
    }

    /**
     * Checks a tableau transfer without mutation. The moved segment must be
     * entirely face up, descend by one rank per card, and share one suit.
     * Its bottom card may enter any empty column or lie on a face-up card one
     * rank higher; the destination's suit is not checked. Stock cannot be moved
     * through this method.
     * @param from source tableau pile of this game
     * @param to destination tableau pile of this game
     * @param count positive number of top cards to transfer
     * @return true if legal; false for rule violations, null/foreign/identical
     *         piles, or a count outside the source's available cards
     */
    @Override
    public boolean canMove(CardPile from, CardPile to, int count) {
        if (from == null || to == null || from == to || !tableau.contains(from)
                || !tableau.contains(to) || count <= 0 || count > from.size()) {
            return false;
        }
        List<Card> cards = from.getCards();
        int start = cards.size() - count;
        Card first = cards.get(start);
        for (int i = start; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (!card.isFaceUp() || card.getSuit() != first.getSuit()
                    || card.getRank() != first.getRank() - (i - start)) { return false; }
        }
        Card top = to.peek();
        return top == null || (top.isFaceUp() && top.getRank() == first.getRank() + 1);
    }

    /** Removes completed runs after a successful transfer without counting another move. */
    @Override
    protected void afterMove() { removeCompletedRuns(); }

    /**
     * Removes exposed, same-suit King-to-Ace top segments from every column.
     * Each removal increments completed runs and exposes the remaining top card;
     * that column is checked again for another run. Does not increment move count.
     * @return number of runs removed by this call, not the cumulative total
     */
    public int removeCompletedRuns() {
        int removed = 0;
        for (CardPile pile : tableau) {
            while (hasCompletedRun(pile)) {
                pile.removeTopCards(13);
                completedRuns++;
                removed++;
                if (!pile.isEmpty()) { pile.peek().setFaceUp(true); }
            }
        }
        return removed;
    }

    /**
     * Checks the top thirteen cards without changing the pile.
     * @param pile non-null tableau pile to inspect
     * @return true if its top segment is face-up, same-suit, and ordered King to Ace
     */
    private boolean hasCompletedRun(CardPile pile) {
        if (pile.size() < 13) { return false; }
        List<Card> cards = pile.getCards();
        int start = cards.size() - 13;
        Suit suit = cards.get(start).getSuit();
        for (int i = 0; i < 13; i++) {
            Card card = cards.get(start + i);
            if (!card.isFaceUp() || card.getSuit() != suit || card.getRank() != 13 - i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the run counter, assuming play has preserved the 104-card game.
     * Does not inspect the remaining tableau or stock.
     * @return true if exactly eight runs have been removed
     */
    @Override
    public boolean isWon() { return completedRuns == 8; }
}
