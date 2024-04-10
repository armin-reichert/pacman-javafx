/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.actors.StaticBonus;
import de.amr.games.pacman.model.world.ArcadeWorld;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static de.amr.games.pacman.model.GameModel.*;
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
    public void testFirstLevelInitializedWhenGameStarts() {
        assertTrue(game.level().isPresent());

        var level = game.level().get();
        assertEquals(1, level.levelNumber());
        assertEquals(0, level.totalNumGhostsKilled());
        assertEquals(0, level.pac().victims().size());
        assertEquals(0, level.cruiseElroyState());

        assertNotNull(level.world());
        var world = level.world();
        assertEquals(ArcadeWorld.TILES_X, world.numCols());
        assertEquals(ArcadeWorld.TILES_Y, world.numRows());

        Direction[] directions = { Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP };
        for (var id: List.of(RED_GHOST, PINK_GHOST, CYAN_GHOST, ORANGE_GHOST)) {
            var ghost = level.ghost(id);
            assertEquals(0f, ghost.velocity().length(), 0);
            assertEquals(level.initialGhostPosition(ghost.id()), ghost.position());
            assertEquals(directions[ghost.id()], ghost.moveDir());
            assertEquals(directions[ghost.id()], ghost.wishDir());
            assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(ghost.id()));
            assertNotEquals(Vector2i.ZERO, level.world().ghostScatterTarget(ghost.id()));
        }
    }

    @Test
    public void testPacCreatedAndInitialized() {
        game.level().ifPresent(level -> {
            var pac = level.pac();
            assertEquals(0, pac.starvingTicks());
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