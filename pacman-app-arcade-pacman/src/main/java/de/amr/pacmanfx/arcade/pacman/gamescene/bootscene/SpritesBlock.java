/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;

import static de.amr.basics.math.MathAdds.lerp;
import static de.amr.basics.math.RandomNumbers.randomFloat;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static java.lang.Math.clamp;

public record SpritesBlock(RectShort[] sprites, int spriteSize, int numSpritesX, int numSpritesY) implements Renderable {

    private static final RectShort BOOT_SCENE_SPRITES_REGION = RectShort.sprite(400, 0, 256, 160);

    public static SpritesBlock randomSpritesBlock(int spriteSize, int numSpritesX, int numSpritesY) {
        final RectShort[] sprites = new RectShort[numSpritesX * numSpritesY];
        for (int row = 0; row < numSpritesY; ++row) {
            final RectShort s1 = randomSprite(BOOT_SCENE_SPRITES_REGION, spriteSize);
            final RectShort s2 = randomSprite(BOOT_SCENE_SPRITES_REGION, spriteSize);
            final int splitCol = numSpritesX / 8 + randomInt(0, numSpritesY / 4);
            for (int col = 0; col < numSpritesX; ++col) {
                sprites[row * numSpritesX + col] = col < splitCol ? s1 : s2;
            }
        }
        return new SpritesBlock(sprites, spriteSize, numSpritesX, numSpritesY);
    }

    public static RectShort randomSprite(RectShort region, int spriteSize) {
        final float xMin = region.x(), xMax = xMin + region.width();
        final float yMin = region.y(), yMax = yMin + region.height();
        float x = lerp(xMin, xMax, randomFloat(0, 1));
        float y = lerp(yMin, yMax, randomFloat(0, 1));
        x = clamp(x, xMin, xMax - spriteSize);
        y = clamp(y, yMin, yMax - spriteSize);
        return RectShort.sprite((int) clamp(x, xMin, xMax - spriteSize), (int) clamp(y, yMin, yMax - spriteSize), spriteSize, spriteSize);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.BACKGROUND;
    }
}
