package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.Card;
import javafx.scene.AccessibleRole;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/** Card image and selection ring captured at construction; model changes require a new view. */
public final class CardView extends Region {
    private final ImageView image = new ImageView();
    private final Region selectionRing = new Region();

    /** Presentation copy: never consult a card whose orientation may have changed. */
    CardView(CardView source) {
        getStyleClass().add("playing-card");
        image.setImage(source.image.getImage());
        image.setPreserveRatio(true);
        image.setSmooth(true);
        getChildren().add(image);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setAccessibleText(source.getAccessibleText());
        setCardWidth(source.getWidth());
    }

    /**
     * Captures the card's current image and accessible description without changing it.
     * @param card card to display; later orientation changes are not observed
     * @param width card width in layout units, with height set to seven fifths of width
     * @throws NullPointerException if card is null
     */
    public CardView(Card card, double width) {
        getStyleClass().add("playing-card");
        image.setImage(CardImages.shared().forCard(card));
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setMouseTransparent(true);
        selectionRing.getStyleClass().add("selection-ring");
        selectionRing.setMouseTransparent(true);
        getChildren().addAll(image, selectionRing);
        setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        setAccessibleText(card.isFaceUp()
                ? rankName(card.getRank()) + " of " + card.getSuit().name().toLowerCase(java.util.Locale.ROOT)
                : "Face-down card");
        setCardWidth(width);
    }

    /**
     * Resizes the card and fixes its layout size at the artwork's 5:7 aspect ratio.
     * @param width requested width in layout units
     */
    public void setCardWidth(double width) {
        setMinSize(width, width * 7 / 5);
        setPrefSize(width, width * 7 / 5);
        setMaxSize(width, width * 7 / 5);
        resize(width, width * 7 / 5);
    }

    /** Fits the artwork and selection ring to the region's current bounds. */
    @Override
    protected void layoutChildren() {
        image.setFitWidth(getWidth());
        image.setFitHeight(getHeight());
        selectionRing.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    private static String rankName(int rank) {
        return switch (rank) {
            case 1 -> "Ace";
            case 11 -> "Jack";
            case 12 -> "Queen";
            case 13 -> "King";
            default -> Integer.toString(rank);
        };
    }
}
