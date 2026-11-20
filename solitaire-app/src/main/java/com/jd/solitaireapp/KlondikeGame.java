package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KlondikeGame extends SolitaireGame {
    private final int drawCount;
    private final CardPile stock = new CardPile();
    private final CardPile waste = new CardPile();
    private final List<CardPile> foundations = new ArrayList<>();

    public KlondikeGame(int drawCount) {
        if (drawCount == 3) {
            throw new UnsupportedOperationException("Klondike Turn 3 is deferred to Step 6");
        }
        if (drawCount != 1) {
            throw new IllegalArgumentException("Draw count must be 1 or 3");
        }
        this.drawCount = drawCount;
        startNewGame();
    }

    @Override
    public void startNewGame() {
        super.startNewGame();
        stock.removeTopCards(stock.size());
        waste.removeTopCards(waste.size());
        foundations.clear();
        for (int i = 0; i < 4; i++) { foundations.add(new CardPile()); }
        for (int i = 0; i < 7; i++) { tableau.add(new CardPile()); }
        Deck deck = Deck.createStandardDeck();
        deck.shuffle();
        for (int row = 0; row < 7; row++) {
            for (int column = row; column < 7; column++) {
                Card card = deck.draw();
                card.setFaceUp(column == row);
                tableau.get(column).addCards(List.of(card));
            }
        }
        while (deck.size() > 0) { stock.addCards(List.of(deck.draw())); }
    }

    public CardPile getStock() { return stock; }
    public CardPile getWaste() { return waste; }
    public List<CardPile> getFoundations() { return Collections.unmodifiableList(foundations); }

    public boolean drawFromStock() {
        if (stock.isEmpty()) { return false; }
        List<Card> drawn = stock.removeTopCards(drawCount);
        drawn.forEach(card -> card.setFaceUp(true));
        waste.addCards(drawn);
        recordMove();
        return true;
    }

    /** Unlimited redeals; reversing the waste preserves the original draw order. */
    public boolean recycleStock() {
        if (!stock.isEmpty() || waste.isEmpty()) { return false; }
        List<Card> recycled = waste.removeTopCards(waste.size());
        Collections.reverse(recycled);
        recycled.forEach(card -> card.setFaceUp(false));
        stock.addCards(recycled);
        recordMove();
        return true;
    }

    @Override
    public boolean canMove(CardPile from, CardPile to, int count) {
        if (from == null || to == null || from == to || count <= 0 || count > from.size()) {
            return false;
        }
        boolean fromTableau = tableau.contains(from);
        if (!fromTableau && from != waste && !foundations.contains(from)) { return false; }
        if (!fromTableau && count != 1) { return false; }
        List<Card> cards = from.getCards();
        int firstIndex = cards.size() - count;
        Card first = cards.get(firstIndex);
        for (int i = firstIndex; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (!card.isFaceUp()) { return false; }
            if (i > firstIndex) {
                Card previous = cards.get(i - 1);
                if (previous.getRank() != card.getRank() + 1 || previous.isRed() == card.isRed()) {
                    return false;
                }
            }
        }
        Card top = to.peek();
        if (tableau.contains(to)) {
            return top == null ? first.getRank() == 13
                    : top.isFaceUp() && top.getRank() == first.getRank() + 1
                    && top.isRed() != first.isRed();
        }
        if (foundations.contains(to) && count == 1 && (fromTableau || from == waste)) {
            return top == null ? first.getRank() == 1
                    : top.isFaceUp() && top.getSuit() == first.getSuit()
                    && first.getRank() == top.getRank() + 1;
        }
        return false;
    }

    @Override
    public boolean isWon() {
        return foundations.size() == 4 && foundations.stream().allMatch(pile -> pile.size() == 13);
    }
}
