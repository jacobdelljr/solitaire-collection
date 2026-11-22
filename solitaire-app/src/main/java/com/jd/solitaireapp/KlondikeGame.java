package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Turn 1 or Turn 3 Klondike with seven tableau columns, four foundations, and unlimited
 * stock recycling. Tableau builds down in alternating colors; foundations build
 * up by suit from Ace to King. Transfers, draws and recycles support undo.
 */
public class KlondikeGame extends SolitaireGame {
    /** Maximum cards per stock draw; either 1 or 3. */
    private final int drawCount;
    /** Face-down cards available for drawing. */
    private final CardPile stock = new CardPile();
    /** Drawn face-up cards; only the top card may be moved. */
    private final CardPile waste = new CardPile();
    /** Four foundations, each acquiring its suit from its first Ace. */
    private final List<CardPile> foundations = new ArrayList<>();

    /**
     * Creates and deals a shuffled Turn 1 or Turn 3 game.
     * @param drawCount maximum cards per draw; either 1 or 3
     * @throws IllegalArgumentException if drawCount is neither 1 nor 3
     */
    public KlondikeGame(int drawCount) {
        if (drawCount != 1 && drawCount != 3) {
            throw new IllegalArgumentException("Draw count must be 1 or 3");
        }
        this.drawCount = drawCount;
        startNewGame();
    }

    /**
     * Resets shared state and deals a shuffled 52-card deck into seven new columns
     * of 1–7 cards, with only each top card face up. Creates four empty foundations,
     * clears the waste, and fills the stock with the remaining 24 face-down cards.
     * Retains the configured draw count.
     * Previously returned tableau/foundation pile objects no longer belong to this game.
     */
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

    /** @return the mutable stock pile; use stock operations to preserve game rules */
    public CardPile getStock() { return stock; }
    /** @return the mutable waste pile; use game operations to move its top card */
    public CardPile getWaste() { return waste; }
    /** @return an unmodifiable live list of the four mutable foundation piles */
    public List<CardPile> getFoundations() { return Collections.unmodifiableList(foundations); }

    /**
     * Moves up to the configured draw count of stock cards face up onto the waste
     * in top-first stock order, leaving the last drawn card on top of the waste.
     * Draws all remaining cards if fewer are available and counts one move per action.
     * Does not automatically recycle an exhausted stock.
     * @return true if drawn; false without mutation if the stock is empty
     */
    public boolean drawFromStock() {
        if (stock.isEmpty()) { return false; }
        saveUndoState();
        List<Card> drawn = stock.removeTopCards(Math.min(drawCount, stock.size()));
        Collections.reverse(drawn);
        drawn.forEach(card -> card.setFaceUp(true));
        waste.addCards(drawn);
        recordMove();
        return true;
    }

    /**
     * Turns the entire waste face down into an empty stock, reversing pile order
     * so remaining cards repeat their prior draw order. Counts one move on success;
     * there is no limit on recycling.
     * @return true if recycled; false without mutation if stock is nonempty or waste empty
     */
    public boolean recycleStock() {
        if (!stock.isEmpty() || waste.isEmpty()) { return false; }
        saveUndoState();
        List<Card> recycled = waste.removeTopCards(waste.size());
        Collections.reverse(recycled);
        recycled.forEach(card -> card.setFaceUp(false));
        stock.addCards(recycled);
        recordMove();
        return true;
    }

    /**
     * Validates a transfer without mutation. Tableau segments must be face-up,
     * descending, and alternating in color. A tableau destination accepts a King
     * in an empty column or a card one rank below and opposite in color to its
     * face-up top. Only one top waste or foundation card may leave those piles.
     * Foundations accept a single tableau/waste Ace when empty, otherwise the next
     * rank of the same suit on a face-up top. Foundation-to-foundation transfers,
     * stock transfers, and moves into waste are rejected.
     * @param from source tableau, waste, or foundation pile of this game
     * @param to destination tableau or foundation pile of this game
     * @param count positive number of top cards to transfer
     * @return true if legal; false for rule violations, null/foreign/identical
     *         piles, or a count outside the source's available cards
     */
    @Override
    public boolean canMove(CardPile from, CardPile to, int count) {
        if (to == null || from == to || !canPickUp(from, count)) { return false; }
        boolean fromTableau = tableau.contains(from);
        Card first = from.getCards().get(from.size() - count);
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

    /** {@inheritDoc} */
    @Override
    public boolean canPickUp(CardPile from, int count) {
        if (from == null || count <= 0 || count > from.size()) {
            return false;
        }
        boolean fromTableau = tableau.contains(from);
        if (!fromTableau && from != waste && !foundations.contains(from)) { return false; }
        if (!fromTableau && count != 1) { return false; }
        List<Card> cards = from.getCards();
        int firstIndex = cards.size() - count;
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
        return true;
    }

    /**
     * Checks foundation sizes, assuming cards reached them through legal moves.
     * Does not revalidate ranks, suits, or the rest of the game state.
     * @return true if there are four foundations containing 13 cards each
     */
    @Override
    public boolean isWon() {
        return foundations.size() == 4 && foundations.stream().allMatch(pile -> pile.size() == 13);
    }
}
