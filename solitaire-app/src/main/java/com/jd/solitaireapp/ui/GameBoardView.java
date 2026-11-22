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

/**
 * Shared board controls, gesture handling, rendering, and deal animation lifecycle.
 * Model operations determine legal play; view operations run on the JavaFX application thread.
 */
public abstract class GameBoardView extends BorderPane {
    /**
     * Model used to validate actions, restore history, and detect completion.
     */
    protected final SolitaireGame game;
    /**
     * Scrollable area containing the rendered piles and captions.
     */
    protected final Pane surface = new Pane();
    /**
     * Non-scrolling header area enabled for ten-column boards.
     */
    protected final Pane fixedArea = new Pane();
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

    /**
     * Builds controls and interaction handlers; subclasses render their piles after initialization.
     * @param mode display labels for the board
     * @param game model backing this board
     * @param columns tableau column count used for sizing; ten enables the fixed area
     * @param onBack callback for menu requests
     * @throws NullPointerException if mode is null
     */
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
        fixedArea.addEventFilter(MouseEvent.ANY, event -> {
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
        backButton.setOnAction(event -> onBack.run());
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
        fixedArea.setVisible(columns == 10);
        fixedArea.setManaged(columns == 10);
        setTop(new VBox(header, fixedArea));
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
        menu.setOnAction(event -> onBack.run());
        HBox options = new HBox(12, again, menu);
        completion.getChildren().addAll(MainMenuView.label("GAME COMPLETE.", "board-title"), options);
        completion.getStyleClass().add("completion-message");
        completion.setVisible(false);
        completion.setManaged(false);
        setBottom(new VBox(8, completion, note));
    }

    /**
     * Invalidates queued deals, cancels animation and gestures, and rebuilds piles
     * from the current model at the viewport size. Does not change model state
     * or update the completion message.
     */
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
        fixedArea.getChildren().clear();
        double bottom = renderPiles(left, width, gap);
        surface.setMinSize(margin * 2 + columns * width + (columns - 1) * gap, bottom + 28);
        surface.setPrefSize(boardWidth, bottom + 28);
    }

    /**
     * Populates the cleared board areas with the current model piles and captions.
     * @param left horizontal origin in layout units
     * @param cardWidth width of each card
     * @param gap horizontal space between columns
     * @return bottom edge of the scrolling content in surface coordinates
     */
    protected abstract double renderPiles(double left, double cardWidth, double gap);

    /**
     * Identifies the pile whose clicks invoke the stock action.
     * @return the game's stock pile
     */
    protected abstract CardPile stockPile();

    /**
     * Attempts the game-specific stock action and updates presentation on success.
     */
    protected abstract void playStock();

    /**
     * Looks up a pile by identity in the current view map.
     * @param pile model pile to look up
     * @return its mapped view, or null if no entry exists
     */
    protected final PileView pileView(CardPile pile) { return pileViews.get(pile); }

    /**
     * Animates presentation copies from the mapped stock view, then refreshes the board.
     * Replaces any active deal; model changes must already have been applied.
     * @param cards destination views in deal order; empty completes immediately
     * @throws NullPointerException if cards is null, or a nonempty deal has no mapped stock view
     */
    protected final void animateDeal(List<CardView> cards) {
        dealing.play(pileView(stockPile()), cards, this::afterAction);
    }

    private void cancelDealing() {
        if (pendingDeal || dealing.isActive()) { afterAction(); }
    }

    /**
     * Lays out and refreshes an attached board, then queues the initial tableau animation.
     * Does nothing while detached. A refresh invalidates the queued callback;
     * cancellation reveals the current model position without undoing the deal.
     */
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

    /**
     * Refreshes the board and shows or hides the completion message from the model's win state.
     */
    protected final void afterAction() {
        refresh();
        boolean won = game.isWon();
        completion.setVisible(won);
        completion.setManaged(won);
    }

    /**
     * Creates a pile view, adds it to the scrolling surface, and registers mouse handling.
     * Stock piles invoke the stock action; other piles support selection and transfers.
     * @param pile model pile to render
     * @param x horizontal position in the parent
     * @param y vertical position in the parent
     * @param width card width in layout units
     * @param arrangement layout of cards within the pile
     * @param wasteCount visible waste positions, used only for WASTE
     * @param name accessible pile description
     * @return bottom edge of the view in parent coordinates
     */
    protected final double addPile(CardPile pile, double x, double y, double width,
                                   PileView.Arrangement arrangement, int wasteCount, String name) {
        return addPile(surface, pile, x, y, width, arrangement, wasteCount, name);
    }

    /**
     * Creates a pile view, adds it to the supplied pane, and registers mouse handling.
     * Stock piles invoke the stock action; other piles support selection and transfers.
     * @param parent pane receiving the view
     * @param pile model pile to render
     * @param x horizontal position in the parent
     * @param y vertical position in the parent
     * @param width card width in layout units
     * @param arrangement layout of cards within the pile
     * @param wasteCount visible waste positions, used only for WASTE
     * @param name accessible pile description
     * @return bottom edge of the view in parent coordinates
     */
    protected final double addPile(Pane parent, CardPile pile, double x, double y, double width,
                                   PileView.Arrangement arrangement, int wasteCount, String name) {
        PileView view = new PileView(pile, width, arrangement, wasteCount);
        view.setManaged(false);
        view.resize(view.prefWidth(-1), view.prefHeight(-1));
        view.relocate(x, y);
        view.setAccessibleText(name + (pile.isEmpty() ? ", empty" : ""));
        parent.getChildren().add(view);
        pileViews.put(pile, view);
        interactions.register(pile, view, pile == stockPile() ? this::playStock : null);
        return y + view.prefHeight(-1);
    }

    /**
     * Adds an unmanaged, styled caption to the scrolling surface.
     * @param text caption text
     * @param x horizontal position in the parent
     * @param y vertical position in the parent
     */
    protected final void caption(String text, double x, double y) {
        caption(surface, text, x, y);
    }

    /**
     * Adds an unmanaged, styled caption to the supplied pane.
     * @param parent pane receiving the caption
     * @param text caption text
     * @param x horizontal position in the parent
     * @param y vertical position in the parent
     */
    protected final void caption(Pane parent, String text, double x, double y) {
        Label label = MainMenuView.label(text, "pile-caption");
        label.setManaged(false);
        label.autosize();
        label.relocate(x, y);
        parent.getChildren().add(label);
    }

    /**
     * Clears gesture targets and rendered board areas when leaving the game.
     * Scene detachment separately cancels pending or running deal animation.
     */
    public void dispose() {
        interactions.reset();
        surface.getChildren().clear();
        fixedArea.getChildren().clear();
    }

    /**
     * Requests keyboard focus for the return-to-menu control.
     */
    public void focusBackButton() {
        backButton.requestFocus();
    }
}
