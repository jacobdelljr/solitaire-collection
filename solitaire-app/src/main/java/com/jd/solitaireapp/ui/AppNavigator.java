package com.jd.solitaireapp.ui;

import java.util.Objects;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

/** Owns the scene and swaps presentation views; game rules remain in the model. */
public final class AppNavigator {
    private final StackPane root = new StackPane();
    private final Scene scene = new Scene(root, 1280, 800);
    private final MainMenuView menu = new MainMenuView(this::showGame);

    public AppNavigator() {
        root.getStyleClass().add("application");
        scene.getStylesheets().add(Objects.requireNonNull(
                AppNavigator.class.getResource("/com/jd/solitaireapp/ui/application.css")).toExternalForm());
        PseudoClass compact = PseudoClass.getPseudoClass("compact");
        root.widthProperty().addListener((observable, oldWidth, newWidth) ->
                root.pseudoClassStateChanged(compact, newWidth.doubleValue() < 1050));
        root.getChildren().setAll(menu);
    }

    public Scene getScene() {
        return scene;
    }

    private void showGame(GameMode mode) {
        GameBoardView board = switch (mode) {
            case KLONDIKE_TURN_ONE -> new KlondikeBoardView(mode, 1, () -> showMenu(mode));
            case KLONDIKE_TURN_THREE -> new KlondikeBoardView(mode, 3, () -> showMenu(mode));
            case SPIDER_ONE_SUIT -> new SpiderBoardView(mode, 1, () -> showMenu(mode));
            case SPIDER_TWO_SUITS -> new SpiderBoardView(mode, 2, () -> showMenu(mode));
            case SPIDER_FOUR_SUITS -> new SpiderBoardView(mode, 4, () -> showMenu(mode));
        };
        root.getChildren().setAll(board);
        Platform.runLater(board::focusBackButton);
    }

    private void showMenu(GameMode previousMode) {
        root.getChildren().setAll(menu);
        Platform.runLater(() -> menu.focusMode(previousMode));
    }
}
