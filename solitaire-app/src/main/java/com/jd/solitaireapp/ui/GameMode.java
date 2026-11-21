package com.jd.solitaireapp.ui;

/** Menu choices carried to the future board view without creating a game yet. */
public enum GameMode {
    KLONDIKE_TURN_ONE("Klondike", "Turn 1"),
    KLONDIKE_TURN_THREE("Klondike", "Turn 3"),
    SPIDER_ONE_SUIT("Spider", "1 Suit"),
    SPIDER_TWO_SUITS("Spider", "2 Suits"),
    SPIDER_FOUR_SUITS("Spider", "4 Suits");

    private final String gameName;
    private final String variant;

    GameMode(String gameName, String variant) {
        this.gameName = gameName;
        this.variant = variant;
    }

    public String gameName() {
        return gameName;
    }

    public String variant() {
        return variant;
    }
}
