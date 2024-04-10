/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.actors.StaticBonus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Armin Reichert
 */
public class PacManGameTest {

    private GameModel game;

    @BeforeClass
    public static void setUp() {
        GameController.it().selectGame(GameVariants.PACMAN);
    }

    @Before
    public void setUpTest() {
        game = GameController.it().game();
        game.reset();
        game.createAndStartLevel(1);
    }

    @Test
    public void testGameControllerCreated() {
        assertNotNull(GameController.it());
    }

    @Test
    public void testLevelInitialized() {
        assertTrue(game.level().isPresent());
        var level = game.level().get();
        assertEquals(1, level.levelNumber());
        assertEquals(0, level.totalNumGhostsKilled());
        assertEquals(0, level.pac().victims().size());
        assertEquals(0, level.cruiseElroyState());
    }

    @Test
    public void testPacCreatedAndInitialized() {
        game.level().ifPresent(level -> {
            var pac = level.pac();
            assertEquals(0, pac.starvingTicks());
        });
    }

    @Test
    public void testGhostsCreatedAndInitialized() {
        game.level().ifPresent(level -> {
            var redGhost = level.ghost(GameModel.RED_GHOST);
            assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(redGhost.id()));
            assertNotEquals(Vector2i.ZERO, level.world().ghostScatterTarget(redGhost.id()));

            var pinkGhost = level.ghost(GameModel.PINK_GHOST);
            assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(pinkGhost.id()));
            assertNotEquals(Vector2i.ZERO, level.world().ghostScatterTarget(pinkGhost.id()));

            var cyanGhost = level.ghost(GameModel.CYAN_GHOST);
            assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(cyanGhost.id()));
            assertNotEquals(Vector2i.ZERO, level.world().ghostScatterTarget(cyanGhost.id()));

            var orangeGhost = level.ghost(GameModel.ORANGE_GHOST);
            assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(orangeGhost.id()));
            assertNotEquals(Vector2i.ZERO, level.world().ghostScatterTarget(orangeGhost.id()));
        });
    }

    @Test
    public void testPacStarving() {
        game.level().ifPresent(level -> {
            var pac = level.pac();
            pac.starve();
            assertEquals(1, pac.starvingTicks());
            pac.starve();
            assertEquals(2, pac.starvingTicks());
        });
    }

    @Test
    public void testDeadPacHasZeroSpeed() {
        game.level().ifPresent(level -> {
            var pac = level.pac();
            pac.setSpeed(42);
            assertEquals(42.0, pac.velocity().length(), Vector2f.EPSILON);
            pac.die();
            assertEquals(0.0, pac.velocity().length(), Vector2f.EPSILON);
        });
    }

    @Test
    public void testPacManGameBonus() {
        for (int levelNumber = 1; levelNumber <= 21; ++levelNumber) {
            game.createAndStartLevel(levelNumber);
            game.level().ifPresent(level -> {
                level.onBonusReached(0);
                assertTrue(level.bonus().isPresent());
                level.bonus().ifPresent(bonus -> {
                    assertTrue(bonus instanceof StaticBonus);
                    assertEquals(game.bonusValue(bonus.symbol()), bonus.points());
                });
            });
        }
    }

    @Test
    public void testChangeCredit() {
        assertEquals(0, GameController.it().credit());
        GameController.it().changeCredit(2);
        assertEquals(2, GameController.it().credit());
        GameController.it().changeCredit(-2);
        assertEquals(0, GameController.it().credit());
    }

    @Test
    public void testLegalCruiseElroyState() {
        game.level().ifPresent(level -> {
            level.setCruiseElroyState(-2);
            assertEquals(-2, level.cruiseElroyState());
            level.setCruiseElroyState(-1);
            assertEquals(-1, level.cruiseElroyState());
            level.setCruiseElroyState(0);
            assertEquals(0, level.cruiseElroyState());
            level.setCruiseElroyState(1);
            assertEquals(1, level.cruiseElroyState());
            level.setCruiseElroyState(2);
            assertEquals(2, level.cruiseElroyState());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalCruiseElroyState() {
        game.level().ifPresent(level -> level.setCruiseElroyState(42));
    }
}