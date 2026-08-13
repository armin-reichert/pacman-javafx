/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.ecs.systems.DefaultGameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.game.GameBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    static class TestContext implements GameContext {

        private final DefaultGameSystems systems =  new DefaultGameSystems();

        private final GameFlowController gameFlow = new GameFlowController("Test game flow");

        private final ArcadePacMan_GameModel gameModel = new ArcadePacMan_GameModel();

        private final GamePlay gamePlay = new ArcadePacMan_GamePlay();

        private final GameEventManager eventManager = new GameEventManager() {
            @Override
            public void addGameEventSubscriber(GameEventListener listener) {
            }

            @Override
            public void removeGameEventSubscriber(GameEventListener listener) {
            }

            @Override
            public void publishGameEvent(GameEvent event) {
            }
        };

        private final GameSession testSession;

        public TestContext() {
            testSession = new GameSession(
                GameVariantID.ARCADE_MS_PACMAN.name(),
                ArcadePacMan_Cartridge.CARTRIDGE.gameFlowFactory().get(),
                new GameCheats()
            );
        }

        @Override
        public void setSession(GameSession session) {}

        @Override
        public GameSession session() {
            return testSession;
        }

        @Override
        public DefaultGameSystems systems() {
            return systems;
        }

        @Override
        public CoinMechanism coinMechanism() {
            return GameBox.instance().coinMechanism();
        }

        @Override
        public GamePlay gamePlay() {
            return gamePlay;
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

    private static TestContext test;

    @BeforeAll
    static void setup() {
        test = new TestContext();
    }

    @BeforeEach
    public void createGameLevel() {
        test.gamePlay().buildNormalLevel(test, 1, 3);
    }

    private void eatNextPellet(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(pelletTile -> {
                foodLayer.markFoodEatenAt(pelletTile);
                test.gamePlay().onEatPellet(test, level, pelletTile);
            });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                test.gamePlay().onEatEnergizer(test, level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        test.session().optLevel().ifPresent(level -> {
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
        test.session().optLevel().ifPresent(level -> {
            while (level.worldMap().foodLayer().remainingFoodCount() > 0) {
                assertFalse(test.model().rules().isLevelCompleted(level));
                eatNextPellet(level);
                eatNextEnergizer(level);
            }
            assertTrue(test.model().rules().isLevelCompleted(level));
        });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        test.session().optLevel().ifPresent(level -> {
            final Ghost blinky = level.ghost(GhostPersonality.RED_GHOST_SHADOW);
            final ElroyComp elroy = blinky.requireComp(ElroyComp.class);
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());

            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy1()) {
                assertEquals(ElroyComp.Boost.NONE, elroy.boost());
                eatNextPellet(level);
            }
            assertEquals(ElroyComp.Boost.MEDIUM, elroy.boost());
            while (foodLayer.remainingFoodCount() > data.numDotsLeftElroy2()) {
                assertEquals(ElroyComp.Boost.MEDIUM, elroy.boost());
                eatNextPellet(level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
            while (foodLayer.remainingFoodCount() > foodLayer.energizerTiles().size()) {
                assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
                eatNextPellet(level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
            while (foodLayer.remainingFoodCount() > 0) {
                assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
                eatNextEnergizer(level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
        });
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        test.session().optLevel().ifPresent(level -> {
            final Pac pac = level.entities().pac();
            eatNextPellet(level);
            assertEquals(1, pac.digestion().restingTicks());
            eatNextEnergizer(level);
            assertEquals(3, pac.digestion().restingTicks());
        });
    }
}