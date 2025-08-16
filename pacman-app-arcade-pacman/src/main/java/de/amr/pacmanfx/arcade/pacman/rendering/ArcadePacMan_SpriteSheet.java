/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.pacmanfx.lib.RectShort.rect;
import static java.util.Objects.requireNonNull;

public record ArcadePacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {

    private static final int R16 = 16; // 16x16 squares in sprite sheet
    private static final int OFF_X = 456; // right from here the sprites are located

    private static RectShort[] clipSpritesAt(int tileX, int tileY, int spriteCount) {
        return IntStream.range(tileX, tileX + spriteCount)
                .mapToObj(tx -> clipSpriteAt(tx, tileY))
                .toArray(RectShort[]::new);
    }

    private static RectShort clipSpriteAt(int tileX, int tileY) {
        return rect(OFF_X + R16 * tileX, R16 * tileY, R16, R16);
    }

    private static final SpriteMap<SpriteID> SPRITE_MAP = new SpriteMap<>(SpriteID.class);

    static {
        SPRITE_MAP.add(SpriteID.MAP_FULL, rect(0, 0, 224, 248));
        SPRITE_MAP.add(SpriteID.MAP_EMPTY, rect(228, 0, 224, 248));
        SPRITE_MAP.add(SpriteID.GHOST_NUMBERS,
            rect(456, 133, 15, 7),  // 200
            rect(472, 133, 15, 7),  // 400
            rect(488, 133, 15, 7),  // 800
            rect(504, 133, 16, 7)   // 1600
        );
        SPRITE_MAP.add(SpriteID.BONUS_SYMBOLS, IntStream.range(0, 8)
            .mapToObj(i -> rect(OFF_X + R16 * (2 + i), 49, 14, 14))
            .toArray(RectShort[]::new));
        SPRITE_MAP.add(SpriteID.BONUS_VALUES,
            rect(457, 148, 14, 7), //  100
            rect(472, 148, 15, 7), //  300
            rect(488, 148, 15, 7), //  500
            rect(504, 148, 15, 7), //  700
            rect(520, 148, 18, 7), // 1000
            rect(518, 164, 20, 7), // 2000
            rect(518, 180, 20, 7), // 3000
            rect(518, 196, 20, 7)  // 5000
        );
        SPRITE_MAP.add(SpriteID.LIVES_COUNTER_SYMBOL,  rect(OFF_X + 129, 15, 16, 16));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_RIGHT, makePacManMunchingSpriteSeq(0));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_LEFT,  makePacManMunchingSpriteSeq(1));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_UP,    makePacManMunchingSpriteSeq(2));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_DOWN,  makePacManMunchingSpriteSeq(3));
        SPRITE_MAP.add(SpriteID.PACMAN_DYING,          makePacManDyingSpriteSeq());
        SPRITE_MAP.add(SpriteID.RED_GHOST_RIGHT,       clipSpritesAt(0, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_LEFT,        clipSpritesAt(2, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_UP,          clipSpritesAt(4, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_DOWN,        clipSpritesAt(6, 4, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_RIGHT,      clipSpritesAt(0, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_LEFT,       clipSpritesAt(2, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_UP,         clipSpritesAt(4, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_DOWN,       clipSpritesAt(6, 5, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_RIGHT,      clipSpritesAt(0, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_LEFT,       clipSpritesAt(2, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_UP,         clipSpritesAt(4, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_DOWN,       clipSpritesAt(6, 6, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_RIGHT,    clipSpritesAt(0, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_LEFT,     clipSpritesAt(2, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_UP,       clipSpritesAt(4, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_DOWN,     clipSpritesAt(6, 7, 2));
        SPRITE_MAP.add(SpriteID.GALLERY_GHOSTS,
                clipSpriteAt(0, 4),
                clipSpriteAt(0, 5),
                clipSpriteAt(0, 6),
                clipSpriteAt(0, 7)
        );
        SPRITE_MAP.add(SpriteID.GHOST_FRIGHTENED,      clipSpritesAt(8, 4, 2));
        SPRITE_MAP.add(SpriteID.GHOST_FLASHING,        clipSpritesAt(8, 4, 4));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_RIGHT,      clipSpritesAt(8, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_LEFT,       clipSpritesAt(9, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_UP,         clipSpritesAt(10, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_DOWN,       clipSpritesAt(11, 5, 1));
        SPRITE_MAP.add(SpriteID.PACMAN_BIG,
                rect(OFF_X + 32, 16, 32, 32),
                rect(OFF_X + 64, 16, 32, 32),
                rect(OFF_X + 96, 16, 32, 32)
        );
        SPRITE_MAP.add(SpriteID.RED_GHOST_STRETCHED,   clipSpritesAt(8, 6, 5));
        SPRITE_MAP.add(SpriteID.RED_GHOST_DAMAGED,
                rect(OFF_X + R16 * 8 + 1, R16 * 7 + 1, 14, 14),
                rect(OFF_X + R16 * 9 + 1, R16 * 7 + 1, 14, 14)
        );
        SPRITE_MAP.add(SpriteID.RED_GHOST_PATCHED, clipSpritesAt(10, 7, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_NAKED,
                rect(OFF_X + R16 *  8, R16 * 8, R16 * 2, R16),
                rect(OFF_X + R16 * 10, R16 * 8, R16 * 2, R16)
        );

        SPRITE_MAP.checkCompleteness();
    }

    private static RectShort[] makePacManMunchingSpriteSeq(int dir) {
        int m = 1; // margin
        int size = 16 - 2 * m;
        RectShort wide   = rect(OFF_X      + m, dir * 16 + m, size, size);
        RectShort middle = rect(OFF_X + 16 + m, dir * 16 + m, size, size);
        RectShort closed = rect(OFF_X + 32 + m, m,            size, size);
        return new RectShort[] {closed, closed, middle, middle, wide, wide, middle, middle};
    }

    private static RectShort[] makePacManDyingSpriteSeq() {
        return IntStream.range(0, 11)
                .mapToObj(i -> rect(504 + i * 16, 0, 16, i <= 9 ? 15 : 16))
                .toArray(RectShort[]::new);
    }

    public ArcadePacMan_SpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public RectShort sprite(SpriteID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(SpriteID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}