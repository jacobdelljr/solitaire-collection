package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.KlondikeGame;

/** Seven-column Klondike rendering for either draw variation. */
public final class KlondikeBoardView extends GameBoardView {
    private final KlondikeGame klondike;
    private final int drawCount;

    public KlondikeBoardView(GameMode mode, int drawCount, Runnable onBack) {
        super(mode, new KlondikeGame(drawCount), 7, onBack);
        this.klondike = (KlondikeGame) game;
        this.drawCount = drawCount;
        refresh();
    }

    @Override
    protected double renderPiles(double left, double width, double gap) {
        double step = width + gap;
        double top = 36;
        caption("STOCK", left, 10);
        caption("WASTE", left + step, 10);
        caption("FOUNDATIONS", left + 3 * step, 10);
        addPile(klondike.getStock(), left, top, width, PileView.Arrangement.TOP, 1, "Stock");
        addPile(klondike.getWaste(), left + step, top, width, PileView.Arrangement.WASTE, drawCount, "Waste");
        for (int i = 0; i < 4; i++) {
            addPile(klondike.getFoundations().get(i), left + (i + 3) * step, top,
                    width, PileView.Arrangement.TOP, 1, "Foundation " + (i + 1));
        }
        double tableauY = top + width * 7 / 5 + 54;
        double bottom = tableauY;
        for (int i = 0; i < 7; i++) {
            bottom = Math.max(bottom, addPile(klondike.getTableau().get(i), left + i * step,
                    tableauY, width, PileView.Arrangement.TABLEAU, 1, "Tableau " + (i + 1)));
        }
        return bottom;
    }
}
