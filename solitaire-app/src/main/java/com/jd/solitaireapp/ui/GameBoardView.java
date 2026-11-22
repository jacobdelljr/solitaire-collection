package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.CardPile;
import com.jd.solitaireapp.SolitaireGame;
import java.util.Locale;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.layout.StackPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Shared board chrome and refresh boundary. Model operations stay in the model. */
public abstract class GameBoardView extends BorderPane {
    protected final SolitaireGame game;
    protected final Pane surface = new Pane();
    private final ScrollPane scroll = new ScrollPane(surface);
    private final Button backButton = new Button("Return to menu");
    private final Pane dragOverlay = new Pane();
    private final Pane dealOverlay = new Pane();
    private final CardDealAnimation dealing = new CardDealAnimation(dealOverlay);
    private final Map<CardPile, PileView> pileViews = new IdentityHashMap<>();
    private boolean pendingDeal;
    private int dealGeneration;
    private final BoardInteractions interactions;
    private final VBox completion = new VBox(10);
    private final int columns;

    protected GameBoardView(GameMode mode, SolitaireGame game, int columns, Runnable onBack) {
        this.game = game;
        this.columns = columns;
        interactions = new BoardInteractions(game, dragOverlay, scroll, this::afterAction);
        dragOverlay.setMouseTransparent(true);
        dragOverlay.setPickOnBounds(false);
        dealOverlay.setMouseTransparent(true);
        dealOverlay.setPickOnBounds(false);
        surface.addEventFilter(MouseEvent.ANY, event -> {
            if (pendingDeal || dealing.isActive()) { event.consume(); }
        });
        sceneProperty().addListener((observable, before, after) -> {
            if (after == null) { cancelDealing(); }
        });
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                interactions.cancel();
                cancelDealing();
                event.consume();
            }
        });
        surface.setOnMousePressed(event -> interactions.cancel());
        // Scroll/resize cancellation prevents stale scene coordinates during a gesture.
        scroll.hvalueProperty().addListener((observable, before, after) -> {
            interactions.cancelGesture();
            cancelDealing();
        });
        scroll.vvalueProperty().addListener((observable, before, after) -> {
            interactions.cancelGesture();
            cancelDealing();
        });
        getStyleClass().add("board-page");
        backButton.setOnAction(event -> {
            cancelDealing();
            interactions.cancel();
            onBack.run();
        });
        VBox title = new VBox(5,
                MainMenuView.label(mode.gameName().toUpperCase(Locale.ROOT), "board-title"),
                MainMenuView.label(mode.variant().toUpperCase(Locale.ROOT), "small-label"));
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button newGame = new Button("NEW GAME");
        newGame.setOnAction(event -> startNewGame());
        Button undo = new Button("UNDO");
        undo.setOnAction(event -> {
            cancelDealing();
            interactions.cancel();
            if (game.undo()) { afterAction(); }
        });
        HBox header = new HBox(20, backButton, title, space, undo, newGame);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("board-header");
        setTop(header);
        scroll.getStyleClass().add("board-scroll");
        scroll.setFitToWidth(true);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.viewportBoundsProperty().addListener((observable, before, after) -> {
            if (Math.abs(before.getWidth() - after.getWidth()) > 0.5
                    || Math.abs(before.getHeight() - after.getHeight()) > 0.5) {
                boolean awaitingLayout = pendingDeal;
                refresh();
                // Initial attachment may need another layout pass for scrollbars.
                if (awaitingLayout) { queueInitialDeal(); }
            }
        });
        setCenter(new StackPane(scroll, dragOverlay, dealOverlay));
        Label note = MainMenuView.label("SC / 001     /     SOLITAIRE COLLECTION", "small-label");
        note.getStyleClass().add("board-footer");
        Button again = new Button("NEW GAME");
        again.setOnAction(event -> startNewGame());
        Button menu = new Button("Return to menu");
        menu.setOnAction(event -> { cancelDealing(); interactions.cancel(); onBack.run(); });
        HBox options = new HBox(12, again, menu);
        completion.getChildren().addAll(MainMenuView.label("GAME COMPLETE.", "board-title"), options);
        completion.getStyleClass().add("completion-message");
        completion.setVisible(false);
        completion.setManaged(false);
        setBottom(new VBox(8, completion, note));
    }

    /** Cancels pending gestures and rebuilds presentation from the current model piles. */
    public final void refresh() {
        dealGeneration++;
        pendingDeal = false;
        dealing.cancel();
        interactions.reset();
        pileViews.clear();
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

    protected abstract CardPile stockPile();

    protected abstract void playStock();

    protected final PileView pileView(CardPile pile) { return pileViews.get(pile); }

    protected final void animateDeal(List<CardView> cards) {
        dealing.play(pileView(stockPile()), cards, this::afterAction);
    }

    private void cancelDealing() {
        if (pendingDeal || dealing.isActive()) { afterAction(); }
    }

    /** Schedule after attachment/layout, and invalidate the callback on any refresh. */
    public final void beginInitialDeal() {
        if (getScene() == null) { return; }
        getScene().getRoot().applyCss();
        getScene().getRoot().layout();
        refresh();
        queueInitialDeal();
    }

    private void queueInitialDeal() {
        pendingDeal = true;
        int generation = dealGeneration;
        // Hide immediately so the completed tableau is never flashed before dealing.
        game.getTableau().forEach(pile -> pileView(pile).cardViews().values()
                .forEach(card -> card.setVisible(false)));
        Platform.runLater(() -> {
            if (!pendingDeal || generation != dealGeneration || getScene() == null) { return; }
            pendingDeal = false;
            List<CardView> cards = new ArrayList<>();
            // Row-wise traversal gives Klondike's successive passes and Spider's 10/10/.../4.
            int rows = game.getTableau().stream().mapToInt(CardPile::size).max().orElse(0);
            for (int row = 0; row < rows; row++) {
                for (CardPile pile : game.getTableau()) {
                    CardView card = pileView(pile).cardViews().get(row);
                    if (card != null) { cards.add(card); }
                }
            }
            animateDeal(cards);
        });
    }

    private void startNewGame() {
        cancelDealing();
        interactions.cancel();
        game.startNewGame();
        afterAction();
        scroll.setVvalue(0);
        scroll.setHvalue(0);
        beginInitialDeal();
    }

    protected final void afterAction() {
        refresh();
        boolean won = game.isWon();
        completion.setVisible(won);
        completion.setManaged(won);
    }

    protected final double addPile(CardPile pile, double x, double y, double width,
                                   PileView.Arrangement arrangement, int wasteCount, String name) {
        PileView view = new PileView(pile, width, arrangement, wasteCount);
        view.setManaged(false);
        view.resize(view.prefWidth(-1), view.prefHeight(-1));
        view.relocate(x, y);
        view.setAccessibleText(name + (pile.isEmpty() ? ", empty" : ""));
        surface.getChildren().add(view);
        pileViews.put(pile, view);
        interactions.register(pile, view, pile == stockPile() ? this::playStock : null);
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
