package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.SpiderGame;
import com.jd.solitaireapp.Card;
import java.util.ArrayList;
import java.util.List;

/** Ten-column Spider rendering with stock above the tableau. */
public final class SpiderBoardView extends GameBoardView {
    private final SpiderGame spider;

    public SpiderBoardView(GameMode mode, int suits, Runnable onBack) {
        super(mode, new SpiderGame(suits), 10, onBack);
        this.spider = (SpiderGame) game;
        refresh();
    }

    @Override
    protected com.jd.solitaireapp.CardPile stockPile() {
        return spider.getStock();
    }

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
        double stockX = left + 9 * (width + gap);
        caption("STOCK", stockX, 10);
        addPile(spider.getStock(), stockX, 36, width,
                PileView.Arrangement.TOP, 1, "Stock");
        double tableauY = 36 + width * 7 / 5 + 54;
        double bottom = tableauY;
        for (int i = 0; i < 10; i++) {
            bottom = Math.max(bottom, addPile(spider.getTableau().get(i), left + i * (width + gap),
                    tableauY, width, PileView.Arrangement.TABLEAU, 1, "Tableau " + (i + 1)));
        }
        return bottom;
    }
}
