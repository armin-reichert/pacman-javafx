import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.GameVariant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCruiseElroy {

    @BeforeAll
    static void setup() {
        theGameController().register(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        Logger.info("Pac-Man game model registered");
        theGameController().select(GameVariant.PACMAN);
        Logger.info("Pac-Man game selected");
        theGame().buildNormalLevel(1);
        Logger.info("Level 1 built");
    }

    private void eatNextPellet() {
        var arcadeGame = (ArcadePacMan_GameModel) theGame();
        theGameLevel().worldMap().tiles()
            .filter(theGameLevel()::hasFoodAt)
            .filter(not(theGameLevel()::isEnergizerPosition))
            .findFirst().ifPresent(tile -> {
                theGameLevel().registerFoodEatenAt(tile);
                arcadeGame.onPelletEaten();
                //Logger.info("Food remaining: {}, cruise elroy: {}", theGameLevel().uneatenFoodCount(), arcadeGame.cruiseElroy());
            });
    }

    private void eatNextEnergizer() {
        var arcadeGame = (ArcadePacMan_GameModel) theGame();
        theGameLevel(). energizerTiles()
            .filter(theGameLevel()::hasFoodAt)
            .findFirst().ifPresent(tile -> {
                theGameLevel().registerFoodEatenAt(tile);
                arcadeGame.onEnergizerEaten();
                //Logger.info("Food remaining: {}, cruise elroy: {}", theGameLevel().uneatenFoodCount(), arcadeGame.cruiseElroy());
            });
    }

    @Test
    @DisplayName("Test Cruise Elroy Mode 1")
    public void testCruiseElroyMode1() {
        var arcadeGame = (ArcadePacMan_GameModel) theGame();
        while (theGameLevel().uneatenFoodCount() > theGameLevel().data().elroy1DotsLeft()) {
            assertEquals(0, arcadeGame.cruiseElroy());
            eatNextPellet();
        }
        assertEquals(1, arcadeGame.cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > theGameLevel().data().elroy2DotsLeft()) {
            assertEquals(1, arcadeGame.cruiseElroy());
            eatNextPellet();
        }
        assertEquals(2, arcadeGame.cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > 4) {
            assertEquals(2, arcadeGame.cruiseElroy());
            eatNextPellet();
        }
        assertEquals(2, arcadeGame.cruiseElroy());
        while (theGameLevel().uneatenFoodCount() > 0) {
            assertEquals(2, arcadeGame.cruiseElroy());
            eatNextEnergizer();
        }
        assertEquals(2, arcadeGame.cruiseElroy());
    }
}
