import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.GameVariant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.amr.pacmanfx.Globals.*;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

public class TestEatingFood {

    @BeforeAll
    static void setup() {
        theGameController().registerGame(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        theGameController().select(GameVariant.PACMAN);
    }

    @BeforeEach
    public void prepareLevel() {
        theGame().buildNormalLevel(1);
    }

    private ArcadePacMan_GameModel pacManGame() { return (ArcadePacMan_GameModel) theGame(); }

    private void eatNextPellet() {
        theGameLevel().worldMap().tiles()
            .filter(theGameLevel()::tileContainsFood)
            .filter(not(theGameLevel()::isEnergizerPosition))
            .findFirst().ifPresent(tile -> {
                theGameLevel().registerFoodEatenAt(tile);
                pacManGame().onPelletEaten();
            });
    }

    private void eatNextEnergizer() {
        theGameLevel().energizerTiles()
            .filter(theGameLevel()::tileContainsFood)
            .findFirst().ifPresent(tile -> {
                theGameLevel().registerFoodEatenAt(tile);
                pacManGame().onEnergizerEaten(tile);
            });
    }

    @Test
    @DisplayName("Test Food Counting")
    public void testFoodCounting() {
        int eaten = theGameLevel().eatenFoodCount(), uneaten = theGameLevel().uneatenFoodCount();
        eatNextPellet();
        assertEquals(eaten + 1, theGameLevel().eatenFoodCount());
        assertEquals(uneaten - 1, theGameLevel().uneatenFoodCount());

        eaten = theGameLevel().eatenFoodCount();
        uneaten = theGameLevel().uneatenFoodCount();
        eatNextEnergizer();
        assertEquals(eaten + 1, theGameLevel().eatenFoodCount());
        assertEquals(uneaten - 1, theGameLevel().uneatenFoodCount());
    }

    @Test
    @DisplayName("Test Level Completion")
    public void testLevelCompletion() {
        while (theGameLevel().uneatenFoodCount() > 0) {
            assertFalse(theGame().isLevelCompleted());
            eatNextPellet();
            eatNextEnergizer();
        }
        assertTrue(theGame().isLevelCompleted());
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode")
    public void testCruiseElroyMode() {
        while (theGameLevel().uneatenFoodCount() > theGameLevel().data().elroy1DotsLeft()) {
            assertEquals(0, pacManGame().cruiseElroy());
            eatNextPellet();
        }
        assertEquals(1, pacManGame().cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > theGameLevel().data().elroy2DotsLeft()) {
            assertEquals(1, pacManGame().cruiseElroy());
            eatNextPellet();
        }
        assertEquals(2, pacManGame().cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > theGameLevel().energizerTiles().count()) {
            assertEquals(2, pacManGame().cruiseElroy());
            eatNextPellet();
        }
        assertEquals(2, pacManGame().cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > 0) {
            assertEquals(2, pacManGame().cruiseElroy());
            eatNextEnergizer();
        }
        assertEquals(2, pacManGame().cruiseElroy());
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
