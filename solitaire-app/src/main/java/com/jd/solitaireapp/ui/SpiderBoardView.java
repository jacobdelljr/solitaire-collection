package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.SpiderGame;
import com.jd.solitaireapp.Card;
import java.util.ArrayList;
import java.util.List;

/** Ten-column Spider rendering with fixed stock above the scrolling tableau. */
public final class SpiderBoardView extends GameBoardView {
    private final SpiderGame spider;

    /**
     * Creates a shuffled Spider game and renders its current piles.
     * Initial deal animation starts separately through {@link #beginInitialDeal()}.
     * @param mode display labels; does not override the numeric game configuration
     * @param suits number of suits: 1, 2, or 4
     * @param onBack callback invoked when the user requests the menu
     * @throws IllegalArgumentException if suits is not 1, 2, or 4
     * @throws NullPointerException if mode is null
     */
    public SpiderBoardView(GameMode mode, int suits, Runnable onBack) {
        super(mode, new SpiderGame(suits), 10, onBack);
        this.spider = (SpiderGame) game;
        refresh();
    }

    @Override
    protected com.jd.solitaireapp.CardPile stockPile() {
        return spider.getStock();
    }

    /** Deals a legal row and animates its ten cards before displaying automatic removals. */
    @Override
    protected void playStock() {
        // Remember only the ten cards being dealt. The model may immediately remove
        // completed runs, so retain the old presentation until all ten cards arrive.
        List<Card> stock = spider.getStock().getCards();
        if (stock.size() < 10) { return; }
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < 10; i++) { drawn.add(stock.get(stock.size() - 1 - i)); }
        if (!spider.dealNextRow()) { return; }
        List<CardView> targets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            targets.add(pileView(spider.getTableau().get(i)).appendDealtCard(drawn.get(i)));
        }
        animateDeal(targets);
    }

    @Override
    protected double renderPiles(double left, double width, double gap) {
        double tableauY = 20;
        double bottom = tableauY;
        for (int i = 0; i < 10; i++) {
            bottom = Math.max(bottom, addPile(spider.getTableau().get(i), left + i * (width + gap),
                    tableauY, width, PileView.Arrangement.TABLEAU, 1, "Tableau " + (i + 1)));
        }
        caption(fixedArea, "STOCK / DEAL ROW", 20, 0);
        double stockBottom = addPile(fixedArea, spider.getStock(), 20, 24, width,
                PileView.Arrangement.TOP, 1, "Stock, deal next row");
        caption(fixedArea, "COMPLETED SEQUENCES", 20 + width + gap * 2, 0);
        caption(fixedArea, spider.getCompletedRuns() + " / 8", 20 + width + gap * 2, 30);
        fixedArea.setMinHeight(stockBottom + 16);
        fixedArea.setPrefHeight(stockBottom + 16);
        return bottom;
    }
}
