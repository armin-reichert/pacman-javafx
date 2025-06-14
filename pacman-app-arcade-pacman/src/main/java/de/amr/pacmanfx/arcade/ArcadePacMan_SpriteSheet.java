/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.SpriteID.*;
import static de.amr.pacmanfx.lib.Sprite.makeSprite;
import static java.util.Objects.requireNonNull;

public record ArcadePacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {
    private static final int R16 = 16; // 16x16 squares in sprite sheet
    private static final int OFF_X = 456; // right from here the sprites are located

    private static Sprite[] collectSprites(int tileX, int tileY, int spriteCount) {
        return IntStream.range(tileX, tileX + spriteCount)
                .mapToObj(tx -> makeSpriteAt(tx, tileY))
                .toArray(Sprite[]::new);
    }

    private static Sprite makeSpriteAt(int tileX, int tileY) {
        return makeSprite(OFF_X + R16 * tileX, R16 * tileY, R16, R16);
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(MAP_FULL, makeSprite(0, 0, 224, 248));
        SPRITE_MAP.put(MAP_EMPTY, makeSprite(228, 0, 224, 248));
        SPRITE_MAP.put(GHOST_NUMBERS, new Sprite[] {
            makeSprite(456, 133, 15, 7),  // 200
            makeSprite(472, 133, 15, 7),  // 400
            makeSprite(488, 133, 15, 7),  // 800
            makeSprite(504, 133, 16, 7)   // 1600
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 8)
            .mapToObj(i -> makeSprite(OFF_X + R16 * (2 + i), 49, 14, 14))
            .toArray(Sprite[]::new));
        SPRITE_MAP.put(BONUS_VALUES, new Sprite[] {
            makeSprite(457, 148, 14, 7), //  100
            makeSprite(472, 148, 15, 7), //  300
            makeSprite(488, 148, 15, 7), //  500
            makeSprite(504, 148, 15, 7), //  700
            makeSprite(520, 148, 18, 7), // 1000
            makeSprite(518, 164, 20, 7), // 2000
            makeSprite(518, 180, 20, 7), // 3000
            makeSprite(518, 196, 20, 7), // 5000
        });
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL,  makeSprite(OFF_X + 129, 15, 16, 16));
        SPRITE_MAP.put(PACMAN_MUNCHING_RIGHT, makePacManMunchingSpriteSeq(0));
        SPRITE_MAP.put(PACMAN_MUNCHING_LEFT,  makePacManMunchingSpriteSeq(1));
        SPRITE_MAP.put(PACMAN_MUNCHING_UP,    makePacManMunchingSpriteSeq(2));
        SPRITE_MAP.put(PACMAN_MUNCHING_DOWN,  makePacManMunchingSpriteSeq(3));
        SPRITE_MAP.put(PACMAN_DYING,          makePacManDyingSpriteSeq());
        SPRITE_MAP.put(RED_GHOST_RIGHT,       collectSprites(0, 4, 2));
        SPRITE_MAP.put(RED_GHOST_LEFT,        collectSprites(2, 4, 2));
        SPRITE_MAP.put(RED_GHOST_UP,          collectSprites(4, 4, 2));
        SPRITE_MAP.put(RED_GHOST_DOWN,        collectSprites(6, 4, 2));
        SPRITE_MAP.put(PINK_GHOST_RIGHT,      collectSprites(0, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_LEFT,       collectSprites(2, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_UP,         collectSprites(4, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_DOWN,       collectSprites(6, 5, 2));
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,      collectSprites(0, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_LEFT,       collectSprites(2, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_UP,         collectSprites(4, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_DOWN,       collectSprites(6, 6, 2));
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT,    collectSprites(0, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,     collectSprites(2, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_UP,       collectSprites(4, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,     collectSprites(6, 7, 2));
        SPRITE_MAP.put(GALLERY_GHOSTS,        new Sprite[] {
                makeSpriteAt(0, 4),
                makeSpriteAt(0, 5),
                makeSpriteAt(0, 6),
                makeSpriteAt(0, 7) });
        SPRITE_MAP.put(GHOST_FRIGHTENED,      collectSprites(8, 4, 2));
        SPRITE_MAP.put(GHOST_FLASHING,        collectSprites(8, 4, 4));
        SPRITE_MAP.put(GHOST_EYES_RIGHT,      collectSprites(8, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_LEFT,       collectSprites(9, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_UP,         collectSprites(10, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_DOWN,       collectSprites(11, 5, 1));
        SPRITE_MAP.put(PACMAN_BIG,            new Sprite[] {
                makeSprite(OFF_X + 32, 16, 32, 32), makeSprite(OFF_X + 64, 16, 32, 32), makeSprite(OFF_X + 96, 16, 32, 32)
        });
        SPRITE_MAP.put(RED_GHOST_STRETCHED,   IntStream.range(0, 5).mapToObj(i -> makeSpriteAt(8 + i, 6)).toArray(Sprite[]::new));
        SPRITE_MAP.put(RED_GHOST_DAMAGED,     new Sprite[] {
                makeSprite(OFF_X + R16 * 8 + 1, R16 * 7 + 1, 14, 14),
                makeSprite(OFF_X + R16 * 9 + 1, R16 * 7 + 1, 14, 14)
        });
        SPRITE_MAP.put(RED_GHOST_PATCHED, collectSprites(10, 7, 2));
        SPRITE_MAP.put(RED_GHOST_NAKED, new Sprite[] {
                makeSprite(OFF_X + R16 * 8, R16 * 8, 2 * R16, R16),
                makeSprite(OFF_X + R16 * 10, R16 * 8, 2 * R16, R16)
        });
    }

    private static Sprite[] makePacManMunchingSpriteSeq(int dir) {
        byte margin = 1;
        int size = R16 - 2 * margin;
        Sprite wide = makeSprite(OFF_X + margin, dir * 16 + margin, size, size);
        Sprite middle = makeSprite(OFF_X + 16 + margin, dir * 16 + margin, size, size);
        Sprite closed = makeSprite(OFF_X + 32 + margin, margin, size, size);
        return new Sprite[] {closed, closed, middle, middle, wide, wide, middle, middle};
    }

    private static Sprite[] makePacManDyingSpriteSeq() {
        return IntStream.range(0, 12)
                .mapToObj(i -> makeSprite(504 + i * R16, 0, R16 - 1, i == 11 ? R16 : R16 - 1))
                .toArray(Sprite[]::new);
    }

    public ArcadePacMan_SpriteSheet(Image sourceImage) {
        this.sourceImage = requireNonNull(sourceImage);
    }

    public Sprite sprite(SpriteID spriteID)  { return (Sprite) SPRITE_MAP.get(spriteID); }

    public Sprite[] spriteSeq(SpriteID spriteID) { return (Sprite[]) SPRITE_MAP.get(spriteID); }

}