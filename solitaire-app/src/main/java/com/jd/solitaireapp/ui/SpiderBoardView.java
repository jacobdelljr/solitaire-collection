package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.SpiderGame;

/** Ten-column Spider rendering with fixed stock above the scrolling tableau. */
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
    protected boolean playStock() {
        return spider.dealNextRow();
    }

    @Override
    protected double renderPiles(double left, double width, double gap) {
        double bottom = 20;
        for (int i = 0; i < 10; i++) {
            bottom = Math.max(bottom, addPile(spider.getTableau().get(i), left + i * (width + gap),
                    20, width, PileView.Arrangement.TABLEAU, 1, "Tableau " + (i + 1)));
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
