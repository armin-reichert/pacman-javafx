/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.lib.RectShort.rect;
import static java.util.Objects.requireNonNull;

public record ArcadeMsPacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {

    // left from this x position, the maps are located
    private static final int BEGIN_SPRITES_X = 456;

    private static RectShort tile(int tileX, int tileY) {
        return rect(BEGIN_SPRITES_X + 16 * tileX, 16 * tileY, 16, 16);
    }

    private static RectShort[] tilesRightOf(int tileX, int tileY, int numTiles) {
        if (numTiles <= 0) {
            throw new IllegalArgumentException("Number of tiles must be positive but is " + numTiles);
        }
        return IntStream.range(tileX, tileX + numTiles)
                .mapToObj(x -> rect(BEGIN_SPRITES_X + 16 * x, 16 * tileY, 16, 16))
                .toArray(RectShort[]::new);
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(FULL_MAZES, new RectShort[] {
                rect(0,     0, 224, 248),
                rect(0,   248, 224, 248),
                rect(0, 2*248, 224, 248),
                rect(0, 3*248, 224, 248),
                rect(0, 4*248, 224, 248),
                rect(0, 5*248, 224, 248),
        });
        SPRITE_MAP.put(EMPTY_MAZES, new RectShort[] {
                rect(228,     0, 224, 248),
                rect(228,   248, 224, 248),
                rect(228, 2*248, 224, 248),
                rect(228, 3*248, 224, 248),
                rect(228, 4*248, 224, 248),
                rect(228, 5*258, 224, 248),
        });
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_RIGHT, makeMsPacManMunchingSpriteSeq(0));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_LEFT,  makeMsPacManMunchingSpriteSeq(1));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_UP,    makeMsPacManMunchingSpriteSeq(2));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_DOWN,  makeMsPacManMunchingSpriteSeq(3));
        SPRITE_MAP.put(MS_PACMAN_DYING,          makeMsPacManDyingSpriteSeq());
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_RIGHT, tilesRightOf(0, 9, 3));
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_LEFT,  new RectShort[] {tile(0, 10), tile(1, 10), tile(2, 9)});
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_UP,    new RectShort[] {tile(0, 11), tile(1, 11), tile(2, 9)});
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_DOWN,  new RectShort[] {tile(0, 12), tile(1, 12), tile(2, 9)});
        SPRITE_MAP.put(RED_GHOST_RIGHT,          tilesRightOf(0, 4, 2));
        SPRITE_MAP.put(RED_GHOST_LEFT,           tilesRightOf(2, 4, 2));
        SPRITE_MAP.put(RED_GHOST_UP,             tilesRightOf(4, 4, 2));
        SPRITE_MAP.put(RED_GHOST_DOWN,           tilesRightOf(6, 4, 2));
        SPRITE_MAP.put(PINK_GHOST_RIGHT,         tilesRightOf(0, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_LEFT,          tilesRightOf(2, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_UP,            tilesRightOf(4, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_DOWN,          tilesRightOf(6, 5, 2));
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,         tilesRightOf(0, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_LEFT,          tilesRightOf(2, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_UP,            tilesRightOf(4, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_DOWN,          tilesRightOf(6, 6, 2));
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT,       tilesRightOf(0, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,        tilesRightOf(2, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_UP,          tilesRightOf(4, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,        tilesRightOf(6, 7, 2));
        SPRITE_MAP.put(GHOST_FRIGHTENED,         tilesRightOf(8, 4, 2));
        SPRITE_MAP.put(GHOST_FLASHING,           tilesRightOf(8, 4, 4));
        SPRITE_MAP.put(GHOST_EYES_RIGHT,         tilesRightOf(8, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_LEFT,          tilesRightOf(9, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_UP,            tilesRightOf(10, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_DOWN,          tilesRightOf(11, 5, 1));
        SPRITE_MAP.put(GHOST_NUMBERS,            tilesRightOf(0, 8, 4));
        SPRITE_MAP.put(BONUS_SYMBOLS,            tilesRightOf(3, 0, 7));
        SPRITE_MAP.put(BONUS_VALUES,             tilesRightOf(3, 1, 7));
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL,     tile(1, 0));
        SPRITE_MAP.put(STORK,                    new RectShort[] {rect(489, 176, 32, 16), rect(521, 176, 32, 16)});
        SPRITE_MAP.put(CLAPPERBOARD,             new RectShort[] {
            rect(456, 208, 32, 32),  // open
            rect(488, 208, 32, 32),  // middle
            rect(520, 208, 32, 32)   // closed
        });
        SPRITE_MAP.put(HEART,                    tile(2, 10));
        SPRITE_MAP.put(BLUE_BAG,                 rect(488, 199, 8, 8));
        SPRITE_MAP.put(JUNIOR_PAC,               rect(509, 200, 8, 8));
    }

    private static RectShort[] makeMsPacManMunchingSpriteSeq(int dir) {
        RectShort wide = tile(0, dir), open = tile(1, dir), closed = tile(2, dir);
        return new RectShort[] {open, open, wide, wide, open, open, open, closed, closed};
    }

    private static RectShort[] makeMsPacManDyingSpriteSeq() {
        RectShort right = tile(1, 0), left = tile(1, 1), up = tile(1, 2), down = tile(1, 3);
        // TODO: this is not yet 100% correct
        return new RectShort[] {down, left, up, right, down, left, up, right, down, left, up};
    }

    public ArcadeMsPacMan_SpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public Map<SpriteID, Object> spriteMap() {
        return SPRITE_MAP;
    }
}