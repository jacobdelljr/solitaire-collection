package com.jd.solitaireapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.jd.solitaireapp.GameTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class SpiderMultiSuitTest {
    @Nested class TwoSuit extends ConfigurationTests {
        TwoSuit() { super(2); }
    }

    @Nested class FourSuit extends ConfigurationTests {
        FourSuit() { super(4); }
    }

    // Inherited JUnit tests run independently for each configuration without
    // requiring junit-jupiter-params or changing the Maven dependencies.
    abstract static class ConfigurationTests {
        private final int suitCount;

        ConfigurationTests(int suitCount) { this.suitCount = suitCount; }

        private List<Card> cards(SpiderGame game) {
            return piles(game).stream().flatMap(p -> p.getCards().stream()).toList();
        }

        private List<Suit> suits(List<Card> cards) {
            return cards.stream().map(Card::getSuit).distinct().sorted().toList();
        }

        private void assertComposition(List<Card> cards) {
            assertEquals(104, cards.size());
            assertEquals(104, new HashSet<>(cards).size());
            List<Suit> suits = suits(cards);
            assertEquals(suitCount, suits.size());
            for (Suit suit : suits) {
                assertEquals(104 / suitCount, cards.stream().filter(c -> c.getSuit() == suit).count());
                for (int rank = 1; rank <= 13; rank++) {
                    int expectedRank = rank;
                    assertEquals(8 / suitCount, cards.stream()
                            .filter(c -> c.getSuit() == suit && c.getRank() == expectedRank).count());
                }
            }
        }

        private void assertInitialGame(SpiderGame game) {
            assertInitialTableau(game, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5);
            assertEquals(54, game.getTableau().stream().mapToInt(CardPile::size).sum());
            assertEquals(50, game.getStock().size());
            assertTrue(game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
            assertComposition(cards(game));
            assertEquals(0, game.getMoveCount());
            assertEquals(0, game.getCompletedRuns());
            assertFalse(game.isWon());
        }

        private List<Card> run(Suit suit) {
            List<Card> cards = new ArrayList<>();
            for (int rank = 13; rank >= 1; rank--) { cards.add(up(suit, rank)); }
            return cards;
        }

        private Fixture fixture() { return new Fixture(new SpiderGame(suitCount)); }

        private static class Fixture {
            final SpiderGame game;
            final CardPile from, to;
            final List<Suit> suits;

            Fixture(SpiderGame game) {
                this.game = game;
                // Only suit membership is taken from the deal, never card order.
                suits = piles(game).stream().flatMap(p -> p.getCards().stream())
                        .map(Card::getSuit).distinct().sorted().toList();
                clear(game);
                from = game.getTableau().get(0);
                to = game.getTableau().get(1);
            }

            void fillColumnsAndStock(int stockSize) {
                for (int i = 0; i < 10; i++) {
                    set(game.getTableau().get(i), up(suits.get(i % suits.size()), 7));
                }
                set(game.getStock());
                for (int i = 0; i < stockSize; i++) {
                    game.getStock().addCards(List.of(new Card(suits.get(i % suits.size()), 4)));
                }
            }
        }

        @Test void constructorCreatesGameSuccessfully() {
            assertDoesNotThrow(() -> new SpiderGame(suitCount));
        }

        @Test void deckHas104DistinctFaceDownCardsWithEqualSuitAndRankCounts() {
            Deck deck = Deck.createSpiderDeck(suitCount);
            assertEquals(104, deck.size());
            List<Card> cards = new ArrayList<>();
            while (deck.size() > 0) { cards.add(deck.draw()); }
            assertComposition(cards);
            assertTrue(cards.stream().noneMatch(Card::isFaceUp));
        }

        @Test void initialDealHas54TableauCardsAnd50StockCards() {
            assertInitialGame(new SpiderGame(suitCount));
        }

        @Test void singleCardMovesOntoNextHigherRankForEverySuitPair() {
            Fixture f = fixture();
            for (Suit source : f.suits) {
                for (Suit destination : f.suits) {
                    Card eight = up(source, 8), nine = up(destination, 9);
                    set(f.from, eight);
                    set(f.to, nine);
                    legalMove(f.game, f.from, f.to, 1);
                    assertTrue(f.from.isEmpty());
                    assertEquals(List.of(nine, eight), f.to.getCards());
                }
            }
            assertEquals(suitCount * suitCount, f.game.getMoveCount());
        }

        @Test void sameSuitDescendingSegmentsMoveOntoEveryDestinationSuitAndRevealSource() {
            Fixture f = fixture();
            for (Suit source : f.suits) {
                for (Suit destination : f.suits) {
                    Card hidden = new Card(source, 2);
                    Card eight = up(source, 8), seven = up(source, 7), nine = up(destination, 9);
                    set(f.from, hidden, eight, seven);
                    set(f.to, nine);
                    legalMove(f.game, f.from, f.to, 2);
                    assertEquals(List.of(nine, eight, seven), f.to.getCards());
                    assertEquals(List.of(hidden), f.from.getCards());
                    assertTrue(hidden.isFaceUp());
                }
            }
            assertEquals(0, f.game.getCompletedRuns());
        }

        @Test void sameSuitDescendingSegmentMayEnterEmptyColumn() {
            Fixture f = fixture();
            for (Suit suit : f.suits) {
                Card eight = up(suit, 8), seven = up(suit, 7);
                set(f.from, eight, seven);
                set(f.to);
                legalMove(f.game, f.from, f.to, 2);
                assertTrue(f.from.isEmpty());
                assertEquals(List.of(eight, seven), f.to.getCards());
            }
        }

        @Test void mixedSuitDescendingSegmentsCannotMoveToOccupiedOrEmptyColumn() {
            Fixture f = fixture();
            for (Suit first : f.suits) {
                for (Suit second : f.suits) {
                    if (first == second) { continue; }
                    set(f.from, up(first, 8), up(second, 7));
                    set(f.to, up(first, 9));
                    rejectedMove(f.game, f.from, f.to, 2);
                    set(f.to);
                    rejectedMove(f.game, f.from, f.to, 2);
                }
            }
        }

        @Test void sameSuitTailCanMoveOffMixedSuitColumn() {
            Fixture f = fixture();
            Card nine = up(f.suits.get(0), 9);
            Card eight = up(f.suits.get(1), 8), seven = up(f.suits.get(1), 7);
            set(f.from, nine, eight, seven);
            legalMove(f.game, f.from, f.to, 2);
            assertEquals(List.of(nine), f.from.getCards());
            assertEquals(List.of(eight, seven), f.to.getCards());
        }

        @Test void completeSameSuitRunsAreRemovedForEverySuitWithoutCountingMoves() {
            Fixture f = fixture();
            int removed = 0;
            for (Suit suit : f.suits) {
                Card hidden = new Card(suit, 5);
                set(f.from, hidden);
                f.from.addCards(run(suit));
                assertEquals(1, f.game.removeCompletedRuns());
                assertEquals(++removed, f.game.getCompletedRuns());
                assertEquals(List.of(hidden), f.from.getCards());
                assertTrue(hidden.isFaceUp());
                assertEquals(0, f.game.getMoveCount());
                assertEquals(0, f.game.removeCompletedRuns());
            }
        }

        @Test void completeDescendingRunWithAnyOneDifferentSuitIsNotRemoved() {
            Fixture f = fixture();
            for (int index = 0; index < 13; index++) {
                List<Card> mixed = run(f.suits.get(0));
                mixed.set(index, up(f.suits.get(1), 13 - index));
                set(f.from);
                f.from.addCards(mixed);
                Observation before = observe(f.game);
                assertEquals(0, f.game.removeCompletedRuns());
                assertEquals(before, observe(f.game));
            }
        }

        @Test void transferCompletingSameSuitRunRemovesItAndCountsOnlyOneMove() {
            Fixture f = fixture();
            for (Suit suit : f.suits) {
                set(f.to);
                f.to.addCards(run(suit).subList(0, 12));
                set(f.from, up(suit, 1));
                legalMove(f.game, f.from, f.to, 1);
                assertTrue(f.from.isEmpty());
                assertTrue(f.to.isEmpty());
            }
            assertEquals(suitCount, f.game.getCompletedRuns());
            assertEquals(suitCount, f.game.getMoveCount());
        }

        @Test void transferCompletingMixedSuitRunLeavesAllThirteenCards() {
            Fixture f = fixture();
            f.to.addCards(run(f.suits.get(0)).subList(0, 12));
            Card ace = up(f.suits.get(1), 1);
            set(f.from, ace);
            List<Card> expected = new ArrayList<>(f.to.getCards());
            expected.add(ace);
            legalMove(f.game, f.from, f.to, 1);
            assertEquals(expected, f.to.getCards());
            assertTrue(f.from.isEmpty());
            assertEquals(0, f.game.getCompletedRuns());
        }

        @Test void stockRowsDealInOrderFaceUpAndCountOneMoveEach() {
            Fixture f = fixture();
            f.fillColumnsAndStock(50);
            for (int row = 1; row <= 5; row++) {
                List<Card> stock = List.copyOf(f.game.getStock().getCards());
                List<List<Card>> columns = f.game.getTableau().stream()
                        .map(p -> List.copyOf(p.getCards())).toList();
                assertTrue(f.game.dealNextRow());
                for (int i = 0; i < 10; i++) {
                    List<Card> expected = new ArrayList<>(columns.get(i));
                    expected.add(stock.get(stock.size() - 1 - i));
                    assertEquals(expected, f.game.getTableau().get(i).getCards());
                    assertTrue(f.game.getTableau().get(i).peek().isFaceUp());
                }
                assertEquals(stock.subList(0, stock.size() - 10), f.game.getStock().getCards());
                assertTrue(f.game.getStock().getCards().stream().noneMatch(Card::isFaceUp));
                assertEquals(row, f.game.getMoveCount());
                assertEquals(0, f.game.getCompletedRuns());
            }
            rejected(f.game, f.game::dealNextRow);
        }

        @Test void emptyColumnOrInsufficientStockRejectsRowWithoutCountingMove() {
            Fixture f = fixture();
            for (int index : new int[]{0, 5, 9}) {
                f.fillColumnsAndStock(10);
                set(f.game.getTableau().get(index));
                rejected(f.game, f.game::dealNextRow);
            }
            f.fillColumnsAndStock(9);
            rejected(f.game, f.game::dealNextRow);
        }

        @Test void stockRowCompletingSameSuitRunRemovesItAndCountsOnlyDeal() {
            Fixture f = fixture();
            for (Suit suit : f.suits) {
                f.fillColumnsAndStock(9);
                set(f.from);
                f.from.addCards(run(suit).subList(0, 12));
                f.game.getStock().addCards(List.of(new Card(suit, 1)));
                int moves = f.game.getMoveCount(), runs = f.game.getCompletedRuns();
                assertTrue(f.game.dealNextRow());
                assertTrue(f.from.isEmpty());
                assertTrue(f.game.getStock().isEmpty());
                assertEquals(runs + 1, f.game.getCompletedRuns());
                assertEquals(moves + 1, f.game.getMoveCount());
            }
        }

        @Test void resetPreservesConfiguredSuitsAndClearsMovesRunsAndWin() {
            Fixture f = fixture();
            List<CardPile> view = f.game.getTableau();
            List<CardPile> oldColumns = List.copyOf(view);
            for (int i = 0; i < 8; i++) {
                f.from.addCards(run(f.suits.get(i % suitCount)));
                assertEquals(1, f.game.removeCompletedRuns());
            }
            assertTrue(f.game.isWon());
            set(f.from, up(f.suits.get(0), 8));
            legalMove(f.game, f.from, f.to, 1);
            f.game.startNewGame();
            assertInitialGame(f.game);
            assertEquals(f.suits, suits(cards(f.game)));
            assertEquals(10, view.size());
            assertTrue(view.stream().noneMatch(oldColumns::contains));
            rejectedMove(f.game, f.to, view.getFirst(), 1);
        }
    }
}
