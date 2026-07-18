/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameVariantConfig;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.basics.math.RectShort.sprite;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;

public final class ArcadeMsPacMan_SpriteSheet implements SpriteSheet<SpriteID> {

    private static class LazyThreadSafeSingletonHolder {
        static final ArcadeMsPacMan_SpriteSheet SINGLETON = new ArcadeMsPacMan_SpriteSheet();
    }

    public static ArcadeMsPacMan_SpriteSheet instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    private static final String SPRITESHEET_PNG = "graphics/mspacman_spritesheet.png";

    // Map images are located left and sprites right of this x position
    private static final int SPRITE_START_X = 456;

    private final SpriteMap<SpriteID> spriteMap = new SpriteMap<>(SpriteID.class);
    private final Image image;

    private ArcadeMsPacMan_SpriteSheet() {
        final ResourceManager moduleResources = () -> ArcadeMsPacMan_GameVariantConfig.class;
        image = moduleResources.loadImage(SPRITESHEET_PNG);

        spriteMap.add(FULL_MAPS,
            sprite(0, 0, 224, 248),
            sprite(0, 248, 224, 248),
            sprite(0, 2 * 248, 224, 248),
            sprite(0, 3 * 248, 224, 248),
            sprite(0, 4 * 248, 224, 248),
            sprite(0, 5 * 248, 224, 248)
        );
        spriteMap.add(EMPTY_MAPS,
            sprite(228, 0, 224, 248),
            sprite(228, 248, 224, 248),
            sprite(228, 2 * 248, 224, 248),
            sprite(228, 3 * 248, 224, 248),
            sprite(228, 4 * 248, 224, 248),
            sprite(228, 5 * 248, 224, 248)
        );
        spriteMap.add(MS_PACMAN_FULL, fullMsPacMan());
        spriteMap.add(MS_PACMAN_MUNCHING_RIGHT, makeMsPacManMunchingSpriteSeq(0));
        spriteMap.add(MS_PACMAN_MUNCHING_LEFT, makeMsPacManMunchingSpriteSeq(1));
        spriteMap.add(MS_PACMAN_MUNCHING_UP, makeMsPacManMunchingSpriteSeq(2));
        spriteMap.add(MS_PACMAN_MUNCHING_DOWN, makeMsPacManMunchingSpriteSeq(3));
        spriteMap.add(MS_PACMAN_DYING, makeMsPacManDyingSpriteSeq());
        spriteMap.add(MR_PACMAN_MUNCHING_RIGHT, tilesRightOf(0, 9, 3));
        spriteMap.add(MR_PACMAN_MUNCHING_LEFT, tile(0, 10), tile(1, 10), tile(2, 9));
        spriteMap.add(MR_PACMAN_MUNCHING_UP, tile(0, 11), tile(1, 11), tile(2, 9));
        spriteMap.add(MR_PACMAN_MUNCHING_DOWN, tile(0, 12), tile(1, 12), tile(2, 9));
        spriteMap.add(RED_GHOST_RIGHT, tilesRightOf(0, 4, 2));
        spriteMap.add(RED_GHOST_LEFT, tilesRightOf(2, 4, 2));
        spriteMap.add(RED_GHOST_UP, tilesRightOf(4, 4, 2));
        spriteMap.add(RED_GHOST_DOWN, tilesRightOf(6, 4, 2));
        spriteMap.add(PINK_GHOST_RIGHT, tilesRightOf(0, 5, 2));
        spriteMap.add(PINK_GHOST_LEFT, tilesRightOf(2, 5, 2));
        spriteMap.add(PINK_GHOST_UP, tilesRightOf(4, 5, 2));
        spriteMap.add(PINK_GHOST_DOWN, tilesRightOf(6, 5, 2));
        spriteMap.add(CYAN_GHOST_RIGHT, tilesRightOf(0, 6, 2));
        spriteMap.add(CYAN_GHOST_LEFT, tilesRightOf(2, 6, 2));
        spriteMap.add(CYAN_GHOST_UP, tilesRightOf(4, 6, 2));
        spriteMap.add(CYAN_GHOST_DOWN, tilesRightOf(6, 6, 2));
        spriteMap.add(ORANGE_GHOST_RIGHT, tilesRightOf(0, 7, 2));
        spriteMap.add(ORANGE_GHOST_LEFT, tilesRightOf(2, 7, 2));
        spriteMap.add(ORANGE_GHOST_UP, tilesRightOf(4, 7, 2));
        spriteMap.add(ORANGE_GHOST_DOWN, tilesRightOf(6, 7, 2));
        spriteMap.add(GHOST_FRIGHTENED, tilesRightOf(8, 4, 2));
        spriteMap.add(GHOST_FLASHING, tilesRightOf(8, 4, 4));
        spriteMap.add(GHOST_EYES_RIGHT, tilesRightOf(8, 5, 1));
        spriteMap.add(GHOST_EYES_LEFT, tilesRightOf(9, 5, 1));
        spriteMap.add(GHOST_EYES_UP, tilesRightOf(10, 5, 1));
        spriteMap.add(GHOST_EYES_DOWN, tilesRightOf(11, 5, 1));
        spriteMap.add(GHOST_NUMBERS, tilesRightOf(0, 8, 4));
        spriteMap.add(BONUS_SYMBOLS, tilesRightOf(3, 0, 7));
        spriteMap.add(BONUS_VALUES, tilesRightOf(3, 1, 7));
        spriteMap.add(LIVES_COUNTER_SYMBOL, tile(1, 0));
        spriteMap.add(STORK, sprite(489, 176, 32, 16), sprite(521, 176, 32, 16));
        spriteMap.add(CLAPPERBOARD,
            sprite(456, 208, 32, 32),  // open
            sprite(488, 208, 32, 32),  // middle
            sprite(520, 208, 32, 32)   // closed
        );
        spriteMap.add(HEART, tile(2, 10));
        spriteMap.add(BLUE_BAG, sprite(488, 199, 8, 8));
        spriteMap.add(JUNIOR_PAC, sprite(509, 200, 8, 8));

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

    public RectShort[] msPacManMunchingSprites(Direction dir) {
        return switch (dir) {
            case RIGHT -> findSprites(SpriteID.MS_PACMAN_MUNCHING_RIGHT);
            case LEFT  -> findSprites(SpriteID.MS_PACMAN_MUNCHING_LEFT);
            case UP    -> findSprites(SpriteID.MS_PACMAN_MUNCHING_UP);
            case DOWN  -> findSprites(SpriteID.MS_PACMAN_MUNCHING_DOWN);
        };
    }

    public RectShort[] mrPacManMunchingSprites(Direction dir) {
        return findSprites(switch (dir) {
            case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
            case LEFT  -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
            case UP    -> SpriteID.MR_PACMAN_MUNCHING_UP;
            case DOWN  -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
        });
    }

    public RectShort[] ghostNormalSprites(byte personality, Direction dir) {
        return findSprites(switch (personality) {
            case GameModel.RED_GHOST_SHADOW -> switch (dir) {
                case RIGHT -> RED_GHOST_RIGHT;
                case LEFT -> RED_GHOST_LEFT;
                case UP -> RED_GHOST_UP;
                case DOWN -> RED_GHOST_DOWN;
            };
            case GameModel.PINK_GHOST_SPEEDY -> switch (dir) {
                case RIGHT -> PINK_GHOST_RIGHT;
                case LEFT -> PINK_GHOST_LEFT;
                case UP -> PINK_GHOST_UP;
                case DOWN -> PINK_GHOST_DOWN;
            };
            case GameModel.CYAN_GHOST_BASHFUL -> switch (dir) {
                case RIGHT -> CYAN_GHOST_RIGHT;
                case LEFT -> CYAN_GHOST_LEFT;
                case UP -> CYAN_GHOST_UP;
                case DOWN -> CYAN_GHOST_DOWN;
            };
            case GameModel.ORANGE_GHOST_POKEY -> switch (dir) {
                case RIGHT -> ORANGE_GHOST_RIGHT;
                case LEFT -> ORANGE_GHOST_LEFT;
                case UP -> ORANGE_GHOST_UP;
                case DOWN -> ORANGE_GHOST_DOWN;
            };
            default -> throw new IllegalArgumentException();
        });
    }

    public RectShort ghostEyesSprite(Direction dir) {
        return findSprite(switch (dir) {
            case RIGHT -> GHOST_EYES_RIGHT;
            case LEFT  -> GHOST_EYES_LEFT;
            case UP    -> GHOST_EYES_UP;
            case DOWN  -> GHOST_EYES_DOWN;
        });
    }

    // private methods

    private RectShort tile(int tileX, int tileY) {
        return sprite(SPRITE_START_X + 16 * tileX, 16 * tileY, 16, 16);
    }

    private RectShort[] tilesRightOf(int tileX, int tileY, int numTiles) {
        if (numTiles <= 0) {
            throw new IllegalArgumentException("Number of tiles must be positive but is " + numTiles);
        }
        return IntStream.range(tileX, tileX + numTiles)
            .mapToObj(tx -> tile(tx, tileY))
            .toArray(RectShort[]::new);
    }

    private RectShort fullMsPacMan() {
        return tile(2, 1);
    }

    private RectShort[] makeMsPacManMunchingSpriteSeq(int dir) {
        final RectShort wide = tile(0, dir), open = tile(1, dir), closed = tile(2, dir);
        return new RectShort[] {open, open, wide, wide, open, open, open, closed, closed};
    }

    private RectShort[] makeMsPacManDyingSpriteSeq() {
        final RectShort right = tile(1, 0), left = tile(1, 1), up = tile(1, 2), down = tile(1, 3);
        // TODO: this is not yet 100% correct
        return new RectShort[] {down, left, up, right, down, left, up, right, down, left, up};
    }
}