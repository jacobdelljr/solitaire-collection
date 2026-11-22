package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.Card;
import com.jd.solitaireapp.Suit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

/** Shared card artwork, loaded and validated on first access to the cache. */
public final class CardImages {
    private static final String ROOT = "/com/jd/solitaireapp/cards/png/";
    private final Map<String, Image> images = new HashMap<>();

    private CardImages() {
        for (Suit suit : Suit.values()) {
            for (int rank = 1; rank <= 13; rank++) {
                load(faceName(suit, rank));
            }
        }
        load("BACK.png");
    }

    /** Initializes the shared cache only when first requested. */
    private static final class Holder {
        private static final CardImages INSTANCE = new CardImages();
    }

    /**
     * Loads all faces and the back on first access, then reuses the cache.
     * @return the shared artwork cache
     * @throws ExceptionInInitializerError if initial loading fails, including
     *         missing, undecodable, or incorrectly proportioned artwork
     */
    public static CardImages shared() {
        return Holder.INSTANCE;
    }

    /**
     * Selects cached artwork using the card's current orientation.
     * @param card card whose image is needed
     * @return the face image if exposed, otherwise the common card back
     * @throws NullPointerException if card is null
     */
    public Image forCard(Card card) {
        return images.get(card.isFaceUp() ? faceName(card.getSuit(), card.getRank()) : "BACK.png");
    }

    private static String faceName(Suit suit, int rank) {
        String rankName = switch (rank) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> Integer.toString(rank);
        };
        String suitName = switch (suit) {
            case CLUBS -> "C";
            case DIAMONDS -> "D";
            case HEARTS -> "H";
            case SPADES -> "S";
        };
        return rankName + "_" + suitName + ".png";
    }

    private void load(String filename) {
        URL resource = CardImages.class.getResource(ROOT + filename);
        if (resource == null) {
            throw new IllegalStateException("Missing approved card image: " + ROOT + filename);
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            throw new IllegalStateException("Cannot decode card image: " + filename, image.getException());
        }
        if (image.getWidth() <= 0 || Math.abs(image.getWidth() / image.getHeight() - 5.0 / 7) > 0.001) {
            throw new IllegalStateException("Expected 5:7 card artwork: " + filename);
        }
        images.put(filename, image);
    }
}
