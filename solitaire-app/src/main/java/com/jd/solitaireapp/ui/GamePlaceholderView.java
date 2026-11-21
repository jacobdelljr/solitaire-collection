package com.jd.solitaireapp.ui;

import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Explicit development placeholder, with no simulated gameplay or model state. */
public final class GamePlaceholderView extends BorderPane {
    private final Button backButton = new Button("Return to menu");

    public GamePlaceholderView(GameMode mode, Runnable onBack) {
        getStyleClass().add("page");
        setTop(MainMenuView.header("SC / 001", "SOLITAIRE COLLECTION"));
        Label title = MainMenuView.label(mode.gameName().toUpperCase(Locale.ROOT) + ".", "display-title");
        Label message = MainMenuView.label(
                "The game board will be added in the next development stage.", "description");
        message.setWrapText(true);
        backButton.getStyleClass().add("back-button");
        backButton.setCancelButton(true);
        backButton.setOnAction(event -> onBack.run());
        VBox content = new VBox(24,
                MainMenuView.label("GAME BOARD / PLACEHOLDER", "eyebrow"),
                title, MainMenuView.label(mode.variant(), "game-title"), message, backButton);
        content.setAlignment(Pos.CENTER_LEFT);
        setCenter(content);
        setBottom(MainMenuView.header("SELECTED / " + mode.variant().toUpperCase(Locale.ROOT), "ESC / RETURN TO MENU"));
    }

    void focusBackButton() {
        backButton.requestFocus();
    }
}
