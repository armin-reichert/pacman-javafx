/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.image.Image;

import static de.amr.games.pacman.lib.RectArea.rect;

public class TengenNonArcadeMapsSpriteSheet {

    static final Vector2i SIZE_M = new Vector2i(28*8, 31*8);
    static final Vector2i SIZE_S = new Vector2i(28*8, 25*8);
    static final Vector2i SIZE_L = new Vector2i(28*8, 37*8);

    private static final Vector2i[] SPRITE_SIZES = {
        SIZE_M, SIZE_M, SIZE_M, SIZE_M, SIZE_M, SIZE_M, SIZE_M.minus(0, 8), SIZE_M,
        SIZE_M, SIZE_L, SIZE_M, SIZE_M, SIZE_M, SIZE_L, SIZE_M, SIZE_S,
        SIZE_L, SIZE_M, SIZE_L, SIZE_L, SIZE_L, SIZE_L, SIZE_L, SIZE_M,
        SIZE_L, SIZE_L, SIZE_M, SIZE_S, SIZE_M, SIZE_S, SIZE_M, SIZE_M, SIZE_L,
        SIZE_S, SIZE_S, SIZE_S, SIZE_S,
    };

    /**
     * @param spriteNumber number (1 based) of map sprite in sprite sheet (row-wise)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map sprite in non-Arcade maps sprite sheet
     */
    private static RectArea nonArcadeMapSprite(int spriteNumber, int width, int height) {
        int col, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { col = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { col = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { col = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { col = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { col = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        return new RectArea(col * width, y, width, height);
    }

    // Map #32 has 3 different images to create an animation effect, frame cycle: (0, 1, 2, 1)+
    static final RectArea[] MAP_32_ANIMATION_FRAMES = {
        rect(1568, 840, 224, 248),  // 0
        rect(1568, 1088, 224, 248), // 1
        rect(1568, 1336, 224, 248), // 2
        rect(1568, 1088, 224, 248), // 1
    };

    private final Image image;

    public TengenNonArcadeMapsSpriteSheet(AssetStorage assets) {
        image = assets.image("tengen.mazes.non_arcade");
    }

    public ImageArea mapSprite(int spriteNumber) {
        Vector2i size = SPRITE_SIZES[spriteNumber - 1];
        return new ImageArea(image, nonArcadeMapSprite(spriteNumber, size.x(), size.y()));
    }
}
