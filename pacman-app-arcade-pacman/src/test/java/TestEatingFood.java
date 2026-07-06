/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameCheats;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.simulation.GamePlay;
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
        private final ArcadePacMan_GameModel gameModel = new ArcadePacMan_GameModel();
        private final GamePlay gamePlay = new ArcadePacMan_GamePlay();
        private final GameEventManager eventManager = new GameEventManager() {
            @Override
            public void addGameEventListener(GameEventListener listener) {
            }

            @Override
            public void removeGameEventListener(GameEventListener listener) {
            }

            @Override
            public void publishGameEvent(GameEvent event) {
            }
        };

        public TestContext() {
            gameModel.setRules(new ArcadePacMan_GameRules());
        }

        @Override
        public CoinMechanism coinMechanism() {
            return machine.coinMechanism();
        }

        @Override
        public GamePlay gamePlay() {
            return gamePlay;
        }

        @Override
        public GameFlow flow() {
            return gameFlow;
        }

        @Override
        public GameCheats cheats() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GameModel model() {
            return gameModel;
        }

        @Override
        public GameEventManager eventManager() {
            return eventManager;
        }
    }

    private static TestContext context;

    @BeforeAll
    static void setup() {
        context = new TestContext();
    }

    @BeforeEach
    public void createGameLevel() {
        context.gamePlay().buildNormalLevel(context.eventManager(), context.model(), 1);
    }

    private void eatNextPellet(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                context.gamePlay().onEatPellet(context.eventManager(), level, tile);
            });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                context.gamePlay().onEatEnergizer(context.eventManager(), level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        context.model().optGameLevel().ifPresent(level -> {
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
        context.model().optGameLevel().ifPresent(level -> {
            while (level.worldMap().foodLayer().remainingFoodCount() > 0) {
                assertFalse(context.model().rules().isLevelCompleted(level));
                eatNextPellet(level);
                eatNextEnergizer(level);
            }
            assertTrue(context.model().rules().isLevelCompleted(level));
        });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        context.model().optGameLevel().ifPresent(level -> {
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
        context.model().optGameLevel().ifPresent(level -> {
            eatNextPellet(level);
            assertEquals(1, level.entities().pac().restingTicks());
            eatNextEnergizer(level);
            assertEquals(3, level.entities().pac().restingTicks());
        });
    }
}