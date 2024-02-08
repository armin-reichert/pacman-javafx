/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.junit.Test;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.function.Predicate.not;
import static org.junit.Assert.*;

/**
 * @author Armin Reichert
 */
public class WorldTest {

	@Test
	public void testNullTileArg() {
		var world = ArcadeWorld.createPacManWorld();
		assertThrows(NullPointerException.class, () -> world.insideBounds(null));
		assertThrows(NullPointerException.class, () -> world.belongsToPortal(null));
		assertThrows(NullPointerException.class, () -> world.isIntersection(null));
		assertThrows(NullPointerException.class, () -> world.isWall(null));
		assertThrows(NullPointerException.class, () -> world.isTunnel(null));
		assertThrows(NullPointerException.class, () -> world.isFoodTile(null));
		assertThrows(NullPointerException.class, () -> world.isEnergizerTile(null));
		assertThrows(NullPointerException.class, () -> world.removeFood(null));
		assertThrows(NullPointerException.class, () -> world.hasFoodAt(null));
		assertThrows(NullPointerException.class, () -> world.hasEatenFoodAt(null));
	}

	@Test
	public void testTileCoordinates() {
		Vector2f pos = v2f(0, 0);
		assertEquals(Vector2i.ZERO, tileAt(pos));
		pos = v2f(7.9f, 7.9f);
		assertEquals(v2i(0, 0), tileAt(pos));
		pos = v2f(8.0f, 7.9f);
		assertEquals(v2i(1, 0), tileAt(pos));
		pos = v2f(8.0f, 0.0f);
		assertEquals(v2i(1, 0), tileAt(pos));
		pos = v2f(0.0f, 8.0f);
		assertEquals(v2i(0, 1), tileAt(pos));

		var guy = new Ghost(GameModel.RED_GHOST, "Guy");

		guy.setPosition(3.99f, 0);
		assertEquals(v2i(0, 0), guy.tile());
		assertEquals(v2f(3.99f, 0.0f), guy.offset());

		guy.setPosition(4.0f, 0);
		assertEquals(v2i(1, 0), guy.tile());
		assertEquals(v2f(-4.0f, 0.0f), guy.offset());
	}

	@Test
	public void testPacManWorld() {
		var world = ArcadeWorld.createPacManWorld();
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(244, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld1() {
		var world = ArcadeWorld.createMsPacManWorld(1);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(220 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(220, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld2() {
		var world = ArcadeWorld.createMsPacManWorld(2);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(240 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(240, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testMsPacManWorld3() {
		var world = ArcadeWorld.createMsPacManWorld(3);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(238 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(238, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(1, world.portals().size());
	}

	@Test
	public void testMsPacManWorld4() {
		var world = ArcadeWorld.createMsPacManWorld(4);
		assertEquals(ArcadeWorld.TILES_Y, world.numRows());
		assertEquals(ArcadeWorld.TILES_X, world.numCols());
		assertEquals(4, world.energizerTiles().count());
		assertEquals(234 + 4, world.tiles().filter(world::isFoodTile).count());
		assertEquals(234, world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile)).count());
		assertEquals(2, world.portals().size());
	}

	@Test
	public void testCopyMapData() {
		byte[][] map = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
		byte[][] copy = copyByteArray2D(map);
		assertEquals(map.length, copy.length);
		for (int i = 0; i < map.length; ++i) {
			assertEquals(map[i].length, copy[i].length);
		}
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				assertEquals(map[i][j], copy[i][j]);
			}
		}
		assertEquals(4, map[1][1]);
		assertEquals(4, copy[1][1]);
		copy[1][1] = (byte) 42;
		assertNotEquals(map[1][1], copy[1][1]);
	}

	@Test
	public void testIllegalMapData() {
		byte[][] map = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
		assertThrows(IllegalArgumentException.class, () -> new World(map));
	}

	@Test
	public void testIllegalArcadeMapSize() {
		byte[][] map = { { 0, 1, 2 }, { 1, 1, 1 }, { 2, 2, 2 } };
		assertThrows(IllegalArgumentException.class, () -> ArcadeWorld.createArcadeWorld(map));
	}
}