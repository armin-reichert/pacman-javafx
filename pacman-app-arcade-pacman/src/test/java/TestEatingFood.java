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
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.amr.pacmanfx.core.Globals.RED_GHOST_SHADOW;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    private static GameContext testContext;

    @BeforeAll
    static void setup() {

        final GameRules gameRules = new ArcadePacMan_GameRules();

        final GameModel gameModel = new ArcadePacMan_GameModel(
            new Arcade_GameFlow(),
            new CoinMechanism(99)
        );

        testContext = new GameContext() {
            @Override
            public GameModel gameModel() {
                return gameModel;
            }

            @Override
            public GameRules gameRules() {
                return gameRules;
            }

            @Override
            public GameFlow gameFlow() {
                return gameModel.flow(); //TODO
            }

            @Override
            public void setCollisionStrategy(CollisionStrategy collisionStrategy) {
            }

            @Override
            public Boolean isCollisionDoubleChecked() {
                return true;
            }

            @Override
            public CollisionStrategy collisionStrategy() {
                return CollisionStrategy.SAME_TILE;
            }

            @Override
            public void setCollisionDoubleChecked(boolean doubleChecked) {
            }

            @Override
            public void startNewHuntingStep() {
            }

            @Override
            public HuntingStepResult huntingResult() {
                return null;
            }
        };
    }

    @BeforeEach
    public void createGameLevel() {
        testContext.gameModel().buildNormalLevel(1);
    }

    private void eatNextPellet() {
        testContext.gameModel().optGameLevel().ifPresent(level -> {
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            foodLayer.tiles()
                .filter(foodLayer::hasFoodAtTile)
                .filter(not(foodLayer::isEnergizerTile))
                .findFirst().ifPresent(tile -> {
                    foodLayer.markFoodEatenAt(tile);
                    level.game().eatPellet(level, tile);
                });
        });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                level.game().eatEnergizer(level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        testContext.gameModel().optGameLevel().ifPresent(level -> {
            final FoodLayer foodLayer = level.worldMap().foodLayer();

            int eaten = foodLayer.eatenFoodCount();
            int uneaten = foodLayer.remainingFoodCount();
            eatNextPellet();
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
        testContext.gameModel().optGameLevel().ifPresent(level -> {
            while (level.worldMap().foodLayer().remainingFoodCount() > 0) {
                assertFalse(level.game().rules().isLevelCompleted(level));
                eatNextPellet();
                eatNextEnergizer(level);
            }
            assertTrue(level.game().rules().isLevelCompleted(level));
        });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        testContext.gameModel().optGameLevel().ifPresent(level -> {
            final Ghost blinky = level.ghost(RED_GHOST_SHADOW);
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());
            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy1()) {
                assertEquals(Elroy.Boost.NONE, blinky.elroy().boost());
                eatNextPellet();
            }
            assertEquals(Elroy.Boost.MEDIUM, blinky.elroy().boost());
            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy2()) {
                assertEquals(Elroy.Boost.MEDIUM, blinky.elroy().boost());
                eatNextPellet();
            }
            assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
            while (foodLayer.remainingFoodCount() > foodLayer.energizerTiles().size()) {
                assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
                eatNextPellet();
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
        testContext.gameModel().optGameLevel().ifPresent(level -> {
            eatNextPellet();
            assertEquals(1, level.entities().pac().restingTicks());
            eatNextEnergizer(level);
            assertEquals(3, level.entities().pac().restingTicks());
        });
    }
}