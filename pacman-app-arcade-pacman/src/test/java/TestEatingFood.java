import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.actors.Blinky;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.GameLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.theGameContext;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    @BeforeAll
    static void setup() {
        theGameContext().gameController().registerGame("PACMAN", new ArcadePacMan_GameModel(theGameContext(), new File("")));
        theGameContext().gameController().selectGameVariant("PACMAN");
    }

    @BeforeEach
    public void createGameLevel() {
        theGameContext().game().buildNormalLevel(1);
    }

    private GameLevel gameLevel() { return theGameContext().gameLevel(); }

    private ArcadePacMan_GameModel pacManGame() { return theGameContext().game(); }

    private void eatNextPellet(GameLevel gameLevel) {
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        foodLayer.tiles()
            .filter(foodLayer::hasFoodAtTile)
            .filter(not(foodLayer::isEnergizerTile))
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                pacManGame().onPelletEaten(gameLevel);
            });
    }

    private void eatNextEnergizer(GameLevel gameLevel) {
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        foodLayer.energizerTiles().stream()
            .filter(foodLayer::hasFoodAtTile)
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                pacManGame().onEnergizerEaten(gameLevel, tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        GameLevel gameLevel = gameLevel();
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        int eaten = foodLayer.eatenFoodCount(), uneaten = foodLayer.uneatenFoodCount();
        eatNextPellet(gameLevel);
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.uneatenFoodCount());

        eaten = foodLayer.eatenFoodCount();
        uneaten = foodLayer.uneatenFoodCount();
        eatNextEnergizer(gameLevel);
        assertEquals(eaten + 1, foodLayer.eatenFoodCount());
        assertEquals(uneaten - 1, foodLayer.uneatenFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        GameLevel gameLevel = gameLevel();
        while (gameLevel.worldMap().foodLayer().uneatenFoodCount() > 0) {
            assertFalse(pacManGame().isLevelCompleted(gameLevel));
            eatNextPellet(gameLevel);
            eatNextEnergizer(gameLevel);
        }
        assertTrue(pacManGame().isLevelCompleted(gameLevel));
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        final ArcadePacMan_GameModel game = pacManGame();
        final GameLevel gameLevel = gameLevel();
        final Blinky blinky = (Blinky) gameLevel.ghost(RED_GHOST_SHADOW);
        final FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        while (foodLayer.uneatenFoodCount() > game.levelData(gameLevel).elroy1DotsLeft()) {
            assertEquals(0, blinky.cruiseElroyState());
            eatNextPellet(gameLevel);
        }
        assertEquals(1, blinky.cruiseElroyState());
        while (foodLayer.uneatenFoodCount() > game.levelData(gameLevel).elroy2DotsLeft()) {
            assertEquals(1, blinky.cruiseElroyState());
            eatNextPellet(gameLevel);
        }
        assertEquals(2, blinky.cruiseElroyState());
        while (foodLayer.uneatenFoodCount() > foodLayer.energizerTiles().size()) {
            assertEquals(2, blinky.cruiseElroyState());
            eatNextPellet(gameLevel);
        }
        assertEquals(2, blinky.cruiseElroyState());
        while (foodLayer.uneatenFoodCount() > 0) {
            assertEquals(2, blinky.cruiseElroyState());
            eatNextEnergizer(gameLevel);
        }
        assertEquals(2, blinky.cruiseElroyState());
    }

    @Test
    @DisplayName("Test Resting")
    public void testResting() {
        GameLevel gameLevel = gameLevel();
        eatNextPellet(gameLevel);
        assertEquals(1, gameLevel.pac().restingTicks());
        eatNextEnergizer(gameLevel);
        assertEquals(3, gameLevel.pac().restingTicks());
    }
}
