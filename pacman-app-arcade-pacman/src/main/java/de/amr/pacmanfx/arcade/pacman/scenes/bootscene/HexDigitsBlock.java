package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.math.RandomNumbers;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public record HexDigitsBlock(byte[][] digits, int width, int height) implements Renderable {

    public HexDigitsBlock(int width, int height) {
        this(randomBytesBlock(width, height), width, height);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
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
