/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;

public record ArcadeMapsSpriteSheet(Image sourceImage) implements SpriteSheet<ArcadeMapsSpriteSheet.ArcadeMapID> {

    // Size of Arcade maze (without the 3 empty rows above and the 2 below the maze!)
    private static final int MAZE_SPRITE_WIDTH  = 28 * TS;
    private static final int MAZE_SPRITE_HEIGHT = 31 * TS;

    public enum ArcadeMapID {
        MAZE1, MAZE2, MAZE3, MAZE4, MAZE5, MAZE6, MAZE7, MAZE8, MAZE9;
    }

    private static final SpriteMap<ArcadeMapID> SPRITE_MAP = new SpriteMap<>(ArcadeMapID.class);

    private static RectShort crop(int row, int col) {
        return new RectShort(col * MAZE_SPRITE_WIDTH, row * MAZE_SPRITE_HEIGHT, MAZE_SPRITE_WIDTH, MAZE_SPRITE_HEIGHT);
    }

    static {
        SPRITE_MAP.add(ArcadeMapID.MAZE1, crop(0, 0));
        SPRITE_MAP.add(ArcadeMapID.MAZE2, crop(0, 1));
        SPRITE_MAP.add(ArcadeMapID.MAZE3, crop(0, 2));
        SPRITE_MAP.add(ArcadeMapID.MAZE4, crop(1, 0));
        SPRITE_MAP.add(ArcadeMapID.MAZE5, crop(1, 1));
        SPRITE_MAP.add(ArcadeMapID.MAZE6, crop(1, 2));
        SPRITE_MAP.add(ArcadeMapID.MAZE7, crop(2, 0));
        SPRITE_MAP.add(ArcadeMapID.MAZE8, crop(2, 1));
        SPRITE_MAP.add(ArcadeMapID.MAZE9, crop(2, 2));
    }

    @Override
    public RectShort sprite(ArcadeMapID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(ArcadeMapID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}
