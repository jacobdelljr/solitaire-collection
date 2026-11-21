package com.jd.solitaireapp;

import java.util.List;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class SolitaireGameTest {
    /** Isolates base transfer/reset behavior from the concrete game rules. */
    private static class ProbeGame extends SolitaireGame {
        final CardPile source = new CardPile();
        final CardPile target = new CardPile();
        boolean allowed;
        int hooks;
        int countAtHook;

        ProbeGame() { tableau.addAll(List.of(source, target)); }
        @Override public boolean canMove(CardPile from, CardPile to, int count) { return allowed; }
        @Override public boolean isWon() { return false; }
        @Override protected void afterMove() {
            super.afterMove();
            hooks++;
            countAtHook = getMoveCount();
        }
    }

    @Test void transferPreservesOrderFlipsRemainingTopAndCountsBeforeHook() {
        ProbeGame game = new ProbeGame();
        Card hidden = new Card(Suit.CLUBS, 9), first = up(8), last = up(7), base = up(10);
        set(game.source, hidden, first, last);
        set(game.target, base);
        game.allowed = true;
        legalMove(game, game.source, game.target, 2);
        assertEquals(List.of(base, first, last), game.target.getCards());
        assertEquals(List.of(hidden), game.source.getCards());
        assertTrue(hidden.isFaceUp());
        assertEquals(1, game.hooks);
        assertEquals(1, game.countAtHook);
    }

    @Test void movingEntirePileHandlesEmptySource() {
        ProbeGame game = new ProbeGame();
        Card card = up(1);
        set(game.source, card);
        game.allowed = true;
        legalMove(game, game.source, game.target, 1);
        assertTrue(game.source.isEmpty());
        assertSame(card, game.target.peek());
    }

    @Test void nonTableauSourceIsNotAutomaticallyFlipped() {
        ProbeGame game = new ProbeGame();
        CardPile external = new CardPile();
        Card hidden = new Card(Suit.CLUBS, 3);
        set(external, hidden, up(2));
        game.allowed = true;
        assertTrue(game.moveCards(external, game.target, 1));
        assertFalse(hidden.isFaceUp());
        assertEquals(1, game.getMoveCount());
    }

    @Test void rejectionDoesNotTransferFlipCountOrCallHook() {
        ProbeGame game = new ProbeGame();
        set(game.source, new Card(Suit.CLUBS, 3), up(2));
        rejectedMove(game, game.source, game.target, 1);
        assertEquals(0, game.hooks);
    }

    @Test void baseResetClearsTableauAndCounterAndExistingViewUpdates() {
        ProbeGame game = new ProbeGame();
        List<CardPile> view = game.getTableau();
        assertThrows(UnsupportedOperationException.class, view::clear);
        game.recordMove();
        game.recordMove();
        assertEquals(2, game.getMoveCount());
        game.startNewGame();
        assertTrue(view.isEmpty());
        assertEquals(0, game.getMoveCount());
    }

    @Test void emptyUndoAndSnapshotCaptureDoNotMutateState() {
        ProbeGame game = new ProbeGame();
        set(game.source, up(1));
        Observation before = observe(game);
        assertFalse(game.undo());
        assertEquals(before, observe(game));
        GameSnapshot snapshot = GameSnapshot.capture(game);
        assertNotNull(snapshot);
        assertEquals(before, observe(game));
        assertFalse(game.undo(), "Capturing a snapshot must not create undo history");
        game.allowed = true;
        legalMove(game, game.source, game.target, 1);
        snapshot.restore(game);
        assertEquals(before, observe(game));
    }
}
