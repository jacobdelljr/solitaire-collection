package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Console demonstration of implemented Klondike and Spider operations, without UI.
 * Shuffled decks make output vary. The implicit public constructor has no setup;
 * the demonstration is run through {@link #main(String[])}.
 */
public class Main {
    /**
     * Prints initial layouts, representative legal/rejected actions, stock
     * operations, move counts, completion status, and deferred-feature notices.
     * @param args command-line arguments, ignored
     */
    public static void main(String[] args) {
        System.out.println("Solitaire Collection - Step 4 console demonstration");
        KlondikeGame klondike = new KlondikeGame(1);
        System.out.println("\nKlondike Turn 1: tableau sizes " + sizes(klondike)
                + ", stock " + klondike.getStock().size());
        System.out.println("Draw: " + klondike.drawFromStock()
                + "; waste top: " + describe(klondike.getWaste().peek()));
        List<CardPile> sources = new ArrayList<>(klondike.getTableau());
        sources.add(klondike.getWaste());
        List<CardPile> destinations = new ArrayList<>(klondike.getTableau());
        destinations.addAll(klondike.getFoundations());
        demonstrateMove(klondike, sources, destinations);
        System.out.println("Recycle nonempty stock (rejected): " + klondike.recycleStock());
        while (klondike.drawFromStock()) { /* Consume the remaining stock. */ }
        System.out.println("Recycle exhausted stock: " + klondike.recycleStock()
                + "; stock now " + klondike.getStock().size());
        System.out.println("Moves: " + klondike.getMoveCount() + "; won: " + klondike.isWon());

        SpiderGame spider = new SpiderGame(1);
        System.out.println("\nOne-suit Spider: tableau sizes " + sizes(spider)
                + ", stock " + spider.getStock().size());
        demonstrateMove(spider, spider.getTableau(), spider.getTableau());
        int before = spider.getMoveCount();
        System.out.println("Move from stock (rejected): "
                + spider.moveCards(spider.getStock(), spider.getTableau().getFirst(), 1)
                + "; move count unchanged: " + (before == spider.getMoveCount()));
        System.out.println("Deal stock row: " + spider.dealNextRow()
                + "; stock now " + spider.getStock().size());
        System.out.println("Completed runs: " + spider.getCompletedRuns()
                + "; moves: " + spider.getMoveCount() + "; won: " + spider.isWon());
        System.out.println("\nStep 6 deferred: Turn 3, multi-suit Spider, and GameSnapshot/undo.");
    }

    /**
     * Collects current tableau sizes for display.
     * @param game game whose tableau is inspected
     * @return an unmodifiable list of sizes in column order
     */
    private static List<Integer> sizes(SolitaireGame game) {
        return game.getTableau().stream().map(CardPile::size).toList();
    }

    /**
     * Formats an exposed card without revealing a face-down card's identity.
     * @param card card to describe, or null for an empty pile
     * @return rank and suit, "face down", or "empty pile"
     */
    private static String describe(Card card) {
        if (card == null) { return "empty pile"; }
        if (!card.isFaceUp()) { return "face down"; }
        String rank = switch (card.getRank()) {
            case 1 -> "Ace";
            case 11 -> "Jack";
            case 12 -> "Queen";
            case 13 -> "King";
            default -> Integer.toString(card.getRank());
        };
        return rank + " of " + card.getSuit();
    }

    /**
     * Executes and prints the first legal transfer found, or prints that none exists.
     * Searches sources, destinations, then positive segment sizes in order.
     * @param game game used to validate and execute the transfer
     * @param sources candidate source piles
     * @param destinations candidate destination piles
     */
    private static void demonstrateMove(SolitaireGame game, List<CardPile> sources,
                                        List<CardPile> destinations) {
        for (CardPile from : sources) {
            for (CardPile to : destinations) {
                for (int count = 1; count <= from.size(); count++) {
                    if (game.canMove(from, to, count)) {
                        String card = describe(from.getCards().get(from.size() - count));
                        System.out.println("Legal move: " + count + " card(s), starting with " + card
                                + ", onto " + describe(to.peek()) + ": " + game.moveCards(from, to, count));
                        return;
                    }
                }
            }
        }
        System.out.println("No legal tableau/foundation move in this shuffled position.");
    }
}
