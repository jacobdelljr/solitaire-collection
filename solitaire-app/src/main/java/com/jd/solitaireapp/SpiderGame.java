package com.jd.solitaireapp;

import java.util.List;

public class SpiderGame extends SolitaireGame {
    private final int suitCount;
    private final CardPile stock = new CardPile();
    private int completedRuns;

    public SpiderGame(int suitCount) {
        if (suitCount == 2 || suitCount == 4) {
            throw new UnsupportedOperationException("Multi-suit Spider is deferred to Step 6");
        }
        if (suitCount != 1) {
            throw new IllegalArgumentException("Spider suit count must be 1, 2, or 4");
        }
        this.suitCount = suitCount;
        startNewGame();
    }

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

    public CardPile getStock() { return stock; }
    public int getCompletedRuns() { return completedRuns; }

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

    @Override
    protected void afterMove() { removeCompletedRuns(); }

    /** Removes all exposed same-suit King-to-Ace runs, including newly revealed runs. */
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

    @Override
    public boolean isWon() { return completedRuns == 8; }
}
