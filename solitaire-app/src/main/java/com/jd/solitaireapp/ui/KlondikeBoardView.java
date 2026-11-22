package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.KlondikeGame;

/** Seven-column Klondike rendering for either draw variation. */
public final class KlondikeBoardView extends GameBoardView {
    private final KlondikeGame klondike;
    private final int drawCount;

    /**
     * Creates a shuffled Klondike game and renders its current piles.
     * Initial deal animation starts separately through {@link #beginInitialDeal()}.
     * @param mode display labels; does not override the numeric game configuration
     * @param drawCount maximum cards per draw: 1 or 3
     * @param onBack callback invoked when the user requests the menu
     * @throws IllegalArgumentException if drawCount is not 1 or 3
     * @throws NullPointerException if mode is null
     */
    public KlondikeBoardView(GameMode mode, int drawCount, Runnable onBack) {
        super(mode, new KlondikeGame(drawCount), 7, onBack);
        this.klondike = (KlondikeGame) game;
        this.drawCount = drawCount;
        refresh();
    }

    @Override
    protected com.jd.solitaireapp.CardPile stockPile() {
        return klondike.getStock();
    }

    /** Draws or recycles stock; successful Turn 3 draws animate the new waste cards. */
    @Override
    protected void playStock() {
        if (klondike.getStock().isEmpty()) {
            if (klondike.recycleStock()) { afterAction(); }
            return;
        }
        int firstDrawn = klondike.getWaste().size();
        if (!klondike.drawFromStock()) { return; }
        afterAction();
        if (drawCount == 3) {
            animateDeal(pileView(klondike.getWaste()).cardViews().entrySet().stream()
                    .filter(entry -> entry.getKey() >= firstDrawn)
                    .map(java.util.Map.Entry::getValue).toList());
        }
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
