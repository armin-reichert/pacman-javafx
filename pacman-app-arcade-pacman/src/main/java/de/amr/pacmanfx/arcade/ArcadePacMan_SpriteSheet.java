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
import java.util.List;
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
                .mapToObj(x -> rect(OFF_X + RASTER * x, RASTER * tileY, RASTER, RASTER))
                .toArray(RectArea[]::new);
    }

    private static final int RASTER = 16; // 16x16 squares in sprite sheet
    private static final int OFF_X = 456;

    private static final List<Direction> DIR_ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    static final RectArea FULL_MAZE_SPRITE = rect(0, 0, 224, 248);
    static final RectArea EMPTY_MAZE_SPRITE = rect(228, 0, 224, 248);

    public enum SpriteID {
        RED_GHOST_RIGHT, RED_GHOST_LEFT, RED_GHOST_UP, RED_GHOST_DOWN,
        PINK_GHOST_RIGHT, PINK_GHOST_LEFT, PINK_GHOST_UP, PINK_GHOST_DOWN,
        CYAN_GHOST_RIGHT, CYAN_GHOST_LEFT, CYAN_GHOST_UP, CYAN_GHOST_DOWN,
        ORANGE_GHOST_RIGHT, ORANGE_GHOST_LEFT, ORANGE_GHOST_UP, ORANGE_GHOST_DOWN,
        GHOST_NUMBERS,
        BONUS_SYMBOLS,
        BONUS_VALUES,
        GALLERY_GHOSTS,
        LIVES_COUNTER_SYMBOL,
        PACMAN_MUNCHING_RIGHT, PACMAN_MUNCHING_LEFT, PACMAN_MUNCHING_UP, PACMAN_MUNCHING_DOWN,
        PACMAN_DYING,
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            rect(456, 133, 15, 7),  // 200
            rect(472, 133, 15, 7),  // 400
            rect(488, 133, 15, 7),  // 800
            rect(504, 133, 16, 7)   // 1600
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 8)
            .mapToObj(i -> rect(OFF_X + RASTER * (2 + i), 49, 14, 14))
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
        SPRITE_MAP.put(GALLERY_GHOSTS, sillyGalleryGhostSpriteExtraction());
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, rect(OFF_X + 129, 15, 16, 16));
        SPRITE_MAP.put(PACMAN_MUNCHING_RIGHT, sillyPacManMunchingSpritesExtraction(0));
        SPRITE_MAP.put(PACMAN_MUNCHING_LEFT, sillyPacManMunchingSpritesExtraction(1));
        SPRITE_MAP.put(PACMAN_MUNCHING_UP, sillyPacManMunchingSpritesExtraction(2));
        SPRITE_MAP.put(PACMAN_MUNCHING_DOWN, sillyPacManMunchingSpritesExtraction(3));
        SPRITE_MAP.put(PACMAN_DYING, sillyPacManDyingSpriteExtraction());
        SPRITE_MAP.put(RED_GHOST_RIGHT,    tilesRightOf(0, 4, 2));
        SPRITE_MAP.put(RED_GHOST_LEFT,     tilesRightOf(2, 4, 2));
        SPRITE_MAP.put(RED_GHOST_UP,       tilesRightOf(4, 4, 2));
        SPRITE_MAP.put(RED_GHOST_DOWN,     tilesRightOf(6, 4, 2));
        SPRITE_MAP.put(PINK_GHOST_RIGHT,   tilesRightOf(0, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_LEFT,    tilesRightOf(2, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_UP,      tilesRightOf(4, 5, 2));
        SPRITE_MAP.put(PINK_GHOST_DOWN,    tilesRightOf(6, 5, 2));
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,   tilesRightOf(0, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_LEFT,    tilesRightOf(2, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_UP,      tilesRightOf(4, 6, 2));
        SPRITE_MAP.put(CYAN_GHOST_DOWN,    tilesRightOf(6, 6, 2));
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT, tilesRightOf(0, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,  tilesRightOf(2, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_UP,    tilesRightOf(4, 7, 2));
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,  tilesRightOf(6, 7, 2));
    }

    public static RectArea getSprite(SpriteID spriteID) { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] getSprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    private static RectArea[] sillyGalleryGhostSpriteExtraction() {
        return IntStream.range(0, 4)
                .mapToObj(id -> rect(OFF_X + RASTER * (2 * DIR_ORDER.indexOf(Direction.RIGHT)), RASTER * (4 + id), RASTER, RASTER))
                .toArray(RectArea[]::new);
    }

    private static RectArea[] sillyPacManMunchingSpritesExtraction(int dir) {
        final RectArea[][] sprites = new RectArea[4][];
        byte margin = 1;
        int size = RASTER - 2 * margin;
        RectArea wide = rect(OFF_X + margin, dir * 16 + margin, size, size);
        RectArea middle = rect(OFF_X + 16 + margin, dir * 16 + margin, size, size);
        RectArea closed = rect(OFF_X + 32 + margin, margin, size, size);
        return rectAreaArray(closed, closed, middle, middle, wide, wide, middle, middle);
    }

    private static RectArea[] sillyPacManDyingSpriteExtraction() {
        var sprites = new RectArea[11];
        for (int i = 0; i < sprites.length; ++i) {
            boolean last = i == sprites.length - 1;
            sprites[i] = rect(504 + i * 16, 0, 15, last ? 16 : 15);
        }
        return sprites;
    }

//        return tilesRightOf(2 * dir, 4 + id, 2);

    private static final RectArea[] GHOST_FRIGHTENED_SPRITES = rectAreaArray(
            rect(OFF_X + RASTER * (8), RASTER * (4), RASTER, RASTER),
            rect(OFF_X + RASTER * (9), RASTER * (4), RASTER, RASTER));

    private static final RectArea[] GHOST_FLASHING_SPRITES = tilesRightOf(8, 4, 4);

    private static final RectArea[][] GHOST_EYES_SPRITES = new RectArea[4][];

    static {
        for (byte dir = 0; dir < 4; ++dir) {
            GHOST_EYES_SPRITES[dir] = rectAreaArray(
                    rect(OFF_X + RASTER * (8 + dir), RASTER * (5), RASTER, RASTER));
        }
    }

    private static final RectArea[] BIG_PAC_MAN_SPRITES = rectAreaArray(
            rect(OFF_X + 32, 16, 32, 32), rect(OFF_X + 64, 16, 32, 32), rect(OFF_X + 96, 16, 32, 32));

    private static final RectArea[] BLINKY_STRETCHED_SPRITES = new RectArea[5];

    static {
        for (int i = 0; i < 5; ++i) {
            BLINKY_STRETCHED_SPRITES[i] = rect(OFF_X + RASTER * (8 + i), RASTER * (6), RASTER, RASTER);
        }
    }

    private static final RectArea[] BLINKY_DAMAGED_SPRITES = new RectArea[2];

    static {
        byte margin = 1;
        int size = RASTER - 2 * margin;
        BLINKY_DAMAGED_SPRITES[0] = rect(OFF_X + RASTER * (8) + margin, RASTER * (7) + margin, size, size);
        BLINKY_DAMAGED_SPRITES[1] = rect(OFF_X + RASTER * (9) + margin, RASTER * (7) + margin, size, size);
    }

    private static final RectArea[] BLINKY_PATCHED_SPRITES = new RectArea[2];

    static {
        BLINKY_PATCHED_SPRITES[0] = rect(OFF_X + RASTER * (10), RASTER * (7), RASTER, RASTER);
        BLINKY_PATCHED_SPRITES[1] = rect(OFF_X + RASTER * (11), RASTER * (7), RASTER, RASTER);
    }

    private static final RectArea[] BLINKY_NAKED_SPRITES = new RectArea[2];

    static {
        BLINKY_NAKED_SPRITES[0] = rect(OFF_X + RASTER * (8), RASTER * (8), 2 * RASTER, RASTER);
        BLINKY_NAKED_SPRITES[1] = rect(OFF_X + RASTER * (10), RASTER * (8), 2 * RASTER, RASTER);
    }

    public ArcadePacMan_SpriteSheet(Image sourceImage) {
        this.sourceImage = requireNonNull(sourceImage);
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return getSprites(GHOST_NUMBERS);
    }

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
    public RectArea[] ghostFrightenedSprites() {
        return GHOST_FRIGHTENED_SPRITES;
    }

    @Override
    public RectArea[] ghostFlashingSprites() {
        return GHOST_FLASHING_SPRITES;
    }

    @Override
    public RectArea[] ghostEyesSprites(Direction dir) {
        return GHOST_EYES_SPRITES[DIR_ORDER.indexOf(dir)];
    }

    public RectArea ghostFacingRight(byte personality) {
        return getSprites(GALLERY_GHOSTS)[personality];
    }

    public RectArea[] bigPacManSprites() {
        return BIG_PAC_MAN_SPRITES;
    }

    public RectArea[] blinkyStretchedSprites() {
        return BLINKY_STRETCHED_SPRITES;
    }

    public RectArea[] blinkyDamagedSprites() {
        return BLINKY_DAMAGED_SPRITES;
    }

    public RectArea[] blinkyPatchedSprites() {
        return BLINKY_PATCHED_SPRITES;
    }

    public RectArea[] blinkyNakedSprites() {
        return BLINKY_NAKED_SPRITES;
    }
}