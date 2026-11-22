package com.jd.solitaireapp.ui;

/** Supported game variants used by the menu and navigator to select a board. */
public enum GameMode {
    /** Klondike drawing one card per stock action. */
    KLONDIKE_TURN_ONE("Klondike", "Turn 1"),
    /** Klondike drawing up to three cards per stock action. */
    KLONDIKE_TURN_THREE("Klondike", "Turn 3"),
    /** Spider using only Spades. */
    SPIDER_ONE_SUIT("Spider", "1 Suit"),
    /** Spider using Spades and Hearts. */
    SPIDER_TWO_SUITS("Spider", "2 Suits"),
    /** Spider using all four suits. */
    SPIDER_FOUR_SUITS("Spider", "4 Suits");

    private final String gameName;
    private final String variant;

    GameMode(String gameName, String variant) {
        this.gameName = gameName;
        this.variant = variant;
    }

    /**
     * Returns the display name shared by variants of the same game.
     * @return "Klondike" or "Spider"
     */
    public String gameName() {
        return gameName;
    }

    /**
     * Returns the variant label displayed on menu buttons and boards.
     * @return the draw-count or suit-count label
     */
    public String variant() {
        return variant;
    }
}
