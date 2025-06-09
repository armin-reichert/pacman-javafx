/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.ArcadePacMan_SpriteSheet.SpriteID.*;
import static de.amr.pacmanfx.lib.RectArea.rect;
import static de.amr.pacmanfx.uilib.assets.SpriteSheet.rectAreaArray;
import static java.util.Objects.requireNonNull;

public record ArcadePacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    /**
     * @param tileX    grid column (in tile coordinates)
     * @param tileY    grid row (in tile coordinates)
     * @param numTiles number of tiles
     * @return horizontal stripe of tiles at given grid position
     */
    private static RectArea[] tilesRightOf(int tileX, int tileY, int numTiles) {
        return IntStream.range(tileX, tileX + numTiles)
                .mapToObj(x -> rect(OFF_X + R16 * x, R16 * tileY, R16, R16))
                .toArray(RectArea[]::new);
    }

    private static RectArea tile(int tileX, int tileY) { return rect(OFF_X + R16 * tileX, R16 * tileY, R16, R16); }

    private static final int R16 = 16; // 16x16 squares in sprite sheet
    private static final int OFF_X = 456;

    public enum SpriteID {
        MAP_FULL,
        MAP_EMPTY,
        RED_GHOST_RIGHT, RED_GHOST_LEFT, RED_GHOST_UP, RED_GHOST_DOWN,
        PINK_GHOST_RIGHT, PINK_GHOST_LEFT, PINK_GHOST_UP, PINK_GHOST_DOWN,
        CYAN_GHOST_RIGHT, CYAN_GHOST_LEFT, CYAN_GHOST_UP, CYAN_GHOST_DOWN,
        ORANGE_GHOST_RIGHT, ORANGE_GHOST_LEFT, ORANGE_GHOST_UP, ORANGE_GHOST_DOWN,
        GHOST_FRIGHTENED,
        GHOST_FLASHING,
        GHOST_EYES_RIGHT, GHOST_EYES_LEFT, GHOST_EYES_UP, GHOST_EYES_DOWN,
        GHOST_NUMBERS,
        BONUS_SYMBOLS,
        BONUS_VALUES,
        GALLERY_GHOSTS,
        LIVES_COUNTER_SYMBOL,
        PACMAN_MUNCHING_RIGHT, PACMAN_MUNCHING_LEFT, PACMAN_MUNCHING_UP, PACMAN_MUNCHING_DOWN,
        PACMAN_DYING,
        PACMAN_BIG,
        RED_GHOST_STRETCHED,
        RED_GHOST_DAMAGED,
        RED_GHOST_PATCHED,
        RED_GHOST_NAKED
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(MAP_FULL, rect(0, 0, 224, 248));
        SPRITE_MAP.put(MAP_EMPTY, rect(228, 0, 224, 248));
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            rect(456, 133, 15, 7),  // 200
            rect(472, 133, 15, 7),  // 400
            rect(488, 133, 15, 7),  // 800
            rect(504, 133, 16, 7)   // 1600
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 8)
            .mapToObj(i -> rect(OFF_X + R16 * (2 + i), 49, 14, 14))
            .toArray(RectArea[]::new));
        SPRITE_MAP.put(BONUS_VALUES, new RectArea[] {
            rect(457, 148, 14, 7), //  100
            rect(472, 148, 15, 7), //  300
            rect(488, 148, 15, 7), //  500
            rect(504, 148, 15, 7), //  700
            rect(520, 148, 18, 7), // 1000
            rect(518, 164, 20, 7), // 2000
            rect(518, 180, 20, 7), // 3000
            rect(518, 196, 20, 7), // 5000
        });
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, rect(OFF_X + 129, 15, 16, 16));
        SPRITE_MAP.put(PACMAN_MUNCHING_RIGHT, sillyPacManMunchingSpritesExtraction(0));
        SPRITE_MAP.put(PACMAN_MUNCHING_LEFT,  sillyPacManMunchingSpritesExtraction(1));
        SPRITE_MAP.put(PACMAN_MUNCHING_UP,    sillyPacManMunchingSpritesExtraction(2));
        SPRITE_MAP.put(PACMAN_MUNCHING_DOWN,  sillyPacManMunchingSpritesExtraction(3));
        SPRITE_MAP.put(PACMAN_DYING,          sillyPacManDyingSpriteExtraction());
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
                rect(OFF_X + 32, 16, 32, 32), rect(OFF_X + 64, 16, 32, 32), rect(OFF_X + 96, 16, 32, 32)
        });
        SPRITE_MAP.put(RED_GHOST_STRETCHED,   IntStream.range(0,5).mapToObj(i -> tile(8 + i, 6)).toArray(RectArea[]::new));
        SPRITE_MAP.put(RED_GHOST_DAMAGED,     new RectArea[] {
                rect(OFF_X + R16 * 8 + 1, R16 * 7 + 1, 14, 14),
                rect(OFF_X + R16 * 9 + 1, R16 * 7 + 1, 14, 14)
        });
        SPRITE_MAP.put(RED_GHOST_PATCHED, new RectArea[] { tile(10, 7), tile(11, 7) });
        SPRITE_MAP.put(RED_GHOST_NAKED, new RectArea[] {
                rect(OFF_X + R16 * 8, R16 * 8, 2 * R16, R16),
                rect(OFF_X + R16 * 10, R16 * 8, 2 * R16, R16)
        });
    }

    public static RectArea getSprite(SpriteID spriteID) { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] getSprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    private static RectArea[] sillyPacManMunchingSpritesExtraction(int dir) {
        byte margin = 1;
        int size = R16 - 2 * margin;
        RectArea wide = rect(OFF_X + margin, dir * 16 + margin, size, size);
        RectArea middle = rect(OFF_X + 16 + margin, dir * 16 + margin, size, size);
        RectArea closed = rect(OFF_X + 32 + margin, margin, size, size);
        return rectAreaArray(closed, closed, middle, middle, wide, wide, middle, middle);
    }

    private static RectArea[] sillyPacManDyingSpriteExtraction() {
        return IntStream.range(0, 12)
                .mapToObj(i -> rect(504 + i * R16, 0, R16 - 1, i == 11 ? R16 : R16 - 1))
                .toArray(RectArea[]::new);
    }

    public ArcadePacMan_SpriteSheet(Image sourceImage) {
        this.sourceImage = requireNonNull(sourceImage);
    }

    @Override
    public RectArea[] ghostNumberSprites() { return getSprites(GHOST_NUMBERS); }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) { return getSprites(BONUS_SYMBOLS)[symbol]; }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        return getSprites(BONUS_VALUES)[symbol];
    }

    @Override
    public RectArea livesCounterSprite() { return getSprite(LIVES_COUNTER_SYMBOL); }

    public RectArea[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> getSprites(PACMAN_MUNCHING_RIGHT);
            case LEFT -> getSprites(PACMAN_MUNCHING_LEFT);
            case UP -> getSprites(PACMAN_MUNCHING_UP);
            case DOWN -> getSprites(PACMAN_MUNCHING_DOWN);
        };
    }

    @Override
    public RectArea[] pacDyingSprites() { return getSprites(PACMAN_DYING); }

    @Override
    public RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return getSprites(switch (id) {
            case 0 -> switch (dir) {
                case RIGHT -> RED_GHOST_RIGHT;
                case LEFT -> RED_GHOST_LEFT;
                case UP -> RED_GHOST_UP;
                case DOWN -> RED_GHOST_DOWN;
            };
            case 1 -> switch (dir) {
                case RIGHT -> PINK_GHOST_RIGHT;
                case LEFT -> PINK_GHOST_LEFT;
                case UP -> PINK_GHOST_UP;
                case DOWN -> PINK_GHOST_DOWN;
            };
            case 2 -> switch (dir) {
                case RIGHT -> CYAN_GHOST_RIGHT;
                case LEFT -> CYAN_GHOST_LEFT;
                case UP -> CYAN_GHOST_UP;
                case DOWN -> CYAN_GHOST_DOWN;
            };
            case 3 -> switch (dir) {
                case RIGHT -> ORANGE_GHOST_RIGHT;
                case LEFT -> ORANGE_GHOST_LEFT;
                case UP -> ORANGE_GHOST_UP;
                case DOWN -> ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException("Illegal ghost ID " + id);
        });
    }

    @Override
    public RectArea[] ghostFrightenedSprites() { return getSprites(GHOST_FRIGHTENED); }

    @Override
    public RectArea[] ghostFlashingSprites() { return getSprites(GHOST_FLASHING); }

    @Override
    public RectArea[] ghostEyesSprites(Direction dir) {
        return getSprites(switch (dir) {
            case Direction.RIGHT -> GHOST_EYES_RIGHT;
            case Direction.LEFT  -> GHOST_EYES_LEFT;
            case Direction.UP    -> GHOST_EYES_UP;
            case Direction.DOWN  -> GHOST_EYES_DOWN;
        });
    }
}