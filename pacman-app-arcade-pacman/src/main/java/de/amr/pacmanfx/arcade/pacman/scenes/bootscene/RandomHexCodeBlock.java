package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.math.RandomNumbers;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public record RandomHexCodeBlock(byte[] hexDigits, int width, int height) implements Renderable {

    public RandomHexCodeBlock(int width, int height) {
        this(randomNoise(), width, height);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

    private static byte[] randomNoise() {
        final byte[] arr = new byte[Arcade_BootScene2D.TILE_WIDTH * Arcade_BootScene2D.TILE_HEIGHT];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) RandomNumbers.randomInt(0, 16);
        }
        return arr;
    }
}
