/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
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
		GameController.create(GameVariant.PACMAN);
	}

	@Before
	public void setUpTest() {
		game = GameController.it().game();
		game.reset();
		game.setLevel(1);
		game.startLevel();
	}

	@Test
	public void testGameControllerCreated() {
		assertNotNull(GameController.it());
	}

	@Test(expected = IllegalStateException.class)
	public void testGameControllerCreatedTwice() {
		GameController.create(GameVariant.MS_PACMAN);
	}

	@Test
	public void testLevelInitialized() {
		assertTrue(game.level().isPresent());
		var level = game.level().get();
		assertEquals(1, level.number());
		assertEquals(0, level.numGhostsKilledInLevel());
		assertEquals(0, level.numGhostsKilledByEnergizer());
		assertEquals(0, level.cruiseElroyState());
	}

	@Test
	public void testPacCreatedAndInitialized() {
		game.level().ifPresent(level -> {
			var pac = level.pac();
			assertEquals(0, pac.restingTicks());
			assertEquals(0, pac.starvingTicks());
		});
	}

	@Test
	public void testGhostsCreatedAndInitialized() {
		game.level().ifPresent(level -> {
			var redGhost = level.ghost(GameModel.RED_GHOST);
			assertEquals(-1, redGhost.killedIndex());
			assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(redGhost.id()));
			assertNotEquals(Vector2i.ZERO, level.ghostScatterTarget(redGhost.id()));

			var pinkGhost = level.ghost(GameModel.PINK_GHOST);
			assertEquals(-1, pinkGhost.killedIndex());
			assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(pinkGhost.id()));
			assertNotEquals(Vector2i.ZERO, level.ghostScatterTarget(pinkGhost.id()));

			var cyanGhost = level.ghost(GameModel.CYAN_GHOST);
			assertEquals(-1, cyanGhost.killedIndex());
			assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(cyanGhost.id()));
			assertNotEquals(Vector2i.ZERO, level.ghostScatterTarget(cyanGhost.id()));

			var orangeGhost = level.ghost(GameModel.ORANGE_GHOST);
			assertEquals(-1, orangeGhost.killedIndex());
			assertNotEquals(Vector2f.ZERO, level.ghostRevivalPosition(orangeGhost.id()));
			assertNotEquals(Vector2i.ZERO, level.ghostScatterTarget(orangeGhost.id()));
		});
	}

	@Test
	public void testPacResting() {
		game.level().ifPresent(level -> {
			var pac = level.pac();
			pac.rest(3);
			assertEquals(3, pac.restingTicks());
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
			pac.killed();
			assertEquals(0.0, pac.velocity().length(), Vector2f.EPSILON);
		});
	}

	@Test
	public void testPacManGameBonus() {
		for (int levelNumber = 1; levelNumber <= 21; ++levelNumber) {
			game.setLevel(levelNumber);
			game.startLevel();
			game.level().ifPresent(level -> {
				level.handleBonusReached(0);
				assertTrue(level.bonus().isPresent());
				level.bonus().ifPresent(bonus -> {
					assertTrue(bonus instanceof StaticBonus);
					assertEquals(GameModel.BONUS_VALUES_PACMAN[bonus.symbol()] * 100, bonus.points());
				});
			});
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScoreNegativePoints() {
		game.scorePoints(-42);
	}

	@Test
	public void testChangeCredit() {
		assertEquals(0, GameController.it().credit());
		GameController.it().changeCredit(2);
		assertEquals(2, GameController.it().credit());
		GameController.it().changeCredit(-2);
		assertEquals(0, GameController.it().credit());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalKilledIndex() {
		game.level().ifPresent(level -> level.ghost(GameModel.RED_GHOST).setKilledIndex(42));
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