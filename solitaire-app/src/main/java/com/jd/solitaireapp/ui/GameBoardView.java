package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.CardPile;
import com.jd.solitaireapp.SolitaireGame;
import java.util.Locale;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Shared board chrome and refresh boundary. Model operations stay in the model. */
public abstract class GameBoardView extends BorderPane {
    protected final SolitaireGame game;
    protected final Pane surface = new Pane();
    private final ScrollPane scroll = new ScrollPane(surface);
    private final Button backButton = new Button("Return to menu");
    private final int columns;

    protected GameBoardView(GameMode mode, SolitaireGame game, int columns, Runnable onBack) {
        this.game = game;
        this.columns = columns;
        getStyleClass().add("board-page");
        backButton.setCancelButton(true);
        backButton.setOnAction(event -> onBack.run());
        VBox title = new VBox(5,
                MainMenuView.label(mode.gameName().toUpperCase(Locale.ROOT), "board-title"),
                MainMenuView.label(mode.variant().toUpperCase(Locale.ROOT), "small-label"));
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button newGame = new Button("NEW GAME");
        newGame.setOnAction(event -> {
            game.startNewGame();
            refresh();
            scroll.setVvalue(0);
            scroll.setHvalue(0);
        });
        HBox header = new HBox(28, backButton, title, space, newGame);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("board-header");
        setTop(header);
        scroll.getStyleClass().add("board-scroll");
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.viewportBoundsProperty().addListener((observable, before, after) -> {
            if (Math.abs(before.getWidth() - after.getWidth()) > 0.5) {
                refresh();
            }
        });
        setCenter(scroll);
        Label note = MainMenuView.label("SC / 001     /     SOLITAIRE COLLECTION", "small-label");
        note.getStyleClass().add("board-footer");
        setBottom(note);
    }

    /** Rebuilds presentation from current piles after a future successful model action. */
    public final void refresh() {
        double viewport = Math.max(0, scroll.getViewportBounds().getWidth());
        double margin = 20;
        double gap = columns == 10 ? 14 : 24;
        // Keep indices legible; narrow windows scroll instead of shrinking indefinitely.
        double width = Math.max(100, Math.min(columns == 10 ? 120 : 140,
                (viewport - margin * 2 - gap * (columns - 1)) / columns));
        double boardWidth = Math.max(viewport, margin * 2 + columns * width + (columns - 1) * gap);
        double left = (boardWidth - columns * width - (columns - 1) * gap) / 2;
        surface.getChildren().clear();
        double bottom = renderPiles(left, width, gap);
        surface.setMinSize(margin * 2 + columns * width + (columns - 1) * gap, bottom + 28);
        surface.setPrefSize(boardWidth, bottom + 28);
    }

    protected abstract double renderPiles(double left, double cardWidth, double gap);

    protected final double addPile(CardPile pile, double x, double y, double width,
                                   PileView.Arrangement arrangement, int wasteCount, String name) {
        PileView view = new PileView(pile, width, arrangement, wasteCount);
        view.setManaged(false);
        view.resize(view.prefWidth(-1), view.prefHeight(-1));
        view.relocate(x, y);
        view.setAccessibleText(name + (pile.isEmpty() ? ", empty" : ""));
        surface.getChildren().add(view);
        return y + view.prefHeight(-1);
    }

    protected final void caption(String text, double x, double y) {
        Label label = MainMenuView.label(text, "pile-caption");
        label.setManaged(false);
        label.autosize();
        label.relocate(x, y);
        surface.getChildren().add(label);
    }

    public void focusBackButton() {
        backButton.requestFocus();
    }
}
