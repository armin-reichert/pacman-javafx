/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.ui.app.GameBox;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.model.actors.Elroy;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.uilib.GameClockFX;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.amr.pacmanfx.core.Globals.RED_GHOST_SHADOW;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    private static GameBox gameBox;

    @BeforeAll
    static void setup() {
        final String variantName = GameVariant.ARCADE_PACMAN.name();
        gameBox = new GameBox(new GameClockFX(), CoinMechanism.OUT_OF_SERVICE);
        gameBox.registerGame(variantName, new ArcadePacMan_GameModel(
            new Arcade_GameFlow(gameBox),
            CoinMechanism.OUT_OF_SERVICE));
        gameBox.select(variantName);
    }

    @BeforeEach
    public void createGameLevel() {
        gameBox.gameModel().buildNormalLevel(1);
    }

    private GameLevel currentGameLevel() {
        return gameBox.gameModel().optGameLevel().orElseThrow();
    }

    private void eatNextPellet() {
        final FoodLayer foodLayer = currentGameLevel().worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                gameBox.gameModel().eatPellet(currentGameLevel(), tile);
            });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.markFoodEatenAt(tile);
                gameBox.gameModel().eatEnergizer(level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        final FoodLayer foodLayer = currentGameLevel().worldMap().foodLayer();

        int eaten = foodLayer.eatenFoodCount();
        int uneaten = foodLayer.remainingFoodCount();
        eatNextPellet();
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.remainingFoodCount());

        eaten = foodLayer.eatenFoodCount();
        uneaten = foodLayer.remainingFoodCount();
        eatNextEnergizer(currentGameLevel());
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.remainingFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        final GameLevel level = currentGameLevel();
        while (level.worldMap().foodLayer().remainingFoodCount() > 0) {
            assertFalse(gameBox.gameModel().rules().isLevelCompleted(level));
            eatNextPellet();
            eatNextEnergizer(level);
        }
        assertTrue(gameBox.gameModel().rules().isLevelCompleted(level));
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        final Ghost blinky = currentGameLevel().ghost(RED_GHOST_SHADOW);
        final FoodLayer foodLayer = currentGameLevel().worldMap().foodLayer();
        final LevelData data = ArcadePacMan_GameRules.levelData(currentGameLevel().number());
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
            eatNextEnergizer(currentGameLevel());
        }
        assertEquals(Elroy.Boost.LARGE, blinky.elroy().boost());
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        eatNextPellet();
        assertEquals(1, currentGameLevel().entities().pac().restingTicks());
        eatNextEnergizer(currentGameLevel());
        assertEquals(3, currentGameLevel().entities().pac().restingTicks());
    }
}