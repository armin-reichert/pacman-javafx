/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_WorldMapManager;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.rules.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.DefaultGameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.FoodLayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    private static class TestGameFlowController extends GameFlowController {
        public TestGameFlowController() {
            super("Arcade Pac-Man Testcase Game Flow");
            for (Arcade_GameState gameState : Arcade_GameState.values()) {
                addState(gameState.state());
            }
        }
    }

    private static final GameEventManager NULL_EVENT_MANAGER = new GameEventManager() {
        @Override
        public void addGameEventSubscriber(GameEventListener listener) {}

        @Override
        public void removeGameEventSubscriber(GameEventListener listener) {}

        @Override
        public void clear() {}

        @Override
        public void publishGameEvent(GameEvent event) {}
    };

    private static final GameContext GAME = new GameContext(
        new CoinMechanism(99),
        new GameVariantConfig(
            new DefaultGameSystems(),
            new ArcadePacMan_GamePlay(),
            new TestGameFlowController(),
            new ArcadePacMan_GameRules(),
            new ArcadePacMan_WorldMapManager()
        ),
        NULL_EVENT_MANAGER
    );

    @BeforeAll
    static void beforeAll() {
        GAME.setSession(new GameSession(GameVariantID.ARCADE_PACMAN.name(), new GameCheats()));
    }

    @BeforeEach
    public void createGameLevel() {
        GAME.variant().gamePlay().buildNormalLevel(GAME, 1, 3);
    }

    private void eatNextPellet(GamePlay gamePlay, GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(level.food()::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst()
            .ifPresent(tile -> {
                level.food().markFoodEatenAt(tile);
                gamePlay.onEatPellet(GAME, level, tile);
            });
    }

    private void eatNextEnergizer(GamePlay gamePlay, GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(level.food()::hasFoodAtTile)
            .findFirst()
            .ifPresent(tile -> {
                level.food().markFoodEatenAt(tile);
                gamePlay.onEatEnergizer(GAME, level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        final GamePlay gamePlay = GAME.variant().gamePlay();
        GAME.session().optLevel().ifPresent(level -> {
            int eaten = level.food().eatenFoodCount();
            int uneaten = level.food().remainingFoodCount();
            eatNextPellet(gamePlay, level);
            assertEquals(eaten + 1, level.food().eatenFoodCount());
            assertEquals(uneaten - 1, level.food().remainingFoodCount());

            eaten = level.food().eatenFoodCount();
            uneaten = level.food().remainingFoodCount();
            eatNextEnergizer(gamePlay, level);
            assertEquals(eaten + 1, level.food().eatenFoodCount());
            assertEquals(uneaten - 1, level.food().remainingFoodCount());
        });
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        final GamePlay gamePlay = GAME.variant().gamePlay();
        GAME.session().optLevel().ifPresent(level -> {
            while (level.food().remainingFoodCount() > 0) {
                assertFalse(GAME.variant().rules().isLevelCompleted(level));
                eatNextPellet(gamePlay, level);
                eatNextEnergizer(gamePlay, level);
            }
            assertTrue(GAME.variant().rules().isLevelCompleted(level));
        });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        final GamePlay gamePlay = GAME.variant().gamePlay();
        GAME.session().optLevel().ifPresent(level -> {
            final Ghost blinky = level.entities().ghost(GhostPersonality.RED_GHOST_SHADOW);
            final ElroyComp elroy = blinky.reqComp(ElroyComp.class);
            final FoodLayer foodLayer = level.worldMap().foodLayer();
            final LevelData data = ArcadePacMan_GameRules.levelData(level.number());

            while (level.food().remainingFoodCount() > data.numDotsLeftElroy1()) {
                assertEquals(ElroyComp.Boost.NONE, elroy.boost());
                eatNextPellet(gamePlay, level);
            }
            assertEquals(ElroyComp.Boost.MEDIUM, elroy.boost());
            while (level.food().remainingFoodCount() > data.numDotsLeftElroy2()) {
                assertEquals(ElroyComp.Boost.MEDIUM, elroy.boost());
                eatNextPellet(gamePlay, level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
            while (level.food().remainingFoodCount() > foodLayer.energizerTiles().size()) {
                assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
                eatNextPellet(gamePlay, level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
            while (level.food().remainingFoodCount() > 0) {
                assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
                eatNextEnergizer(gamePlay, level);
            }
            assertEquals(ElroyComp.Boost.LARGE, elroy.boost());
        });
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        final GamePlay gamePlay = GAME.variant().gamePlay();
        GAME.session().optLevel().ifPresent(level -> {
            final Pac pac = level.entities().pac();
            eatNextPellet(gamePlay, level);
            assertEquals(1, pac.digestion().restingTicks());
            eatNextEnergizer(gamePlay, level);
            assertEquals(3, pac.digestion().restingTicks());
        });
    }
}