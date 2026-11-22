package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.Card;
import com.jd.solitaireapp.CardPile;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import java.util.LinkedHashMap;
import java.util.Map;

/** Positions cards bottom-to-top, preserving the top-left rank and suit indices. */
public final class PileView extends Pane {
    private final Map<Integer, CardView> cards = new LinkedHashMap<>();

    Map<Integer, CardView> cardViews() {
        return cards;
    }
    public enum Arrangement { TOP, TABLEAU, WASTE }

    public PileView(CardPile pile, double width, Arrangement arrangement, int visibleWasteCards) {
        getStyleClass().add("card-pile");
        double height = width * 7 / 5;
        Region outline = new Region();
        outline.getStyleClass().add("empty-pile");
        outline.setManaged(false);
        outline.resize(width, height);
        getChildren().add(outline);
        double x = 0;
        double y = 0;
        int first = switch (arrangement) {
            case TABLEAU -> 0;
            case TOP -> Math.max(0, pile.size() - 1);
            case WASTE -> Math.max(0, pile.size() - visibleWasteCards);
        };
        for (int i = first; i < pile.size(); i++) {
            Card card = pile.getCards().get(i);
            CardView view = new CardView(card, width);
            cards.put(i, view);
            view.setManaged(false);
            view.relocate(x, y);
            getChildren().add(view);
            if (i < pile.size() - 1) {
                if (arrangement == Arrangement.TABLEAU) {
                    // Artwork's upper indices end at y=110 in its 500px-wide design.
                    y += card.isFaceUp() ? width * 0.25 : width * 0.16;
                } else if (arrangement == Arrangement.WASTE) {
                    x += width * 0.23;
                }
            }
        }
        setMinSize(width + x, height + y);
        setPrefSize(width + x, height + y);
        setMaxSize(width + x, height + y);
    }
}
