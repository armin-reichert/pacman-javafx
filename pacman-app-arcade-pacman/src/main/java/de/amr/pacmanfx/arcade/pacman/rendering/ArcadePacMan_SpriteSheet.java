/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.pacmanfx.lib.math.RectShort.rect;

public class ArcadePacMan_SpriteSheet implements SpriteSheet<SpriteID> {

    /** Sprite sheet has a 16x16 raster. */
    public static final int SQUARE_SIZE = 16;

    /** Left of this x, there are the maze images, sprites start right from here. */
    public static final int SPRITES_START_X = 456;

    private static RectShort clipSpriteRect(int x, int y, int w, int h) {
        return rect(SPRITES_START_X + x, y, w, h);
    }

    private static RectShort clipSpriteTile(int tileX, int tileY) {
        return clipSpriteRect(SQUARE_SIZE * tileX, SQUARE_SIZE * tileY, SQUARE_SIZE, SQUARE_SIZE);
    }

    private static RectShort[] clipSpriteTiles(int startTileX, int startTileY, int count) {
        return IntStream.range(startTileX, startTileX + count)
            .mapToObj(tileX -> clipSpriteTile(tileX, startTileY))
            .toArray(RectShort[]::new);
    }

    private static RectShort pacFullSprite() {
        int margin = 1;
        int size = 14; // SQUARE_SIZE - 2 * margin
        return clipSpriteRect(2 * SQUARE_SIZE + margin, margin, size, size);
    }

    private static RectShort[] makePacManMunchingSpriteSeq(int dir) {
        int margin = 1;
        int size = 14; // SQUARE_SIZE - 2 * margin
        // Note: the close mouth sprite is only available at row 0
        RectShort closed = clipSpriteRect(2 * SQUARE_SIZE + margin, margin, size, size);
        RectShort wide   = clipSpriteRect(margin, dir * SQUARE_SIZE + margin, size, size);
        RectShort middle = clipSpriteRect(SQUARE_SIZE + margin, dir * SQUARE_SIZE + margin, size, size);
        return new RectShort[] {closed, closed, middle, middle, wide, wide, middle, middle};
    }

    private static RectShort[] makePacManDyingSpriteSeq() {
        return IntStream.range(0, 11).mapToObj(i -> rect(504 + i * 16, 0, 16, 16)).toArray(RectShort[]::new);
    }

    private static final SpriteMap<SpriteID> SPRITE_MAP = new SpriteMap<>(SpriteID.class);

    static {
        // -- Map images
        SPRITE_MAP.add(SpriteID.MAP_FULL, rect(0, 0, 224, 248));
        SPRITE_MAP.add(SpriteID.MAP_EMPTY, rect(228, 0, 224, 248));

        // -- Eaten ghost values
        SPRITE_MAP.add(SpriteID.GHOST_NUMBERS,
            rect(456, 133, 15, 7),  // 200
            rect(472, 133, 15, 7),  // 400
            rect(488, 133, 15, 7),  // 800
            rect(504, 133, 16, 7)   // 1600
        );

        // Energizer
        SPRITE_MAP.add(SpriteID.ENERGIZER, rect(8, 24, 8, 8));

        // -- 8 bonus symbols ("fruits")
        SPRITE_MAP.add(SpriteID.BONUS_SYMBOLS,
            IntStream.range(0, 8)
                .mapToObj(i -> clipSpriteRect(SQUARE_SIZE * (2 + i), 49, 14, 14))
                .toArray(RectShort[]::new));

        // -- Bonus value numbers
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

        SPRITE_MAP.add(SpriteID.LIVES_COUNTER_SYMBOL, clipSpriteRect(129, 15, 16, 16));

        // -- Pac-Man sprites

        SPRITE_MAP.add(SpriteID.PACMAN_FULL, pacFullSprite());
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_RIGHT, makePacManMunchingSpriteSeq(0));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_LEFT, makePacManMunchingSpriteSeq(1));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_UP, makePacManMunchingSpriteSeq(2));
        SPRITE_MAP.add(SpriteID.PACMAN_MUNCHING_DOWN, makePacManMunchingSpriteSeq(3));

        SPRITE_MAP.add(SpriteID.PACMAN_DYING, makePacManDyingSpriteSeq());

        SPRITE_MAP.add(SpriteID.PACMAN_BIG,
            clipSpriteRect(32, 16, 32, 32),
            clipSpriteRect(64, 16, 32, 32),
            clipSpriteRect(96, 16, 32, 32)
        );

        // -- Ghost sprites

        SPRITE_MAP.add(SpriteID.RED_GHOST_RIGHT, clipSpriteTiles(0, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_LEFT, clipSpriteTiles(2, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_UP, clipSpriteTiles(4, 4, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_DOWN, clipSpriteTiles(6, 4, 2));

        SPRITE_MAP.add(SpriteID.PINK_GHOST_RIGHT, clipSpriteTiles(0, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_LEFT, clipSpriteTiles(2, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_UP, clipSpriteTiles(4, 5, 2));
        SPRITE_MAP.add(SpriteID.PINK_GHOST_DOWN, clipSpriteTiles(6, 5, 2));

        SPRITE_MAP.add(SpriteID.CYAN_GHOST_RIGHT, clipSpriteTiles(0, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_LEFT, clipSpriteTiles(2, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_UP, clipSpriteTiles(4, 6, 2));
        SPRITE_MAP.add(SpriteID.CYAN_GHOST_DOWN, clipSpriteTiles(6, 6, 2));

        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_RIGHT, clipSpriteTiles(0, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_LEFT, clipSpriteTiles(2, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_UP, clipSpriteTiles(4, 7, 2));
        SPRITE_MAP.add(SpriteID.ORANGE_GHOST_DOWN, clipSpriteTiles(6, 7, 2));

        SPRITE_MAP.add(SpriteID.GHOST_FRIGHTENED, clipSpriteTiles(8, 4, 2));
        SPRITE_MAP.add(SpriteID.GHOST_FLASHING, clipSpriteTiles(8, 4, 4));

        SPRITE_MAP.add(SpriteID.GHOST_EYES_RIGHT, clipSpriteTiles(8, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_LEFT, clipSpriteTiles(9, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_UP, clipSpriteTiles(10, 5, 1));
        SPRITE_MAP.add(SpriteID.GHOST_EYES_DOWN, clipSpriteTiles(11, 5, 1));

        // -- Intro scene ghost sprites
        SPRITE_MAP.add(SpriteID.GALLERY_GHOSTS, clipSpriteTile(0, 4), clipSpriteTile(0, 5), clipSpriteTile(0, 6), clipSpriteTile(0, 7));

        // -- Cut scenes sprites
        SPRITE_MAP.add(SpriteID.RED_GHOST_STRETCHED, clipSpriteTiles(8, 6, 5));
        SPRITE_MAP.add(SpriteID.RED_GHOST_DAMAGED,
            clipSpriteRect(SQUARE_SIZE * 8 + 1, SQUARE_SIZE * 7 + 1, 14, 14),
            clipSpriteRect(SQUARE_SIZE * 9 + 1, SQUARE_SIZE * 7 + 1, 14, 14)
        );
        SPRITE_MAP.add(SpriteID.RED_GHOST_PATCHED, clipSpriteTiles(10, 7, 2));
        SPRITE_MAP.add(SpriteID.RED_GHOST_NAKED,
            clipSpriteRect(SQUARE_SIZE * 8, SQUARE_SIZE * 8, SQUARE_SIZE * 2, SQUARE_SIZE),
            clipSpriteRect(SQUARE_SIZE * 10, SQUARE_SIZE * 8, SQUARE_SIZE * 2, SQUARE_SIZE)
        );

        SPRITE_MAP.checkCompleteness();
    }

    private static final ResourceManager LOCAL_RESOURCES = () -> ArcadePacMan_UIConfig.class;

    private Image sourceImage;

    public ArcadePacMan_SpriteSheet() {
        this.sourceImage = LOCAL_RESOURCES.loadImage("graphics/pacman_spritesheet.png");
    }

    @Override
    public Image sourceImage() {
        return sourceImage;
    }

    @Override
    public void dispose() {
        sourceImage = null;
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