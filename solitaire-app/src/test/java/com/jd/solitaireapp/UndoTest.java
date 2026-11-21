package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

/** Step 6 red tests: successful undo returns true; exhausted history returns false. */
class UndoTest {
    @Nested class TurnOne extends KlondikeTests { TurnOne() { super(1); } }
    @Nested class TurnThree extends KlondikeTests { TurnThree() { super(3); } }
    @Nested class OneSuit extends SpiderTests { OneSuit() { super(1); } }
    @Nested class TwoSuits extends SpiderTests { TwoSuits() { super(2); } }
    @Nested class FourSuits extends SpiderTests { FourSuits() { super(4); } }

    private static void undoTo(SolitaireGame game, Observation expected) {
        assertTrue(game.undo(), "A successful action must be undoable");
        assertEquals(expected, observe(game), "Undo must restore every pile, orientation and counter");
    }

    private static void noUndo(SolitaireGame game) {
        Observation before = observe(game);
        assertFalse(game.undo(), "Empty history must return false");
        assertEquals(before, observe(game), "Empty-history undo must not modify the game");
    }

    // Inherited tests exercise all five configurations without adding JUnit params.
    abstract static class CommonTests {
        abstract SolitaireGame game();

        private SolitaireGame tableauFixture() {
            SolitaireGame game = game();
            clear(game);
            Suit queenSuit = game instanceof KlondikeGame ? Suit.HEARTS : Suit.SPADES;
            set(game.getTableau().get(0), new Card(Suit.SPADES, 5), up(13), up(queenSuit, 12));
            return game;
        }

        @Test void tableauSegmentUndoRestoresOrderAndAutomaticFlip() {
            SolitaireGame game = tableauFixture();
            Observation before = observe(game);
            legalMove(game, game.getTableau().get(0), game.getTableau().get(1), 2);
            assertTrue(game.getTableau().get(0).peek().isFaceUp());
            undoTo(game, before);
            noUndo(game);
        }

        @Test void emptyHistoryLeavesDeterministicPositionUnchanged() {
            SolitaireGame game = tableauFixture();
            noUndo(game);
            noUndo(game);
        }

        @Test void invalidMoveDoesNotCreateHistory() {
            SolitaireGame game = tableauFixture();
            rejectedMove(game, game.getTableau().get(0), game.getTableau().get(1), 3);
            noUndo(game);
        }

        @Test void invalidMoveDoesNotHideOrDuplicatePreviousHistory() {
            SolitaireGame game = tableauFixture();
            Observation before = observe(game);
            legalMove(game, game.getTableau().get(0), game.getTableau().get(1), 2);
            rejectedMove(game, game.getTableau().get(0), game.getTableau().get(2), 0);
            undoTo(game, before);
            noUndo(game);
        }

        @Test void consecutiveUndosRestoreStatesInReverseOrder() {
            SolitaireGame game = tableauFixture();
            Observation initial = observe(game);
            legalMove(game, game.getTableau().get(0), game.getTableau().get(1), 2);
            Observation first = observe(game);
            legalMove(game, game.getTableau().get(1), game.getTableau().get(2), 2);
            assertEquals(2, game.getMoveCount());
            undoTo(game, first);
            undoTo(game, initial);
            noUndo(game);
        }

        @Test void newGameClearsPreviousUndoHistory() {
            SolitaireGame game = tableauFixture();
            legalMove(game, game.getTableau().get(0), game.getTableau().get(1), 2);
            game.startNewGame();
            assertEquals(0, game.getMoveCount());
            // Observe the new deal; no assertion depends on its shuffled order.
            noUndo(game);
        }

        @Test void snapshotRestoresCapturedValuesAfterCardsAndPilesChange() {
            SolitaireGame game = tableauFixture();
            Observation before = observe(game);
            GameSnapshot snapshot = GameSnapshot.capture(game);
            assertEquals(before, observe(game), "Capture must be read-only");
            legalMove(game, game.getTableau().get(0), game.getTableau().get(1), 2);
            snapshot.restore(game);
            assertEquals(before, observe(game), "Snapshot must retain values, not mutable list aliases");
        }
    }

    abstract static class KlondikeTests extends CommonTests {
        private final int drawCount;
        KlondikeTests(int drawCount) { this.drawCount = drawCount; }
        @Override KlondikeGame game() { return new KlondikeGame(drawCount); }

        private KlondikeGame empty() {
            KlondikeGame game = game();
            clear(game);
            return game;
        }

        @Test void undoTableauToFoundationRestoresHiddenCard() {
            KlondikeGame game = empty();
            Card hidden = new Card(Suit.CLUBS, 7);
            set(game.getTableau().get(0), hidden, up(1));
            Observation before = observe(game);
            legalMove(game, game.getTableau().get(0), game.getFoundations().get(0), 1);
            assertTrue(hidden.isFaceUp());
            undoTo(game, before);
        }

        @Test void undoWasteToFoundationRestoresBothPileOrders() {
            KlondikeGame game = empty();
            set(game.getFoundations().get(0), up(1));
            set(game.getWaste(), up(Suit.HEARTS, 9), up(2));
            Observation before = observe(game);
            legalMove(game, game.getWaste(), game.getFoundations().get(0), 1);
            undoTo(game, before);
        }

        @Test void undoFoundationToTableauRestoresFoundation() {
            KlondikeGame game = empty();
            set(game.getFoundations().get(0), up(1), up(2));
            set(game.getTableau().get(0), up(Suit.HEARTS, 3));
            Observation before = observe(game);
            legalMove(game, game.getFoundations().get(0), game.getTableau().get(0), 1);
            undoTo(game, before);
        }

        @Test void undoStockDrawRestoresStockWasteAndFaceStates() {
            KlondikeGame game = empty();
            set(game.getStock(), new Card(Suit.SPADES, 4), new Card(Suit.HEARTS, 3),
                    new Card(Suit.CLUBS, 2), new Card(Suit.DIAMONDS, 1));
            set(game.getWaste(), up(8));
            Observation before = observe(game);
            assertTrue(game.drawFromStock());
            assertEquals(1 + drawCount, game.getWaste().size());
            assertEquals(1, game.getMoveCount());
            undoTo(game, before);
            noUndo(game);
        }

        @Test void undoFinalShortDrawAndRecycleInReverseOrder() {
            KlondikeGame game = empty();
            set(game.getStock(), new Card(Suit.SPADES, 2));
            set(game.getWaste(), up(7), up(Suit.HEARTS, 6));
            Observation initial = observe(game);
            assertTrue(game.drawFromStock());
            Observation drawn = observe(game);
            assertTrue(game.recycleStock());
            assertEquals(2, game.getMoveCount());
            assertTrue(game.getWaste().isEmpty());
            assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
            undoTo(game, drawn);
            undoTo(game, initial);
            noUndo(game);
        }

        @Test void unsuccessfulEmptyStockOperationsDoNotCreateHistory() {
            KlondikeGame game = empty();
            rejected(game, game::drawFromStock);
            rejected(game, game::recycleStock);
            noUndo(game);
        }

        @Test void rejectedStockOperationsPreserveExistingHistory() {
            KlondikeGame game = empty();
            set(game.getStock(), new Card(Suit.SPADES, 2));
            Observation initial = observe(game);
            assertTrue(game.drawFromStock());
            Observation drawn = observe(game);
            rejected(game, game::drawFromStock);
            assertTrue(game.recycleStock());
            rejected(game, game::recycleStock);
            undoTo(game, drawn);
            undoTo(game, initial);
            noUndo(game);
        }
    }

    abstract static class SpiderTests extends CommonTests {
        private final int suitCount;
        SpiderTests(int suitCount) { this.suitCount = suitCount; }
        @Override SpiderGame game() { return new SpiderGame(suitCount); }

        private Suit suit(int index) {
            Suit[] suits = {Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS};
            return suits[index % suitCount];
        }

        private SpiderGame rowFixture(int stockSize) {
            SpiderGame game = game();
            clear(game);
            for (int i = 0; i < 10; i++) {
                set(game.getTableau().get(i), new Card(suit(i), 9), up(suit(i), 8));
            }
            for (int i = 0; i < stockSize; i++) {
                game.getStock().addCards(List.of(new Card(suit(i), i % 13 + 1)));
            }
            return game;
        }

        @Test void undoStockRowRestoresEveryColumnAndStockOrder() {
            SpiderGame game = rowFixture(20);
            Observation before = observe(game);
            assertTrue(game.dealNextRow());
            assertEquals(1, game.getMoveCount());
            assertEquals(10, game.getStock().size());
            assertTrue(game.getTableau().stream().allMatch(p -> p.size() == 3 && p.peek().isFaceUp()));
            undoTo(game, before);
            noUndo(game);
        }

        @Test void undoMoveRestoresRemovedRunAndBothAutomaticFlips() {
            SpiderGame game = game();
            clear(game);
            Card sourceHidden = new Card(suit(0), 9), targetHidden = new Card(suit(1), 8);
            CardPile source = game.getTableau().get(0), target = game.getTableau().get(1);
            set(source, sourceHidden, up(suit(1), 1));
            set(target, targetHidden);
            for (int rank = 13; rank >= 2; rank--) { target.addCards(List.of(up(suit(1), rank))); }
            Observation before = observe(game);
            legalMove(game, source, target, 1);
            assertEquals(1, game.getCompletedRuns());
            assertEquals(List.of(targetHidden), target.getCards());
            assertTrue(sourceHidden.isFaceUp());
            assertTrue(targetHidden.isFaceUp());
            undoTo(game, before);
            noUndo(game);
        }

        @Test void undoRowRestoresMultipleRemovedRunsAndEarlierRunCount() {
            SpiderGame game = rowFixture(20);
            // Two runs finish on the first row; all columns remain nonempty for the second.
            for (int column = 0; column < 2; column++) {
                CardPile pile = game.getTableau().get(column);
                set(pile, new Card(suit(column), 7));
                for (int rank = 13; rank >= 2; rank--) { pile.addCards(List.of(up(suit(column), rank))); }
            }
            List<Card> stock = new ArrayList<>();
            for (int i = 19; i >= 0; i--) {
                stock.add(new Card(suit(i % 10), i < 2 ? 1 : 5));
            }
            set(game.getStock(), stock.toArray(Card[]::new));
            Observation initial = observe(game);
            assertTrue(game.dealNextRow());
            assertEquals(2, game.getCompletedRuns());
            assertEquals(1, game.getMoveCount());
            Observation first = observe(game);
            assertTrue(game.dealNextRow());
            assertEquals(2, game.getCompletedRuns());
            undoTo(game, first);
            undoTo(game, initial);
            noUndo(game);
        }

        @Test void insufficientStockDoesNotCreateOrDuplicateHistory() {
            SpiderGame game = rowFixture(19);
            Observation before = observe(game);
            assertTrue(game.dealNextRow());
            rejected(game, game::dealNextRow);
            undoTo(game, before);
            noUndo(game);
        }

        @Test void emptyColumnPreventsDealWithoutCreatingHistory() {
            SpiderGame game = rowFixture(10);
            set(game.getTableau().get(0));
            rejected(game, game::dealNextRow);
            noUndo(game);
        }
    }
}
