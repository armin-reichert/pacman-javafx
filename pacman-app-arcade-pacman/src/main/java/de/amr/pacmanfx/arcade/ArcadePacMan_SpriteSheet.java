/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.SpriteID.*;
import static de.amr.pacmanfx.lib.RectArea.ra;
import static java.util.Objects.requireNonNull;

public record ArcadePacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    private static RectArea[] tilesRightOf(int tileX, int tileY, int numTiles) {
        return IntStream.range(tileX, tileX + numTiles)
                .mapToObj(x -> ra(OFF_X + R16 * x, R16 * tileY, R16, R16))
                .toArray(RectArea[]::new);
    }

    private static RectArea tile(int tileX, int tileY) { return ra(OFF_X + R16 * tileX, R16 * tileY, R16, R16); }

    private static final int R16 = 16; // 16x16 squares in sprite sheet
    private static final int OFF_X = 456;

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(MAP_FULL, ra(0, 0, 224, 248));
        SPRITE_MAP.put(MAP_EMPTY, ra(228, 0, 224, 248));
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            ra(456, 133, 15, 7),  // 200
            ra(472, 133, 15, 7),  // 400
            ra(488, 133, 15, 7),  // 800
            ra(504, 133, 16, 7)   // 1600
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 8)
            .mapToObj(i -> ra(OFF_X + R16 * (2 + i), 49, 14, 14))
            .toArray(RectArea[]::new));
        SPRITE_MAP.put(BONUS_VALUES, new RectArea[] {
            ra(457, 148, 14, 7), //  100
            ra(472, 148, 15, 7), //  300
            ra(488, 148, 15, 7), //  500
            ra(504, 148, 15, 7), //  700
            ra(520, 148, 18, 7), // 1000
            ra(518, 164, 20, 7), // 2000
            ra(518, 180, 20, 7), // 3000
            ra(518, 196, 20, 7), // 5000
        });
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, ra(OFF_X + 129, 15, 16, 16));
        SPRITE_MAP.put(PACMAN_MUNCHING_RIGHT, crappyPacManMunchingSpritesExtraction(0));
        SPRITE_MAP.put(PACMAN_MUNCHING_LEFT,  crappyPacManMunchingSpritesExtraction(1));
        SPRITE_MAP.put(PACMAN_MUNCHING_UP,    crappyPacManMunchingSpritesExtraction(2));
        SPRITE_MAP.put(PACMAN_MUNCHING_DOWN,  crappyPacManMunchingSpritesExtraction(3));
        SPRITE_MAP.put(PACMAN_DYING,          crappyPacManDyingSpriteExtraction());
        SPRITE_MAP.put(RED_GHOST_RIGHT,       tilesRightOf(0, 4, 2));
        SPRITE_MAP.put(RED_GHOST_LEFT,        tilesRightOf(2, 4, 2));
        SPRITE_MAP.put(RED_GHOST_UP,          tilesRightOf(4, 4, 2));
        SPRITE_MAP.put(RED_GHOST_DOWN,        tilesRightOf(6, 4, 2));
        SPRITE_MAP.put(PINK_GHOST_RIGHT,      tilesRightOf(0, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_LEFT,       tilesRightOf(2, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_UP,         tilesRightOf(4, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_DOWN,       tilesRightOf(6, 5, 2));
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,      tilesRightOf(0, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_LEFT,       tilesRightOf(2, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_UP,         tilesRightOf(4, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_DOWN,       tilesRightOf(6, 6, 2));
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT,    tilesRightOf(0, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,     tilesRightOf(2, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_UP,       tilesRightOf(4, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,     tilesRightOf(6, 7, 2));
        SPRITE_MAP.put(GALLERY_GHOSTS,        new RectArea[] { tile(0, 4), tile(0, 5), tile(0, 6), tile(0, 7) });
        SPRITE_MAP.put(GHOST_FRIGHTENED,      tilesRightOf(8, 4, 2));
        SPRITE_MAP.put(GHOST_FLASHING,        tilesRightOf(8, 4, 4));
        SPRITE_MAP.put(GHOST_EYES_RIGHT,      tilesRightOf(8, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_LEFT,       tilesRightOf(9, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_UP,         tilesRightOf(10, 5, 1));
        SPRITE_MAP.put(GHOST_EYES_DOWN,       tilesRightOf(11, 5, 1));
        SPRITE_MAP.put(PACMAN_BIG,            new RectArea[] {
                ra(OFF_X + 32, 16, 32, 32), ra(OFF_X + 64, 16, 32, 32), ra(OFF_X + 96, 16, 32, 32)
        });
        SPRITE_MAP.put(RED_GHOST_STRETCHED,   IntStream.range(0,5).mapToObj(i -> tile(8 + i, 6)).toArray(RectArea[]::new));
        SPRITE_MAP.put(RED_GHOST_DAMAGED,     new RectArea[] {
                ra(OFF_X + R16 * 8 + 1, R16 * 7 + 1, 14, 14),
                ra(OFF_X + R16 * 9 + 1, R16 * 7 + 1, 14, 14)
        });
        SPRITE_MAP.put(RED_GHOST_PATCHED, new RectArea[] { tile(10, 7), tile(11, 7) });
        SPRITE_MAP.put(RED_GHOST_NAKED, new RectArea[] {
                ra(OFF_X + R16 * 8, R16 * 8, 2 * R16, R16),
                ra(OFF_X + R16 * 10, R16 * 8, 2 * R16, R16)
        });
    }

    public static RectArea   sprite(SpriteID spriteID)  { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] sprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    private static RectArea[] crappyPacManMunchingSpritesExtraction(int dir) {
        byte margin = 1;
        int size = R16 - 2 * margin;
        RectArea wide = ra(OFF_X + margin, dir * 16 + margin, size, size);
        RectArea middle = ra(OFF_X + 16 + margin, dir * 16 + margin, size, size);
        RectArea closed = ra(OFF_X + 32 + margin, margin, size, size);
        return new RectArea[] {closed, closed, middle, middle, wide, wide, middle, middle};
    }

    private static RectArea[] crappyPacManDyingSpriteExtraction() {
        return IntStream.range(0, 12)
                .mapToObj(i -> ra(504 + i * R16, 0, R16 - 1, i == 11 ? R16 : R16 - 1))
                .toArray(RectArea[]::new);
    }

    public ArcadePacMan_SpriteSheet(Image sourceImage) {
        this.sourceImage = requireNonNull(sourceImage);
    }

    @Override
    public RectArea[] ghostNumberSprites() { return sprites(GHOST_NUMBERS); }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) { return sprites(BONUS_SYMBOLS)[symbol]; }

    @Override
    public RectArea bonusValueSprite(byte symbol) { return sprites(BONUS_VALUES)[symbol]; }

    @Override
    public RectArea livesCounterSprite() { return sprite(LIVES_COUNTER_SYMBOL); }

}