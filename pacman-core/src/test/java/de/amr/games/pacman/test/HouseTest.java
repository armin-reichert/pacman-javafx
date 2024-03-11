/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import de.amr.games.pacman.model.world.Door;
import org.junit.Test;

import static de.amr.games.pacman.lib.Globals.v2i;
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
}