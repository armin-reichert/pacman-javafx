/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.model.actors.Blinky;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.world.FoodLayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    @BeforeAll
    static void setup() {
        final String variantName = GameVariant.ARCADE_PACMAN.name();
        final File highScoreFile = new File("");
        GameBox.instance().registerGame(variantName, new ArcadePacMan_GameModel(GameBox.instance().coinMechanism(), highScoreFile));
        GameBox.instance().gameVariantNameProperty().set(variantName);
    }

    @BeforeEach
    public void createGameLevel() {
        GameBox.instance().currentGame().buildNormalLevel(1);
    }

    private ArcadePacMan_GameModel theGame() { return GameBox.instance().currentGame(); }

    private GameLevel theGameLevel() {
        return theGame().level();
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
        while (theGameLevel().worldMap().foodLayer().uneatenFoodCount() > 0) {
            assertFalse(theGame().isLevelCompleted());
            eatNextPellet();
            eatNextEnergizer(theGameLevel());
        }
        assertTrue(theGame().isLevelCompleted());
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        final Blinky blinky = (Blinky) theGameLevel().ghost(RED_GHOST_SHADOW);
        final FoodLayer foodLayer = theGameLevel().worldMap().foodLayer();
        final LevelData data = theGame().levelData(theGameLevel().number());
        while (foodLayer.uneatenFoodCount() > data.numDotsLeftElroy1()) {
            assertEquals(Blinky.ElroyMode.NONE, blinky.elroyMode());
            eatNextPellet();
        }
        assertEquals(Blinky.ElroyMode._1, blinky.elroyMode());
        while (foodLayer.uneatenFoodCount() > data.numDotsLeftElroy2()) {
            assertEquals(Blinky.ElroyMode._1, blinky.elroyMode());
            eatNextPellet();
        }
        assertEquals(Blinky.ElroyMode._2, blinky.elroyMode());
        while (foodLayer.uneatenFoodCount() > foodLayer.energizerTiles().size()) {
            assertEquals(Blinky.ElroyMode._2, blinky.elroyMode());
            eatNextPellet();
        }
        assertEquals(Blinky.ElroyMode._2, blinky.elroyMode());
        while (foodLayer.uneatenFoodCount() > 0) {
            assertEquals(Blinky.ElroyMode._2, blinky.elroyMode());
            eatNextEnergizer(theGameLevel());
        }
        assertEquals(Blinky.ElroyMode._2, blinky.elroyMode());
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