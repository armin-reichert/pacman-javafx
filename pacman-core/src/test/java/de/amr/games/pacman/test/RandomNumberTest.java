/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.test;

import org.junit.Assert;
import org.junit.Test;

import static de.amr.games.pacman.lib.Globals.randomFloat;
import static de.amr.games.pacman.lib.Globals.randomInt;

/**
 * @author Armin Reichert
 */
public class RandomNumberTest {

    static final int N = 1_000_000;

    @Test
    public void testRandomInt() {
        for (int i = 0; i < N; ++i) {
            var number = randomInt(10, 100);
            Assert.assertTrue(10 <= number && number < 100);
        }
    }

    @Test
    public void testRandomFloat() {
        for (int i = 0; i < N; ++i) {
            var number = randomFloat(10.0f, 100.0f);
            Assert.assertTrue(10.0f <= number && number < 100.0f);
        }
    }
}