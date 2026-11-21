package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.SpiderGame;

/** Ten-column Spider rendering with stock below the longest tableau column. */
public final class SpiderBoardView extends GameBoardView {
    private final SpiderGame spider;

    public SpiderBoardView(GameMode mode, int suits, Runnable onBack) {
        super(mode, new SpiderGame(suits), 10, onBack);
        this.spider = (SpiderGame) game;
        refresh();
    }

    @Override
    protected double renderPiles(double left, double width, double gap) {
        double bottom = 20;
        for (int i = 0; i < 10; i++) {
            bottom = Math.max(bottom, addPile(spider.getTableau().get(i), left + i * (width + gap),
                    20, width, PileView.Arrangement.TABLEAU, 1, "Tableau " + (i + 1)));
        }
        double stockX = left + 9 * (width + gap);
        caption("STOCK", stockX, bottom + 30);
        return addPile(spider.getStock(), stockX, bottom + 56, width,
                PileView.Arrangement.TOP, 1, "Stock");
    }
}
