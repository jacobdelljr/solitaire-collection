package com.jd.solitaireapp.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Editorial opening menu. Buttons communicate only the selected mode. */
public final class MainMenuView extends BorderPane {
    private final Map<GameMode, Button> modeButtons = new EnumMap<>(GameMode.class);

    public MainMenuView(Consumer<GameMode> onSelect) {
        getStyleClass().add("page");
        setTop(header("SC / 001", "A COLLECTION OF CARD GAMES"));

        Label title = label("SOLITAIRE.", "display-title");
        VBox introduction = new VBox(14, title,
                label("A collection of classic card games.", "description"));
        Region pause = new Region();
        pause.setMinHeight(36);
        VBox.setVgrow(pause, Priority.ALWAYS);

        VBox choices = new VBox(
                gameRow("01", "KLONDIKE", "The classic. Build by suit, ace to king.", onSelect,
                        GameMode.KLONDIKE_TURN_ONE, GameMode.KLONDIKE_TURN_THREE),
                gameRow("02", "SPIDER", "A study in sequence. Complete every run.", onSelect,
                        GameMode.SPIDER_ONE_SUIT, GameMode.SPIDER_TWO_SUITS, GameMode.SPIDER_FOUR_SUITS));
        choices.getStyleClass().add("choices");
        VBox body = new VBox(introduction, pause, choices);
        body.getStyleClass().add("menu-body");
        setCenter(body);
        setBottom(header("FIVE WAYS TO PLAY", "SELECT A MODE TO BEGIN"));
    }

    private HBox gameRow(String number, String name, String description,
                         Consumer<GameMode> onSelect, GameMode... modes) {
        Label index = label(number, "index");
        index.setMinWidth(40);
        Label detail = label(description, "row-description");
        detail.setWrapText(true);
        VBox copy = new VBox(9, label(name, "game-title"), detail);
        copy.setMinWidth(180);
        HBox.setHgrow(copy, Priority.ALWAYS);
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_RIGHT);
        controls.setMinWidth(Region.USE_PREF_SIZE);
        for (GameMode mode : modes) {
            Button button = new Button(mode.variant());
            button.getStyleClass().add("mode-button");
            button.setAccessibleText(mode.gameName() + ", " + mode.variant());
            button.setOnAction(event -> onSelect.accept(mode));
            modeButtons.put(mode, button);
            controls.getChildren().add(button);
        }
        HBox row = new HBox(20, index, copy, controls);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("game-row");
        return row;
    }

    void focusMode(GameMode mode) {
        modeButtons.get(mode).requestFocus();
    }

    static Label label(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    static HBox header(String left, String right) {
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(16, label(left, "small-label"), space, label(right, "small-label"));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }
}
