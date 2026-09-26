/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.math.RandomNumbers;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;

public record HexDigitsBlock(byte[][] digits, int width, int height) implements Renderable {

    public static HexDigitsBlock randomHexDigits(int width, int height) {
        return new HexDigitsBlock(randomBytesBlock(width, height), width, height);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.BACKGROUND;
    }

    private static byte[][] randomBytesBlock(int width, int height) {
        final byte[][] block = new byte[height][width];
        final byte[] row0 = randomRow(width);
        final byte[] row1 = randomRow(width);
        for (int row = 0; row < height; ++row) {
            switch (RandomNumbers.randomInt(0, 2)) {
                case 0 -> block[row] = randomShift(row0);
                case 1 -> block[row] = randomShift(row1);
            }
        }
        return block;
    }

    private static byte[] randomRow(int width) {
        final byte[] row = new byte[width];
        for (int i = 0; i < row.length; i++) {
            row[i] = (byte) RandomNumbers.randomInt(0, 16);
        }
        return row;
    }

    private static byte[] randomShift(byte[] bytes) {
        final int n = bytes.length;
        final int offset = RandomNumbers.randomInt(1, n/2);
        final byte[] shifted = new byte[n];
        for (int i = 0; i < n; ++i) {
            final int k = offset + i;
            shifted[i] = bytes[k < n ? k : k - n];
        }
        return shifted;
    }
}
