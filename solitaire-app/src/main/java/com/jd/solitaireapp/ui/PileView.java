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

    /** Temporary presentation of a successful Spider deal, before automatic removals render. */
    CardView appendDealtCard(Card card) {
        CardView previous = cards.get(cards.size() - 1);
        double width = previous.getWidth();
        CardView view = new CardView(card, width);
        view.setManaged(false);
        view.relocate(0, previous.getLayoutY() + width * 0.25);
        cards.put(cards.size(), view);
        getChildren().add(view);
        return view;
    }
    /** Layout strategies for the cards captured by a pile view. */
    public enum Arrangement {
        /** Overlays the top card and its covered predecessor for drag reveal. */
        TOP,
        /** Fans all cards vertically, with smaller offsets for face-down cards. */
        TABLEAU,
        /** Fans the requested number of top waste cards horizontally, retaining a covered predecessor. */
        WASTE
    }

    /**
     * Captures a pile's current cards and orientations in the chosen layout.
     * Later model changes require rebuilding the view; an empty pile retains an outline.
     * @param pile pile to render in bottom-to-top order
     * @param width card width in layout units
     * @param arrangement card layout strategy
     * @param visibleWasteCards number of exposed waste positions; used only for WASTE
     * @throws NullPointerException if pile or arrangement is null
     */
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
        // Render a covered predecessor for drag reveal, preserving its actual face.
        int first = switch (arrangement) {
            case TABLEAU -> 0;
            case TOP -> Math.max(0, pile.size() - 2);
            case WASTE -> Math.max(0, pile.size() - visibleWasteCards - 1);
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
                } else if (arrangement == Arrangement.WASTE
                        && i >= pile.size() - visibleWasteCards) {
                    x += width * 0.23;
                }
            }
        }
        setMinSize(width + x, height + y);
        setPrefSize(width + x, height + y);
        setMaxSize(width + x, height + y);
    }
}
