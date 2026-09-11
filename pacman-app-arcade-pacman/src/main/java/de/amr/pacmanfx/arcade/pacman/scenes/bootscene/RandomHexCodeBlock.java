package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.math.RandomNumbers;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public record RandomHexCodeBlock(byte[] numbers, int width, int height) implements Renderable {

    public RandomHexCodeBlock(int width, int height) {
        this(randomNoise(), width, height);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

    private static byte[] randomNoise() {
        final byte[] numbers = new byte[Arcade_BootScene.TILE_WIDTH * Arcade_BootScene.TILE_HEIGHT];
        for (int i = 0; i < numbers.length; i++) {
            int row = i / Arcade_BootScene.TILE_WIDTH;
            //int col = i % Arcade_BootScene2D.TILE_WIDTH;
            if (RandomNumbers.chance(0.5) && row % 2 == 0) {
                continue;
            }
            numbers[i] = (byte) RandomNumbers.randomInt(0, 16);
        }
        return numbers;
    }
}
