import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.lib.worldmap.FoodLayer;
import de.amr.pacmanfx.model.GameLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

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
        gameLevel.tiles()
            .filter(gameLevel::tileContainsFood)
            .filter(not(gameLevel::isEnergizerPosition))
            .findFirst().ifPresent(tile -> {
                    gameLevel.registerFoodEatenAt(tile);
                pacManGame().onPelletEaten();
            });
    }

    private void eatNextEnergizer(GameLevel gameLevel) {
        FoodLayer foodLayer = gameLevel.worldMap().foodLayer();
        gameLevel.energizerPositions().stream()
            .filter(foodLayer::tileContainsFood)
            .findFirst().ifPresent(tile -> {
                foodLayer.registerFoodEatenAt(tile);
                pacManGame().onEnergizerEaten(tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        GameLevel gameLevel = gameLevel();
        int eaten = gameLevel.eatenFoodCount(), uneaten = gameLevel.uneatenFoodCount();
        eatNextPellet(gameLevel);
        assertEquals(eaten + 1, gameLevel.eatenFoodCount());
        assertEquals(uneaten - 1, gameLevel.uneatenFoodCount());

        eaten = gameLevel.eatenFoodCount();
        uneaten = gameLevel.uneatenFoodCount();
        eatNextEnergizer(gameLevel);
        assertEquals(eaten + 1, gameLevel.eatenFoodCount());
        assertEquals(uneaten - 1, gameLevel.uneatenFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        GameLevel gameLevel = gameLevel();
        while (gameLevel.uneatenFoodCount() > 0) {
            assertFalse(pacManGame().isLevelCompleted());
            eatNextPellet(gameLevel);
            eatNextEnergizer(gameLevel);
        }
        assertTrue(pacManGame().isLevelCompleted());
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        GameLevel gameLevel = gameLevel();
        while (gameLevel.uneatenFoodCount() > gameLevel.data().elroy1DotsLeft()) {
            assertEquals(0, pacManGame().cruiseElroy());
            eatNextPellet(gameLevel);
        }
        assertEquals(1, pacManGame().cruiseElroy());
        while (gameLevel.uneatenFoodCount() > gameLevel.data().elroy2DotsLeft()) {
            assertEquals(1, pacManGame().cruiseElroy());
            eatNextPellet(gameLevel);
        }
        assertEquals(2, pacManGame().cruiseElroy());
        while (gameLevel.uneatenFoodCount() > gameLevel.energizerPositions().size()) {
            assertEquals(2, pacManGame().cruiseElroy());
            eatNextPellet(gameLevel);
        }
        assertEquals(2, pacManGame().cruiseElroy());
        while (gameLevel.uneatenFoodCount() > 0) {
            assertEquals(2, pacManGame().cruiseElroy());
            eatNextEnergizer(gameLevel);
        }
        assertEquals(2, pacManGame().cruiseElroy());
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
