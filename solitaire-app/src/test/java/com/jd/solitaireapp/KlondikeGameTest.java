package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class KlondikeGameTest {
    private KlondikeGame game;
    private CardPile from, to, foundation;

    @BeforeEach void arrangeEmptyGame() {
        game = new KlondikeGame(1);
        clear(game);
        from = game.getTableau().get(0);
        to = game.getTableau().get(1);
        foundation = game.getFoundations().get(0);
    }

    private void assertInitialGame(KlondikeGame initial) {
        assertInitialTableau(initial, 1, 2, 3, 4, 5, 6, 7);
        assertEquals(24, initial.getStock().size());
        assertTrue(initial.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        assertTrue(initial.getWaste().isEmpty());
        assertEquals(4, initial.getFoundations().size());
        assertTrue(initial.getFoundations().stream().allMatch(CardPile::isEmpty));
        List<Card> cards = piles(initial).stream().flatMap(p -> p.getCards().stream()).toList();
        assertEquals(52, cards.size());
        assertEquals(52, new HashSet<>(cards).size());
        assertEquals(52, cards.stream().map(c -> c.getSuit() + ":" + c.getRank()).distinct().count());
        assertEquals(0, initial.getMoveCount());
        assertFalse(initial.isWon());
    }

    @Test void constructorDealsCompleteStandardGame() { assertInitialGame(new KlondikeGame(1)); }

    @Test void invalidDrawCountsAreRejectedAndTurnThreeIsSupported() {
        for (int count : new int[]{-1, 0, 2, 4, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> new KlondikeGame(count));
        }
        assertInitialGame(assertDoesNotThrow(() -> new KlondikeGame(3)));
    }

    @Test void alternatingDescendingSegmentMovesInOrderAndRevealsSource() {
        Card hidden = new Card(Suit.SPADES, 5), queen = up(Suit.HEARTS, 12), jack = up(11), king = up(13);
        set(from, hidden, queen, jack);
        set(to, king);
        legalMove(game, from, to, 2);
        assertEquals(List.of(king, queen, jack), to.getCards());
        assertEquals(List.of(hidden), from.getCards());
        assertTrue(hidden.isFaceUp());
    }

    @Test void kingLedSequenceCanMoveIntoEmptyColumn() {
        Card king = up(13), queen = up(Suit.HEARTS, 12);
        set(from, king, queen);
        legalMove(game, from, to, 2);
        assertTrue(from.isEmpty());
        assertEquals(List.of(king, queen), to.getCards());
    }

    @Test void nonKingCannotEnterEmptyColumn() {
        set(from, up(12));
        rejectedMove(game, from, to, 1);
    }

    @Test void sameColorOrWrongRankDestinationIsRejected() {
        set(from, up(11));
        set(to, up(Suit.CLUBS, 12));
        rejectedMove(game, from, to, 1);
        set(to, up(Suit.HEARTS, 13));
        rejectedMove(game, from, to, 1);
    }

    @Test void hiddenDestinationAndAnyHiddenMovedCardAreRejected() {
        set(from, up(Suit.HEARTS, 12), up(11));
        set(to, new Card(Suit.SPADES, 13));
        rejectedMove(game, from, to, 2);
        to.peek().setFaceUp(true);
        from.peek().setFaceUp(false);
        rejectedMove(game, from, to, 2);
        from.peek().setFaceUp(true);
        from.getCards().getFirst().setFaceUp(false);
        rejectedMove(game, from, to, 2);
    }

    @Test void brokenRankAndColorWithinMovedSegmentAreRejected() {
        set(to, up(13));
        set(from, up(Suit.HEARTS, 12), up(10));
        rejectedMove(game, from, to, 2);
        set(from, up(Suit.HEARTS, 12), up(Suit.DIAMONDS, 11));
        rejectedMove(game, from, to, 2);
    }

    @Test void onlySelectedTopSegmentMustBeASequence() {
        Card bottom = up(3), queen = up(Suit.HEARTS, 12);
        set(from, bottom, queen);
        set(to, up(13));
        legalMove(game, from, to, 1);
        assertSame(bottom, from.peek());
    }

    @Test void nullIdenticalForeignEmptyAndInvalidCountRequestsPreserveState() {
        set(from, up(13));
        rejectedMove(game, null, to, 1);
        rejectedMove(game, from, null, 1);
        rejectedMove(game, from, from, 1);
        CardPile foreign = new CardPile();
        Card card = up(13);
        set(foreign, card);
        rejectedMove(game, foreign, to, 1);
        rejectedMove(game, from, foreign, 1);
        assertEquals(List.of(card), foreign.getCards());
        rejectedMove(game, to, from, 1);
        for (int count : new int[]{-1, 0, 2, Integer.MAX_VALUE}) {
            rejectedMove(game, from, to, count);
        }
    }

    @Test void stockCannotParticipateInTransfersAndWasteCannotBeDestination() {
        set(from, up(13));
        set(game.getStock(), up(13));
        rejectedMove(game, game.getStock(), to, 1);
        rejectedMove(game, from, game.getStock(), 1);
        rejectedMove(game, from, game.getWaste(), 1);
    }

    @Test void wasteTopCanMoveToTableauButWasteSequenceCannot() {
        Card queen = up(Suit.HEARTS, 12), jack = up(11);
        set(game.getWaste(), queen, jack);
        set(to, up(Suit.HEARTS, 12));
        rejectedMove(game, game.getWaste(), to, 2);
        legalMove(game, game.getWaste(), to, 1);
        assertSame(queen, game.getWaste().peek());
        assertSame(jack, to.peek());
    }

    @Test void foundationAcceptsAceThenNextRankOfSameSuitFromTableauOrWaste() {
        Card ace = up(Suit.HEARTS, 1), two = up(Suit.HEARTS, 2);
        set(from, ace);
        legalMove(game, from, foundation, 1);
        set(game.getWaste(), two);
        legalMove(game, game.getWaste(), foundation, 1);
        assertEquals(List.of(ace, two), foundation.getCards());
        assertEquals(2, game.getMoveCount());
    }

    @Test void foundationRejectsNonAceStartWrongSuitRankAndHiddenTop() {
        set(from, up(2));
        rejectedMove(game, from, foundation, 1);
        set(foundation, up(Suit.HEARTS, 1));
        rejectedMove(game, from, foundation, 1);
        set(from, up(Suit.HEARTS, 3));
        rejectedMove(game, from, foundation, 1);
        set(from, up(Suit.HEARTS, 2));
        foundation.peek().setFaceUp(false);
        rejectedMove(game, from, foundation, 1);
    }

    @Test void foundationRejectsMultipleCardsAndFoundationToFoundationMoves() {
        set(from, up(Suit.HEARTS, 2), up(1));
        rejectedMove(game, from, foundation, 2);
        set(foundation, up(1));
        rejectedMove(game, foundation, game.getFoundations().get(1), 1);
    }

    @Test void foundationTopMayReturnToTableauButMultipleCardsCannot() {
        Card ace = up(1), two = up(2);
        set(foundation, ace, two);
        set(to, up(Suit.HEARTS, 3));
        rejectedMove(game, foundation, to, 2);
        legalMove(game, foundation, to, 1);
        assertEquals(List.of(ace), foundation.getCards());
        assertSame(two, to.peek());
    }

    @Test void drawMovesExactlyOneTopCardFaceUpAndCountsOnce() {
        Card bottom = new Card(Suit.CLUBS, 3), top = new Card(Suit.SPADES, 4), oldWaste = up(5);
        set(game.getStock(), bottom, top);
        set(game.getWaste(), oldWaste);
        assertTrue(game.drawFromStock());
        assertEquals(List.of(bottom), game.getStock().getCards());
        assertEquals(List.of(oldWaste, top), game.getWaste().getCards());
        assertTrue(top.isFaceUp());
        assertFalse(bottom.isFaceUp());
        assertEquals(1, game.getMoveCount());
    }

    @Test void emptyStockDrawDoesNotRecycleOrChangeState() {
        set(game.getWaste(), up(1));
        rejected(game, game::drawFromStock);
    }

    @Test void recyclingRequiresEmptyStockAndNonemptyWaste() {
        rejected(game, game::recycleStock);
        set(game.getStock(), new Card(Suit.SPADES, 3));
        set(game.getWaste(), up(1));
        rejected(game, game::recycleStock);
    }

    @Test void repeatedRecyclingPreservesRemainingDrawOrderAndCountsEachAction() {
        Card first = new Card(Suit.CLUBS, 4), second = new Card(Suit.HEARTS, 5), third = new Card(Suit.SPADES, 6);
        set(game.getStock(), third, second, first);
        int moves = 0;
        for (int cycle = 0; cycle < 3; cycle++) {
            for (Card expected : List.of(first, second, third)) {
                assertTrue(game.drawFromStock());
                assertSame(expected, game.getWaste().peek());
                assertEquals(++moves, game.getMoveCount());
            }
            assertTrue(game.recycleStock());
            assertEquals(++moves, game.getMoveCount());
            assertTrue(game.getWaste().isEmpty());
            assertEquals(List.of(third, second, first), game.getStock().getCards());
            assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
        }
    }

    @Test void recyclingDoesNotRestoreCardsAlreadyMovedOutOfWaste() {
        Card first = new Card(Suit.SPADES, 1), second = new Card(Suit.HEARTS, 7);
        set(game.getStock(), second, first);
        assertTrue(game.drawFromStock());
        legalMove(game, game.getWaste(), foundation, 1);
        assertTrue(game.drawFromStock());
        assertTrue(game.recycleStock());
        assertEquals(List.of(second), game.getStock().getCards());
        assertSame(first, foundation.peek());
    }

    @Test void winningRequiresAllFourCompleteFoundations() {
        assertFalse(game.isWon());
        for (int i = 0; i < 4; i++) {
            for (int rank = 1; rank <= 13; rank++) {
                set(from, up(Suit.values()[i], rank));
                legalMove(game, from, game.getFoundations().get(i), 1);
                assertEquals(i == 3 && rank == 13, game.isWon());
            }
        }
        assertEquals(52, game.getMoveCount());
    }

    @Test void resetReplacesTableauAndFoundationsAndRestoresInitialState() {
        List<CardPile> tableauView = game.getTableau(), foundationView = game.getFoundations();
        List<CardPile> oldPiles = new ArrayList<>(tableauView);
        CardPile oldFoundation = foundation;
        set(game.getStock(), new Card(Suit.SPADES, 1));
        assertTrue(game.drawFromStock());
        legalMove(game, game.getWaste(), foundation, 1);
        game.startNewGame();
        assertInitialGame(game);
        assertEquals(7, tableauView.size());
        assertEquals(4, foundationView.size());
        assertTrue(tableauView.stream().noneMatch(oldPiles::contains));
        assertFalse(foundationView.contains(oldFoundation));
        rejectedMove(game, oldFoundation, game.getFoundations().get(0), 1);
        assertThrows(UnsupportedOperationException.class, tableauView::clear);
        assertThrows(UnsupportedOperationException.class, foundationView::clear);
    }
}
