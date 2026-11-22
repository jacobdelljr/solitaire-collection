package com.jd.solitaireapp.ui;

import java.util.Objects;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.scene.layout.StackPane;

/** Owns the scene and swaps presentation views; game rules remain in the model. */
public final class AppNavigator {
    private GameBoardView activeBoard;
    private boolean confirmingLeave;
    private final StackPane root = new StackPane();
    private final Scene scene = new Scene(root, 1280, 800);
    private final MainMenuView menu = new MainMenuView(this::showGame);

    /**
     * Creates the styled scene with the mode menu initially displayed.
     * @throws NullPointerException if the application stylesheet is missing
     */
    public AppNavigator() {
        root.getStyleClass().add("application");
        scene.getStylesheets().add(Objects.requireNonNull(
                AppNavigator.class.getResource("/com/jd/solitaireapp/ui/application.css")).toExternalForm());
        PseudoClass compact = PseudoClass.getPseudoClass("compact");
        root.widthProperty().addListener((observable, oldWidth, newWidth) ->
                root.pseudoClassStateChanged(compact, newWidth.doubleValue() < 1050));
        root.getChildren().setAll(menu);
    }

    /**
     * Returns the scene whose root is reused for menu and game navigation.
     * @return the navigator's scene
     */
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
        activeBoard = board;
        root.getChildren().setAll(board);
        board.beginInitialDeal();
        Platform.runLater(board::focusBackButton);
    }

    /**
     * Requests confirmation if a board is active, disposing it only on approval.
     * Does not close the window itself; cancelling or dismissing the dialog keeps the game.
     * @return true if closure may proceed, including when no board is active;
     *         false if cancelled or another confirmation is already open
     */
    public boolean requestClose() {
        if (!confirmLeave()) { return false; }
        disposeBoard();
        return true;
    }

    private void disposeBoard() {
        if (activeBoard != null) {
            activeBoard.dispose();
            activeBoard = null;
        }
    }

    private boolean confirmLeave() {
        if (activeBoard == null) { return true; }
        if (confirmingLeave) { return false; }
        confirmingLeave = true;
        try {
            Dialog<Boolean> dialog = new Dialog<>();
            dialog.initOwner(scene.getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Leave this game?");
            DialogPane pane = dialog.getDialogPane();
            pane.getStylesheets().addAll(scene.getStylesheets());
            pane.getStyleClass().addAll("application", "leave-dialog");
            pane.setContent(new VBox(16,
                    MainMenuView.label("Leave this game?", "board-title"),
                    MainMenuView.label("Your current game will be lost.", "description")));
            ButtonType cancel = new ButtonType(
                    "CANCEL", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType leave = new ButtonType(
                    "LEAVE GAME", ButtonBar.ButtonData.OK_DONE);
            pane.getButtonTypes().setAll(cancel, leave);
            Button cancelButton = (Button) pane.lookupButton(cancel);
            Button leaveButton = (Button) pane.lookupButton(leave);
            leaveButton.setDefaultButton(false);
            cancelButton.setDefaultButton(true);
            cancelButton.setCancelButton(true);
            dialog.setResultConverter(button -> button == leave);
            dialog.setOnShown(event -> cancelButton.requestFocus());
            return dialog.showAndWait().orElse(false);
        } finally {
            confirmingLeave = false;
        }
    }

    private void showMenu(GameMode previousMode) {
        if (!confirmLeave()) { return; }
        disposeBoard();
        root.getChildren().setAll(menu);
        Platform.runLater(() -> menu.focusMode(previousMode));
    }
}
