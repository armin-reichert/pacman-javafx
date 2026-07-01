/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.amr.pacmanfx.model.GameModel.RED_GHOST_SHADOW;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    static class TestContext implements GameContext {

        private final PacManGamesMachine machine = new PacManGamesMachine();
        private final GameFlow  gameFlow  = new Arcade_GameFlow();
        private final GameModel gameModel = new ArcadePacMan_GameModel();
        private final GameRules gameRules = new ArcadePacMan_GameRules();

        @Override
        public CoinMechanism coinMechanism() {
            return machine.coinMechanism();
        }

        @Override
        public GameFlow flow() {
            return gameFlow;
        }

        @Override
        public GameRules rules() {
            return gameRules;
        }

        @Override
        public GameModel model() {
            return gameModel;
        }

        @Override
        public HuntingStepResult huntingStepResult() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHuntingStepResult(HuntingStepResult result) {
            throw new UnsupportedOperationException();
        }
    }

    private static TestContext testContext;

    @BeforeAll
    static void setup() {
        testContext = new TestContext();
    }

    @BeforeEach
    public void createGameLevel() {
        testContext.model().buildNormalLevel(testContext, 1);
    }

    private void eatNextPellet(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                level.gameModel().eatPellet(testContext, level, tile);
            });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                level.gameModel().eatEnergizer(testContext, level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        testContext.model().optGameLevel().ifPresent(level -> {
            final FoodLayer foodLayer = level.worldMap().foodLayer();

            int eaten = foodLayer.eatenFoodCount();
            int uneaten = foodLayer.remainingFoodCount();
            eatNextPellet(level);
            assertEquals(eaten + 1, foodLayer.eatenFoodCount());
            assertEquals(uneaten - 1, foodLayer.remainingFoodCount());

            eaten = foodLayer.eatenFoodCount();
            uneaten = foodLayer.remainingFoodCount();
            eatNextEnergizer(level);
            assertEquals(eaten + 1, foodLayer.eatenFoodCount());
            assertEquals(uneaten - 1, foodLayer.remainingFoodCount());
            
        });
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        testContext.model().optGameLevel().ifPresent(level -> {
            while (level.worldMap().foodLayer().remainingFoodCount() > 0) {
                assertFalse(testContext.rules().isLevelCompleted(level));
                eatNextPellet(level);
                eatNextEnergizer(level);
            }
            assertTrue(testContext.rules().isLevelCompleted(level));
        });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        testContext.model().optGameLevel().ifPresent(level -> {
            final Ghost blinky = level.ghost(RED_GHOST_SHADOW);
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy1()) {
                assertEquals(Elroy.Boost.NONE, blinky.elroy().boost());
                eatNextPellet(level);
            }
            assertEquals(Elroy.Boost.MEDIUM, blinky.elroy().boost());
            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy2()) {
                assertEquals(Elroy.Boost.MEDIUM, blinky.elroy().boost());
                eatNextPellet(level);
            }
            assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
            while (foodLayer.remainingFoodCount() > foodLayer.energizerTiles().size()) {
                assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
                eatNextPellet(level);
            }
            assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
            while (foodLayer.remainingFoodCount() > 0) {
                assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
                eatNextEnergizer(level);
            }
            assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
        });
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        testContext.model().optGameLevel().ifPresent(level -> {
            eatNextPellet(level);
            assertEquals(1, level.entities().pac().restingTicks());
            eatNextEnergizer(level);
            assertEquals(3, level.entities().pac().restingTicks());
        });
    }
}