package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.Card;
import javafx.scene.AccessibleRole;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/** Read-only card presentation; orientation is read from the model on refresh. */
public final class CardView extends Region {
    private final ImageView image = new ImageView();
    private final Region selectionRing = new Region();

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

    public void setCardWidth(double width) {
        setMinSize(width, width * 7 / 5);
        setPrefSize(width, width * 7 / 5);
        setMaxSize(width, width * 7 / 5);
        resize(width, width * 7 / 5);
    }

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
