package com.jd.solitaireapp.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/** Owns one cancellable deal. Copies only the already-rendered face or card back. */
final class CardDealAnimation {
    private static final Duration TRAVEL = Duration.millis(190);
    private static final double STAGGER_MILLIS = 45;
    private final Pane overlay;
    private final List<CardView> destinations = new ArrayList<>();
    private ParallelTransition running;

    /**
     * Creates an animation controller for a dedicated overlay.
     * @param overlay pane holding temporary moving card copies
     */
    CardDealAnimation(Pane overlay) { this.overlay = overlay; }

    /**
     * Reports whether this controller holds a running deal.
     * @return true until completion or cancellation clears the animation
     */
    boolean isActive() { return running != null; }

    /**
     * Cancels the previous deal, hides destination views, and animates copies of
     * their already-rendered faces or backs from the stock. Does not mutate cards.
     * @param stock attached node supplying the animation origin
     * @param cards destination views in stagger order
     * @param finished callback after normal completion, or immediately for an empty list;
     *         not invoked when cancelled
     */
    void play(Node stock, List<CardView> cards, Runnable finished) {
        cancel();
        if (cards.isEmpty()) { finished.run(); return; }
        Point2D origin = overlay.sceneToLocal(stock.localToScene(0, 0));
        running = new ParallelTransition();
        for (int i = 0; i < cards.size(); i++) {
            CardView target = cards.get(i);
            Point2D end = overlay.sceneToLocal(target.localToScene(0, 0));
            CardView moving = new CardView(target);
            moving.setManaged(false);
            moving.setMouseTransparent(true);
            moving.relocate(origin.getX(), origin.getY());
            moving.setVisible(false);
            overlay.getChildren().add(moving);
            destinations.add(target);
            target.setVisible(false);
            PauseTransition delay = new PauseTransition(Duration.millis(1 + i * STAGGER_MILLIS));
            delay.setOnFinished(event -> { moving.setVisible(true); moving.toFront(); });
            TranslateTransition travel = new TranslateTransition(TRAVEL, moving);
            travel.setToX(end.getX() - origin.getX());
            travel.setToY(end.getY() - origin.getY());
            travel.setInterpolator(Interpolator.EASE_BOTH);
            travel.setOnFinished(event -> {
                target.setVisible(true);
                overlay.getChildren().remove(moving);
            });
            running.getChildren().add(new SequentialTransition(delay, travel));
        }
        running.setOnFinished(event -> { cancel(); finished.run(); });
        running.play();
    }

    /** Stops the deal without its completion callback, reveals destinations, and clears the overlay. */
    void cancel() {
        if (running != null) {
            running.stop();
            running.setOnFinished(null);
            running.getChildren().clear();
            running = null;
        }
        destinations.forEach(card -> card.setVisible(true));
        destinations.clear();
        overlay.getChildren().clear();
    }
}
