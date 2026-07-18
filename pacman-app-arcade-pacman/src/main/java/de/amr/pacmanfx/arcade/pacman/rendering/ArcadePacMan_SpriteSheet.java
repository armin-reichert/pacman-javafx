/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.basics.math.RectShort.sprite;

public final class ArcadePacMan_SpriteSheet implements SpriteSheet<SpriteID> {

    private static class LazyThreadSafeSingletonHolder {
        static final ArcadePacMan_SpriteSheet SINGLETON = new ArcadePacMan_SpriteSheet();
    }

    public static ArcadePacMan_SpriteSheet instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static final String SPRITESHEET_PNG = "graphics/pacman_spritesheet.png";

    private static final int RASTER_SIZE = 16;

    // Map images are located left and sprites right of this x position
    private static final int HORIZONTAL_SPLIT_X = 456;

    private final SpriteMap<SpriteID> spriteMap = new SpriteMap<>(SpriteID.class);
    private final Image image;

    private ArcadePacMan_SpriteSheet() {
        final ResourceManager moduleResources = () -> ArcadePacMan_GameVariantConfig.class;
        image = moduleResources.loadImage(SPRITESHEET_PNG);

        // -- Map images
        spriteMap.add(SpriteID.MAP_FULL, sprite(0, 0, 224, 248));
        spriteMap.add(SpriteID.MAP_EMPTY, sprite(228, 0, 224, 248));

        // -- Eaten ghost values
        spriteMap.add(SpriteID.GHOST_NUMBERS,
            sprite(456, 133, 15, 7),  // 200
            sprite(472, 133, 15, 7),  // 400
            sprite(488, 133, 15, 7),  // 800
            sprite(504, 133, 16, 7)   // 1600
        );

        // Energizer
        spriteMap.add(SpriteID.ENERGIZER, sprite(8, 24, 8, 8));

        // -- 8 bonus symbols ("fruits")
        spriteMap.add(SpriteID.BONUS_SYMBOLS,
            IntStream.range(0, 8)
                .mapToObj(i -> clipSprite(RASTER_SIZE * (2 + i), 49, 14, 14))
                .toArray(RectShort[]::new));

        // -- Bonus value numbers
        spriteMap.add(SpriteID.BONUS_VALUES,
            sprite(457, 148, 14, 7), //  100
            sprite(472, 148, 15, 7), //  300
            sprite(488, 148, 15, 7), //  500
            sprite(504, 148, 15, 7), //  700
            sprite(520, 148, 18, 7), // 1000
            sprite(518, 164, 20, 7), // 2000
            sprite(518, 180, 20, 7), // 3000
            sprite(518, 196, 20, 7)  // 5000
        );

        spriteMap.add(SpriteID.LIVES_COUNTER_SYMBOL, clipSprite(129, 15, 16, 16));

        // -- Pac-Man sprites

        spriteMap.add(SpriteID.PACMAN_FULL, pacFullSprite());
        spriteMap.add(SpriteID.PACMAN_MUNCHING_RIGHT, makePacManMunchingSpriteSeq(0));
        spriteMap.add(SpriteID.PACMAN_MUNCHING_LEFT, makePacManMunchingSpriteSeq(1));
        spriteMap.add(SpriteID.PACMAN_MUNCHING_UP, makePacManMunchingSpriteSeq(2));
        spriteMap.add(SpriteID.PACMAN_MUNCHING_DOWN, makePacManMunchingSpriteSeq(3));

        spriteMap.add(SpriteID.PACMAN_DYING, makePacManDyingSpriteSeq());

        spriteMap.add(SpriteID.PACMAN_BIG,
            sprite(488, 16, 32, 32),
            sprite(520, 16, 32, 32),
            sprite(552, 16, 33, 32)
        );

        // -- Ghost sprites

        spriteMap.add(SpriteID.RED_GHOST_RIGHT, clipSpriteTiles(0, 4, 2));
        spriteMap.add(SpriteID.RED_GHOST_LEFT, clipSpriteTiles(2, 4, 2));
        spriteMap.add(SpriteID.RED_GHOST_UP, clipSpriteTiles(4, 4, 2));
        spriteMap.add(SpriteID.RED_GHOST_DOWN, clipSpriteTiles(6, 4, 2));

        spriteMap.add(SpriteID.PINK_GHOST_RIGHT, clipSpriteTiles(0, 5, 2));
        spriteMap.add(SpriteID.PINK_GHOST_LEFT, clipSpriteTiles(2, 5, 2));
        spriteMap.add(SpriteID.PINK_GHOST_UP, clipSpriteTiles(4, 5, 2));
        spriteMap.add(SpriteID.PINK_GHOST_DOWN, clipSpriteTiles(6, 5, 2));

        spriteMap.add(SpriteID.CYAN_GHOST_RIGHT, clipSpriteTiles(0, 6, 2));
        spriteMap.add(SpriteID.CYAN_GHOST_LEFT, clipSpriteTiles(2, 6, 2));
        spriteMap.add(SpriteID.CYAN_GHOST_UP, clipSpriteTiles(4, 6, 2));
        spriteMap.add(SpriteID.CYAN_GHOST_DOWN, clipSpriteTiles(6, 6, 2));

        spriteMap.add(SpriteID.ORANGE_GHOST_RIGHT, clipSpriteTiles(0, 7, 2));
        spriteMap.add(SpriteID.ORANGE_GHOST_LEFT, clipSpriteTiles(2, 7, 2));
        spriteMap.add(SpriteID.ORANGE_GHOST_UP, clipSpriteTiles(4, 7, 2));
        spriteMap.add(SpriteID.ORANGE_GHOST_DOWN, clipSpriteTiles(6, 7, 2));

        spriteMap.add(SpriteID.GHOST_FRIGHTENED, clipSpriteTiles(8, 4, 2));
        spriteMap.add(SpriteID.GHOST_FLASHING, clipSpriteTiles(8, 4, 4));

        spriteMap.add(SpriteID.GHOST_EYES_RIGHT, clipSpriteTiles(8, 5, 1));
        spriteMap.add(SpriteID.GHOST_EYES_LEFT, clipSpriteTiles(9, 5, 1));
        spriteMap.add(SpriteID.GHOST_EYES_UP, clipSpriteTiles(10, 5, 1));
        spriteMap.add(SpriteID.GHOST_EYES_DOWN, clipSpriteTiles(11, 5, 1));

        // -- Intro scene ghost sprites
        spriteMap.add(SpriteID.GALLERY_GHOSTS, clipSpriteTile(0, 4), clipSpriteTile(0, 5), clipSpriteTile(0, 6), clipSpriteTile(0, 7));

        // -- Cut scenes sprites
        spriteMap.add(SpriteID.RED_GHOST_STRETCHED, clipSpriteTiles(8, 6, 5));

        spriteMap.add(SpriteID.RED_GHOST_DAMAGED, sprite(585, 113, 14, 14), sprite(601, 113, 14, 14));
        spriteMap.add(SpriteID.RED_GHOST_PATCHED, sprite(617, 113, 14, 14), sprite(633, 113, 14, 14));

        spriteMap.add(SpriteID.RED_GHOST_NAKED,
            clipSprite(RASTER_SIZE * 8, RASTER_SIZE * 8, RASTER_SIZE * 2, RASTER_SIZE),
            clipSprite(RASTER_SIZE * 10, RASTER_SIZE * 8, RASTER_SIZE * 2, RASTER_SIZE)
        );

        spriteMap.checkCompleteness();
    }

    @Override
    public Image sourceImage() {
        return image;
    }

    @Override
    public RectShort findSprite(SpriteID id) {
        return spriteMap.sprite(id);
    }

    @Override
    public RectShort[] findSprites(SpriteID id) {
        return spriteMap.spriteSequence(id);
    }

    // public

    public RectShort[] pacMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> findSprites(SpriteID.PACMAN_MUNCHING_RIGHT);
            case LEFT  -> findSprites(SpriteID.PACMAN_MUNCHING_LEFT);
            case UP    -> findSprites(SpriteID.PACMAN_MUNCHING_UP);
            case DOWN  -> findSprites(SpriteID.PACMAN_MUNCHING_DOWN);
        };
    }

    public RectShort[] ghostNormalSprites(byte personality, Direction dir) {
        return findSprites(switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> switch (dir) {
                case RIGHT -> SpriteID.RED_GHOST_RIGHT;
                case LEFT ->  SpriteID.RED_GHOST_LEFT;
                case UP ->    SpriteID.RED_GHOST_UP;
                case DOWN ->  SpriteID.RED_GHOST_DOWN;
            };
            case GameModel.PINK_GHOST_SPEEDY -> switch (dir) {
                case RIGHT -> SpriteID.PINK_GHOST_RIGHT;
                case LEFT ->  SpriteID.PINK_GHOST_LEFT;
                case UP ->    SpriteID.PINK_GHOST_UP;
                case DOWN ->  SpriteID.PINK_GHOST_DOWN;
            };
            case GameModel.CYAN_GHOST_BASHFUL -> switch (dir) {
                case RIGHT -> SpriteID.CYAN_GHOST_RIGHT;
                case LEFT ->  SpriteID.CYAN_GHOST_LEFT;
                case UP ->    SpriteID.CYAN_GHOST_UP;
                case DOWN ->  SpriteID.CYAN_GHOST_DOWN;
            };
            case GameModel.ORANGE_GHOST_POKEY -> switch (dir) {
                case RIGHT -> SpriteID.ORANGE_GHOST_RIGHT;
                case LEFT ->  SpriteID.ORANGE_GHOST_LEFT;
                case UP ->    SpriteID.ORANGE_GHOST_UP;
                case DOWN ->  SpriteID.ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException();
        });
    }

    public RectShort ghostEyesSprite(Direction dir) {
        return switch (dir) {
            case RIGHT -> findSprite(SpriteID.GHOST_EYES_RIGHT);
            case LEFT  -> findSprite(SpriteID.GHOST_EYES_LEFT);
            case UP    -> findSprite(SpriteID.GHOST_EYES_UP);
            case DOWN  -> findSprite(SpriteID.GHOST_EYES_DOWN);
        };
    }

    // private methods

    private RectShort clipSprite(int x, int y, int w, int h) {
        return sprite(HORIZONTAL_SPLIT_X + x, y, w, h);
    }

    private RectShort clipSpriteTile(int tileX, int tileY) {
        return clipSprite(RASTER_SIZE * tileX, RASTER_SIZE * tileY, RASTER_SIZE, RASTER_SIZE);
    }

    private RectShort[] clipSpriteTiles(int startTileX, int startTileY, int count) {
        return IntStream.range(startTileX, startTileX + count)
            .mapToObj(tileX -> clipSpriteTile(tileX, startTileY))
            .toArray(RectShort[]::new);
    }

    private RectShort pacFullSprite() {
        final int margin = 1;
        final int size = 14; // SQUARE_SIZE - 2 * margin
        return clipSprite(2 * RASTER_SIZE + margin, margin, size, size);
    }

    private RectShort[] makePacManMunchingSpriteSeq(int dir) {
        final int margin = 1;
        final int size = 14; // SQUARE_SIZE - 2 * margin
        // Note: the close mouth sprite is only available at row 0
        final RectShort closed = clipSprite(2 * RASTER_SIZE + margin, margin, size, size);
        final RectShort wide   = clipSprite(margin, dir * RASTER_SIZE + margin, size, size);
        final RectShort middle = clipSprite(RASTER_SIZE + margin, dir * RASTER_SIZE + margin, size, size);
        return new RectShort[] {closed, closed, middle, middle, wide, wide, middle, middle};
    }

    private RectShort[] makePacManDyingSpriteSeq() {
        return IntStream.range(0, 11).mapToObj(i -> sprite(504 + i * 16, 1, 15, i == 10 ? 15 : 14)).toArray(RectShort[]::new);
    }
}