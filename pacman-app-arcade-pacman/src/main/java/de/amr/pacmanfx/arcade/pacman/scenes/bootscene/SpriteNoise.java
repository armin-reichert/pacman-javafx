package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;
import javafx.geometry.Rectangle2D;

import static de.amr.basics.math.MathAdds.lerp;
import static de.amr.basics.math.RandomNumbers.randomFloat;
import static de.amr.basics.math.RandomNumbers.randomInt;
import static de.amr.basics.math.RectShort.sprite;
import static java.lang.Math.clamp;

public record SpriteNoise(RectShort[] sprites, int width, int height) implements Renderable {

    private static final Rectangle2D BOOT_SCENE_SPRITES = new Rectangle2D(400, 0, 256, 160);
    public static final int GRID_SIZE = 16;

    public SpriteNoise(int width, int height) {
        this(randomSprites(width, height), width, height);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

    private static RectShort[] randomSprites(int width, int height) {
        final RectShort[] sprites = new RectShort[width * height];
        for (int row = 0; row < width; ++row) {
            final RectShort f1 = randomSpriteFragment();
            final RectShort f2 = randomSpriteFragment();
            final int splitCol = height / 8 + randomInt(0, height / 4);
            for (int col = 0; col < height; ++col) {
                sprites[row * height + col] = col < splitCol ? f1 : f2;
            }
        }
        return sprites;
    }

    private static RectShort randomSpriteFragment() {
        double xMin = lerp(BOOT_SCENE_SPRITES.getMinX(), BOOT_SCENE_SPRITES.getMaxX(), randomFloat(0, 1));
        xMin = clamp(xMin, BOOT_SCENE_SPRITES.getMinX(), BOOT_SCENE_SPRITES.getMaxX() - GRID_SIZE);
        double yMin = lerp(BOOT_SCENE_SPRITES.getMinY(), BOOT_SCENE_SPRITES.getMaxY(), randomFloat(0, 1));
        yMin = clamp(yMin, BOOT_SCENE_SPRITES.getMinY(), BOOT_SCENE_SPRITES.getMaxY() - GRID_SIZE);
        return sprite((short) xMin, (short) yMin, GRID_SIZE, GRID_SIZE);
    }
}
