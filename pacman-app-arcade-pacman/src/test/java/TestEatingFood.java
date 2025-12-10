import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.arcade.pacman.model.actors.Blinky;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.StandardGameVariant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    @BeforeAll
    static void setup() {
        final String variantName = StandardGameVariant.PACMAN.name();
        final File highScoreFile = new File("");
        THE_GAME_BOX.registerGame(variantName, new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile));
        THE_GAME_BOX.gameVariantNameProperty().set(variantName);
    }

    @BeforeEach
    public void createGameLevel() {
        THE_GAME_BOX.currentGame().buildNormalLevel(1);
    }

    private ArcadePacMan_GameModel theGame() { return THE_GAME_BOX.currentGame(); }

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
                theGame().onPelletEaten(theGameLevel());
            });
    }

    private void eatNextEnergizer() {
        final FoodLayer foodLayer = theGameLevel().worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                theGame().onEnergizerEaten(theGameLevel(), tile);
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
        eatNextEnergizer();
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.uneatenFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        while (theGameLevel().worldMap().foodLayer().uneatenFoodCount() > 0) {
            assertFalse(theGame().isLevelCompleted());
            eatNextPellet();
            eatNextEnergizer();
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
            assertEquals(0, blinky.cruiseElroyValue());
            eatNextPellet();
        }
        assertEquals(1, blinky.cruiseElroyValue());
        while (foodLayer.uneatenFoodCount() > data.numDotsLeftElroy2()) {
            assertEquals(1, blinky.cruiseElroyValue());
            eatNextPellet();
        }
        assertEquals(2, blinky.cruiseElroyValue());
        while (foodLayer.uneatenFoodCount() > foodLayer.energizerTiles().size()) {
            assertEquals(2, blinky.cruiseElroyValue());
            eatNextPellet();
        }
        assertEquals(2, blinky.cruiseElroyValue());
        while (foodLayer.uneatenFoodCount() > 0) {
            assertEquals(2, blinky.cruiseElroyValue());
            eatNextEnergizer();
        }
        assertEquals(2, blinky.cruiseElroyValue());
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        eatNextPellet();
        assertEquals(1, theGameLevel().pac().restingTicks());
        eatNextEnergizer();
        assertEquals(3, theGameLevel().pac().restingTicks());
    }
}