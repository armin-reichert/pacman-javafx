/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.ms_pacman.SpriteID.*;
import static de.amr.pacmanfx.lib.Sprite.makeSprite;
import static java.util.Objects.requireNonNull;

public record ArcadeMsPacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {

    private static final byte R16 = 16;
    private static final int OFF_X = 456;

    // third "column" contains the sprites (first two columns the maze images)
    private static Sprite tile(int tileX, int tileY) {
        return makeSprite(OFF_X + R16 * tileX, R16 * tileY, R16, R16);
    }

    private static Sprite[] tilesRightOf(int tileX, int tileY, int numTiles) {
        return IntStream.range(tileX, tileX + numTiles)
                .mapToObj(x -> makeSprite(OFF_X + R16 * x, R16 * tileY, R16, R16))
                .toArray(Sprite[]::new);
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_RIGHT, makeMsPacManMunchingSpriteSeq(0));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_LEFT,  makeMsPacManMunchingSpriteSeq(1));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_UP,    makeMsPacManMunchingSpriteSeq(2));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_DOWN,  makeMsPacManMunchingSpriteSeq(3));
        SPRITE_MAP.put(MS_PACMAN_DYING,          makeMsPacManDyingSpriteSeq());
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_RIGHT, new Sprite[] {tile(0, 9),  tile(1, 9),  tile(2, 9)});
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_LEFT,  new Sprite[] {tile(0, 10), tile(1, 10), tile(2, 9)});
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_UP,    new Sprite[] {tile(0, 11), tile(1, 11), tile(2, 9)});
        SPRITE_MAP.put(MR_PACMAN_MUNCHING_DOWN,  new Sprite[] {tile(0, 12), tile(1, 12), tile(2, 9)});
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
        SPRITE_MAP.put(STORK,                    new Sprite[] {makeSprite(489, 176, 32, 16), makeSprite(521, 176, 32, 16)});
        SPRITE_MAP.put(CLAPPERBOARD,             new Sprite[] {
            makeSprite(456, 208, 32, 32),  // open
            makeSprite(488, 208, 32, 32),  // middle
            makeSprite(520, 208, 32, 32)   // closed
        });
        SPRITE_MAP.put(HEART,                    tile(2, 10));
        SPRITE_MAP.put(BLUE_BAG,                 makeSprite(488, 199, 8, 8));
        SPRITE_MAP.put(JUNIOR_PAC,               makeSprite(509, 200, 8, 8));
    }

    private static Sprite[] makeMsPacManMunchingSpriteSeq(int dir) {
        Sprite wide = tile(0, dir), open = tile(1, dir), closed = tile(2, dir);
        return new Sprite[] {open, open, wide, wide, open, open, open, closed, closed};
    }

    private static Sprite[] makeMsPacManDyingSpriteSeq() {
        Sprite right = tile(1, 0), left = tile(1, 1), up = tile(1, 2), down = tile(1, 3);
        // TODO: this is not yet 100% correct
        return new Sprite[] {down, left, up, right, down, left, up, right, down, left, up};
    }

    public ArcadeMsPacMan_SpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public Map<SpriteID, Object> spriteMap() {
        return SPRITE_MAP;
    }
}