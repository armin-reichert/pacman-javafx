/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.Door;
import org.junit.Test;

import static de.amr.games.pacman.lib.Globals.v2i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author Armin Reichert
 */
public class HouseTest {

	@Test
	public void testDoorWingNotNull() {
		assertThrows(NullPointerException.class, () -> new Door(null, v2i(0, 0)));
		assertThrows(NullPointerException.class, () -> new Door(v2i(0, 0), null));
	}

	@Test
	public void testArcadeHouseProperties() {
		var house = GameModel.createArcadeHouse();
		assertEquals(GameModel.ARCADE_HOUSE_POSITION,    house.topLeftTile());
		assertEquals(GameModel.ARCADE_HOUSE_SIZE,        house.size());
		assertEquals(GameModel.ARCADE_HOUSE_DOOR,        house.door());
		assertEquals(GameModel.ARCADE_HOUSE_SEAT_LEFT,   house.seat("left"));
		assertEquals(GameModel.ARCADE_HOUSE_SEAT_MIDDLE, house.seat("middle"));
		assertEquals(GameModel.ARCADE_HOUSE_SEAT_RIGHT,  house.seat("right"));
	}
}