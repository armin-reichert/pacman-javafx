/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public record ArcadeMapsSpriteSheet(Image sourceImage) implements SpriteSheet<ArcadeMapsSpriteSheet.MazeID> {

    // Size of Arcade maze (without the 3 empty rows above and the 2 below the maze!)
    private static final int MAZE_SPRITE_WIDTH  = 28 * TS;
    private static final int MAZE_SPRITE_HEIGHT = 31 * TS;

    public enum MazeID {
        MAZE1, MAZE2, MAZE3, MAZE4, MAZE5, MAZE6, MAZE7, MAZE8, MAZE9
    }

    private static final SpriteMap<MazeID> SPRITE_MAP = new SpriteMap<>(MazeID.class);

    private static RectShort spriteAtCell(int row, int col) {
        return new RectShort(col * MAZE_SPRITE_WIDTH, row * MAZE_SPRITE_HEIGHT, MAZE_SPRITE_WIDTH, MAZE_SPRITE_HEIGHT);
    }

    static {
        SPRITE_MAP.add(MazeID.MAZE1, spriteAtCell(0, 0));
        SPRITE_MAP.add(MazeID.MAZE2, spriteAtCell(0, 1));
        SPRITE_MAP.add(MazeID.MAZE3, spriteAtCell(0, 2));
        SPRITE_MAP.add(MazeID.MAZE4, spriteAtCell(1, 0));
        SPRITE_MAP.add(MazeID.MAZE5, spriteAtCell(1, 1));
        SPRITE_MAP.add(MazeID.MAZE6, spriteAtCell(1, 2));
        SPRITE_MAP.add(MazeID.MAZE7, spriteAtCell(2, 0));
        SPRITE_MAP.add(MazeID.MAZE8, spriteAtCell(2, 1));
        SPRITE_MAP.add(MazeID.MAZE9, spriteAtCell(2, 2));

        SPRITE_MAP.checkCompleteness();
    }

    public ArcadeMapsSpriteSheet {
        requireNonNull(sourceImage, "Sprite sheet source image must not be null");
    }

    @Override
    public RectShort sprite(MazeID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(MazeID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}