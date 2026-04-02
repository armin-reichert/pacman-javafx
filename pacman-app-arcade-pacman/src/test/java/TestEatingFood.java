/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.actors.ElroyState;
import de.amr.pacmanfx.model.actors.RedGhostShadow;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.ui.GameBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    private static GameBox gameBox;

    @BeforeAll
    static void setup() {
        final String variantName = GameVariant.ARCADE_PACMAN.name();
        final File highScoreFile = new File("");
        gameBox = new GameBox();
        gameBox.registerGame(variantName, new ArcadePacMan_GameModel(gameBox, highScoreFile));
        gameBox.gameVariantNameProperty().set(variantName);
    }

    @BeforeEach
    public void createGameLevel() {
        gameBox.game().buildNormalLevel(1);
    }

    private ArcadePacMan_GameModel theGame() { return gameBox.game(); }

    private GameLevel theGameLevel() {
        return theGame().optGameLevel().orElseThrow();
    }

    private void eatNextPellet() {
        final FoodLayer foodLayer = theGameLevel().worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                theGame().eatPellet(theGameLevel(), tile);
            });
    }

    private void eatNextEnergizer(GameLevel level) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                theGame().eatEnergizer(level, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        final FoodLayer foodLayer = theGameLevel().worldMap().foodLayer();

        int eaten = foodLayer.eatenFoodCount();
        int uneaten = foodLayer.uneatenFoodCount();
        eatNextPellet();
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.uneatenFoodCount());

        eaten = foodLayer.eatenFoodCount();
        uneaten = foodLayer.uneatenFoodCount();
        eatNextEnergizer(theGameLevel());
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.uneatenFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        final GameLevel level = theGameLevel();
        while (level.worldMap().foodLayer().uneatenFoodCount() > 0) {
            assertFalse(theGame().isLevelCompleted(level));
            eatNextPellet();
            eatNextEnergizer(level);
        }
        assertTrue(theGame().isLevelCompleted(level));
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        final RedGhostShadow blinky = (RedGhostShadow) theGameLevel().ghost(RED_GHOST_SHADOW);
        final FoodLayer foodLayer = theGameLevel().worldMap().foodLayer();
        final LevelData data = theGame().levelData(theGameLevel().number());
        while (foodLayer.uneatenFoodCount() > data.numDotsLeftElroy1()) {
            assertEquals(ElroyState.Mode.ZERO, blinky.elroyState().mode());
            eatNextPellet();
        }
        assertEquals(ElroyState.Mode.ONE, blinky.elroyState().mode());
        while (foodLayer.uneatenFoodCount() > data.numDotsLeftElroy2()) {
            assertEquals(ElroyState.Mode.ONE, blinky.elroyState().mode());
            eatNextPellet();
        }
        assertEquals(ElroyState.Mode.TWO, blinky.elroyState().mode());
        while (foodLayer.uneatenFoodCount() > foodLayer.energizerTiles().size()) {
            assertEquals(ElroyState.Mode.TWO, blinky.elroyState().mode());
            eatNextPellet();
        }
        assertEquals(ElroyState.Mode.TWO, blinky.elroyState().mode());
        while (foodLayer.uneatenFoodCount() > 0) {
            assertEquals(ElroyState.Mode.TWO, blinky.elroyState().mode());
            eatNextEnergizer(theGameLevel());
        }
        assertEquals(ElroyState.Mode.TWO, blinky.elroyState().mode());
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        eatNextPellet();
        assertEquals(1, theGameLevel().pac().restingTicks());
        eatNextEnergizer(theGameLevel());
        assertEquals(3, theGameLevel().pac().restingTicks());
    }
}