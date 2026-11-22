package com.jd.solitaireapp.ui;

import com.jd.solitaireapp.CardPile;
import com.jd.solitaireapp.SolitaireGame;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/** Shared mouse gestures. All legality and state mutations are delegated to the game. */
final class BoardInteractions {
    /** Model pile, its rendered view, and optional stock-click action. */
    private record Target(CardPile pile, PileView view, Runnable stockAction) { }
    /** Bottom index of a selected top segment in its source pile. */
    private record Selection(Target source, int index) {
        int count() { return source.pile().size() - index; }
    }

    private final SolitaireGame game;
    private final Pane overlay;
    private final Node viewport;
    private final Runnable afterAction;
    private final List<Target> targets = new ArrayList<>();
    private Selection selected;
    private Selection pressed;
    private Point2D pressPoint;
    private Pane dragGroup;
    private Point2D dragOrigin;
    private boolean dragging;

    /**
     * Creates gesture handling that delegates pickup and transfer rules to the model.
     * @param game model to validate and execute moves
     * @param overlay dedicated pane for dragged card copies
     * @param viewport node containing the scroll viewport used for drop bounds
     * @param afterAction callback after a successful transfer
     */
    BoardInteractions(SolitaireGame game, Pane overlay, Node viewport, Runnable afterAction) {
        this.game = game;
        this.overlay = overlay;
        this.viewport = viewport;
        this.afterAction = afterAction;
    }

    /**
     * Installs click selection and drag handlers on a rendered pile.
     * @param pile source or destination model pile
     * @param view view receiving mouse events
     * @param stockAction action for stock clicks, or null for ordinary movable piles
     */
    void register(CardPile pile, PileView view, Runnable stockAction) {
        Target target = new Target(pile, view, stockAction);
        targets.add(target);
        view.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) { return; }
            pressPoint = new Point2D(event.getSceneX(), event.getSceneY());
            pressed = selectionAt(target, event);
            event.consume();
        });
        view.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown() || pressed == null) { return; }
            if (!dragging && pressPoint.distance(event.getSceneX(), event.getSceneY()) >= 5) {
                startDrag();
            }
            if (dragging) {
                Point2D current = overlay.sceneToLocal(event.getSceneX(), event.getSceneY());
                Point2D start = overlay.sceneToLocal(pressPoint);
                dragGroup.relocate(dragOrigin.getX() + current.getX() - start.getX(),
                        dragOrigin.getY() + current.getY() - start.getY());
                highlight(destinationAt(event.getSceneX(), event.getSceneY()));
            }
            event.consume();
        });
        view.setOnMouseReleased(event -> {
            if (event.getButton() != MouseButton.PRIMARY || pressPoint == null) { return; }
            if (dragging) {
                Target destination = destinationAt(event.getSceneX(), event.getSceneY());
                Selection moving = pressed;
                cancel();
                if (destination != null && game.moveCards(moving.source().pile(), destination.pile(), moving.count())) {
                    afterAction.run();
                }
            } else if (pressPoint.distance(event.getSceneX(), event.getSceneY()) < 5) {
                click(target, pressed);
            }
            pressed = null;
            pressPoint = null;
            event.consume();
        });
        // A release may rebuild the board. Never interpret its synthesized click as a second action.
        view.setOnMouseClicked(MouseEvent::consume);
    }

    private Selection selectionAt(Target target, MouseEvent event) {
        if (target.stockAction() != null) { return null; }
        List<Integer> indices = new ArrayList<>(target.view().cardViews().keySet());
        for (int n = indices.size() - 1; n >= 0; n--) {
            int index = indices.get(n);
            CardView card = target.view().cardViews().get(index);
            if (card.contains(card.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                Selection candidate = new Selection(target, index);
                // Pickup checks the source only; dropping separately checks the destination.
                return game.canPickUp(target.pile(), candidate.count())
                        ? candidate : null;
            }
        }
        return null;
    }

    private void click(Target target, Selection candidate) {
        if (target.stockAction() != null) {
            cancel();
            target.stockAction().run();
        } else if (selected != null && target != selected.source()
                && game.moveCards(selected.source().pile(), target.pile(), selected.count())) {
            cancel();
            afterAction.run();
        } else {
            Selection previous = selected;
            cancel();
            if (candidate != null && !candidate.equals(previous)) {
                selected = candidate;
                markSelection(candidate, true);
            }
        }
    }

    private void startDrag() {
        if (selected != null) { markSelection(selected, false); }
        selected = null;
        dragging = true;
        dragGroup = new Pane();
        dragGroup.setManaged(false);
        dragGroup.setMouseTransparent(true);
        dragGroup.getStyleClass().add("dragged-cards");
        CardView first = pressed.source().view().cardViews().get(pressed.index());
        dragOrigin = overlay.sceneToLocal(first.localToScene(0, 0));
        for (var entry : pressed.source().view().cardViews().entrySet()) {
            if (entry.getKey() < pressed.index()) { continue; }
            CardView original = entry.getValue();
            CardView copy = new CardView(pressed.source().pile().getCards().get(entry.getKey()), original.getWidth());
            copy.setManaged(false);
            Point2D point = overlay.sceneToLocal(original.localToScene(0, 0));
            copy.relocate(point.getX() - dragOrigin.getX(), point.getY() - dragOrigin.getY());
            dragGroup.getChildren().add(copy);
            original.setOpacity(0);
        }
        overlay.getChildren().add(dragGroup);
    }

    private Target destinationAt(double sceneX, double sceneY) {
        Node clip = viewport.lookup(".viewport");
        if (clip == null || !clip.contains(clip.sceneToLocal(sceneX, sceneY))) { return null; }
        for (Target target : targets) {
            if (target.view().contains(target.view().sceneToLocal(sceneX, sceneY))
                    && game.canMove(pressed.source().pile(), target.pile(), pressed.count())) {
                return target;
            }
        }
        return null;
    }

    private void highlight(Target destination) {
        for (Target target : targets) {
            target.view().pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("drop-target"), target == destination);
        }
    }

    private void markSelection(Selection selection, boolean active) {
        selection.source().view().cardViews().forEach((index, view) -> {
            if (index >= selection.index()) {
                view.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("selected"), active);
            }
        });
    }

    /**
     * Clears selection and drag state, restores card opacity, and empties the overlay
     * without changing model piles or undo history.
     */
    void cancel() {
        if (selected != null) { markSelection(selected, false); }
        targets.forEach(target -> target.view().cardViews().values().forEach(view -> view.setOpacity(1)));
        highlight(null);
        overlay.getChildren().clear();
        selected = null;
        pressed = null;
        pressPoint = null;
        dragGroup = null;
        dragging = false;
    }

    /**
     * Cancels an active drag; otherwise clears the current press but preserves click selection.
     */
    void cancelGesture() {
        if (dragging) {
            cancel();
        } else {
            // Keep a click selection while scrolling to an off-screen destination.
            pressed = null;
            pressPoint = null;
        }
    }

    /**
     * Cancels transient gestures and forgets registered targets before a board rebuild.
     */
    void reset() {
        cancel();
        targets.clear();
    }
}
