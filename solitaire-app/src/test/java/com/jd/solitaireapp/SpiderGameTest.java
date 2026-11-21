package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class SpiderGameTest {
    private SpiderGame game;
    private CardPile from, to;

    @BeforeEach void arrangeEmptyGame() {
        game = new SpiderGame(1);
        clear(game);
        from = game.getTableau().get(0);
        to = game.getTableau().get(1);
    }

    private void assertInitialGame(SpiderGame initial) {
        assertInitialTableau(initial, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5);
        assertEquals(50, initial.getStock().size());
        assertTrue(initial.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        List<Card> cards = piles(initial).stream().flatMap(p -> p.getCards().stream()).toList();
        assertEquals(104, cards.size());
        assertEquals(104, new HashSet<>(cards).size());
        assertTrue(cards.stream().allMatch(c -> c.getSuit() == Suit.SPADES));
        for (int rank = 1; rank <= 13; rank++) {
            int expected = rank;
            assertEquals(8, cards.stream().filter(c -> c.getRank() == expected).count());
        }
        assertEquals(0, initial.getMoveCount());
        assertEquals(0, initial.getCompletedRuns());
        assertFalse(initial.isWon());
    }

    private void fillColumns() {
        game.getTableau().forEach(p -> set(p, up(7)));
    }

    private void fillStock(int count) {
        set(game.getStock());
        for (int i = 0; i < count; i++) {
            game.getStock().addCards(List.of(new Card(Suit.SPADES, 4)));
        }
    }

    @Test void constructorDealsCompleteOneSuitGame() { assertInitialGame(new SpiderGame(1)); }

    @Test void invalidSuitCountsAreRejectedAndSupportedGamesCanBeCreated() {
        for (int count : new int[]{-1, 0, 3, 5, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new SpiderGame(count));
        }
        for (int count : new int[]{1, 2, 4}) {
            assertDoesNotThrow(() -> new SpiderGame(count));
        }
    }

    @Test void descendingSegmentMovesInOrderAndExposesRemainingSource() {
        Card hidden = new Card(Suit.SPADES, 2), eight = up(8), seven = up(7), nine = up(9);
        set(from, hidden, eight, seven);
        set(to, nine);
        legalMove(game, from, to, 2);
        assertEquals(List.of(nine, eight, seven), to.getCards());
        assertEquals(List.of(hidden), from.getCards());
        assertTrue(hidden.isFaceUp());
        assertEquals(0, game.getCompletedRuns());
    }

    @Test void anyRankOrDescendingSegmentMayEnterEmptyColumn() {
        Card two = up(2), ace = up(1);
        set(from, two, ace);
        legalMove(game, from, to, 2);
        assertEquals(List.of(two, ace), to.getCards());
        assertTrue(from.isEmpty());
        legalMove(game, to, from, 1);
        assertSame(ace, from.peek());
    }

    @Test void onlyMovedTopSegmentNeedsToDescend() {
        Card bottom = up(1), six = up(6);
        set(from, bottom, six);
        set(to, up(7));
        legalMove(game, from, to, 1);
        assertSame(bottom, from.peek());
    }

    @Test void equalAscendingAndSkippedRanksWithinSegmentAreRejected() {
        for (int topRank : new int[]{8, 9, 6}) {
            set(from, up(8), up(topRank));
            set(to, up(9));
            rejectedMove(game, from, to, 2);
        }
    }

    @Test void hiddenCardsAnywhereInMovedSegmentAreRejected() {
        set(from, up(8), new Card(Suit.SPADES, 7));
        set(to, up(9));
        rejectedMove(game, from, to, 2);
        from.peek().setFaceUp(true);
        from.getCards().getFirst().setFaceUp(false);
        rejectedMove(game, from, to, 2);
    }

    @Test void hiddenOrWrongRankDestinationIsRejected() {
        set(from, up(8));
        set(to, new Card(Suit.SPADES, 9));
        rejectedMove(game, from, to, 1);
        for (int rank : new int[]{8, 7, 10}) {
            set(to, up(rank));
            rejectedMove(game, from, to, 1);
        }
    }

    @Test void nullIdenticalForeignEmptyAndInvalidCountRequestsPreserveState() {
        set(from, up(8));
        rejectedMove(game, null, to, 1);
        rejectedMove(game, from, null, 1);
        rejectedMove(game, from, from, 1);
        CardPile foreign = new CardPile();
        Card foreignCard = up(8);
        set(foreign, foreignCard);
        rejectedMove(game, foreign, to, 1);
        rejectedMove(game, from, foreign, 1);
        assertEquals(List.of(foreignCard), foreign.getCards());
        rejectedMove(game, to, from, 1);
        for (int count : new int[]{-1, 0, 2, Integer.MAX_VALUE}) {
            rejectedMove(game, from, to, count);
        }
    }

    @Test void stockIsNotAMoveSourceOrDestination() {
        set(from, up(8));
        set(game.getStock(), up(9));
        rejectedMove(game, from, game.getStock(), 1);
        rejectedMove(game, game.getStock(), to, 1);
    }

    @Test void rowDealsExactlyOneTopStockCardToEachColumnInOrder() {
        fillColumns();
        fillStock(20);
        List<Card> stockBefore = List.copyOf(game.getStock().getCards());
        List<Card> oldTops = game.getTableau().stream().map(CardPile::peek).toList();
        assertTrue(game.dealNextRow());
        for (int i = 0; i < 10; i++) {
            CardPile column = game.getTableau().get(i);
            assertEquals(List.of(oldTops.get(i), stockBefore.get(19 - i)), column.getCards());
            assertTrue(column.peek().isFaceUp());
        }
        assertEquals(stockBefore.subList(0, 10), game.getStock().getCards());
        assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        assertEquals(1, game.getMoveCount());
        assertEquals(0, game.getCompletedRuns());
    }

    @Test void exactlyTenStockCardsCanBeDealtThenEmptyStockIsRejected() {
        fillColumns();
        fillStock(10);
        assertTrue(game.dealNextRow());
        assertTrue(game.getStock().isEmpty());
        rejected(game, game::dealNextRow);
    }

    @Test void insufficientStockPreservesAllPilesEvenWhenColumnsAreFull() {
        fillColumns();
        fillStock(9);
        rejected(game, game::dealNextRow);
    }

    @Test void emptyColumnAnywhereRejectsEntireRowBeforeDealing() {
        for (int index : new int[]{0, 5, 9}) {
            fillColumns();
            fillStock(10);
            set(game.getTableau().get(index));
            rejected(game, game::dealNextRow);
        }
    }

    @Test void allFiveStockRowsCanBeDealtWithoutDependingOnShuffle() {
        fillColumns();
        fillStock(50);
        for (int row = 1; row <= 5; row++) {
            assertTrue(game.dealNextRow());
            assertEquals(50 - row * 10, game.getStock().size());
            assertEquals(row, game.getMoveCount());
        }
        assertTrue(game.getTableau().stream().allMatch(p -> p.size() == 6));
        rejected(game, game::dealNextRow);
    }

    @Test void directRunRemovalCountsOneAndExposesRemainingTopWithoutCountingMove() {
        Card hidden = new Card(Suit.SPADES, 5);
        set(from, hidden);
        from.addCards(run());
        assertEquals(1, game.removeCompletedRuns());
        assertEquals(List.of(hidden), from.getCards());
        assertTrue(hidden.isFaceUp());
        assertEquals(1, game.getCompletedRuns());
        assertEquals(0, game.getMoveCount());
        assertEquals(0, game.removeCompletedRuns());
    }

    @Test void completeRunWithNoUnderlyingCardEmptiesColumn() {
        from.addCards(run());
        assertEquals(1, game.removeCompletedRuns());
        assertTrue(from.isEmpty());
        assertNull(from.peek());
    }

    @Test void stackedAndSeparateRunsAreAllRemovedInOneCall() {
        from.addCards(run());
        from.peek().setFaceUp(false); // Removing the upper run will reveal this Ace.
        from.addCards(run());
        to.addCards(run());
        assertEquals(3, game.removeCompletedRuns());
        assertTrue(from.isEmpty());
        assertTrue(to.isEmpty());
        assertEquals(3, game.getCompletedRuns());
        assertEquals(0, game.getMoveCount());
    }

    @Test void shortHiddenAndMisorderedRunsAreNotRemoved() {
        from.addCards(run().subList(0, 12));
        rejectedRemoval();
        set(from);
        List<Card> hiddenRun = run();
        hiddenRun.get(6).setFaceUp(false);
        from.addCards(hiddenRun);
        rejectedRemoval();
        set(from);
        List<Card> brokenRun = run();
        brokenRun.set(6, up(8));
        from.addCards(brokenRun);
        rejectedRemoval();
    }

    private void rejectedRemoval() {
        Observation before = observe(game);
        assertEquals(0, game.removeCompletedRuns());
        assertEquals(before, observe(game));
    }

    @Test void buriedCompleteRunIsNotRemovedUntilExposed() {
        from.addCards(run());
        from.addCards(List.of(up(5)));
        rejectedRemoval();
        legalMove(game, from, to, 1);
        assertTrue(from.isEmpty());
        assertEquals(1, game.getCompletedRuns());
    }

    @Test void transferCompletingRunRemovesItAutomaticallyAndCountsOnlyTransfer() {
        to.addCards(run().subList(0, 12));
        Card hidden = new Card(Suit.SPADES, 5);
        set(from, hidden, up(1));
        legalMove(game, from, to, 1);
        assertTrue(to.isEmpty());
        assertTrue(hidden.isFaceUp());
        assertEquals(1, game.getCompletedRuns());
        assertEquals(1, game.getMoveCount());
    }

    @Test void stockRowCompletingRunRemovesItAutomaticallyAndCountsOnlyDeal() {
        fillColumns();
        set(from);
        from.addCards(run().subList(0, 12));
        fillStock(9);
        game.getStock().addCards(List.of(new Card(Suit.SPADES, 1)));
        assertTrue(game.dealNextRow());
        assertTrue(from.isEmpty());
        assertEquals(1, game.getCompletedRuns());
        assertEquals(1, game.getMoveCount());
        assertTrue(game.getStock().isEmpty());
    }

    @Test void winOccursAtEighthRemovedRun() {
        assertFalse(game.isWon());
        for (int i = 1; i <= 8; i++) {
            from.addCards(run());
            assertEquals(1, game.removeCompletedRuns());
            assertEquals(i, game.getCompletedRuns());
            assertEquals(i == 8, game.isWon());
        }
        assertEquals(0, game.getMoveCount());
    }

    @Test void resetClearsRunsAndMovesAndReplacesColumnsWithFreshDeal() {
        List<CardPile> view = game.getTableau();
        List<CardPile> old = new ArrayList<>(view);
        from.addCards(run());
        assertEquals(1, game.removeCompletedRuns());
        set(from, up(8));
        legalMove(game, from, to, 1);
        game.startNewGame();
        assertInitialGame(game);
        assertEquals(10, view.size());
        assertTrue(view.stream().noneMatch(old::contains));
        rejectedMove(game, to, view.getFirst(), 1);
        assertThrows(UnsupportedOperationException.class, view::clear);
    }
}
