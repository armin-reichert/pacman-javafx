/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.lib.math.RectShort.rect;

public final class ArcadeMsPacMan_SpriteSheet implements SpriteSheet<SpriteID> {

    private static final String SPRITESHEET_PNG = "graphics/mspacman_spritesheet.png";

    // Map images are located left and sprites right of this x position
    private static final int HORIZONTAL_SPLIT_X = 456;

    public static final ArcadeMsPacMan_SpriteSheet INSTANCE = new ArcadeMsPacMan_SpriteSheet();

    private final SpriteMap<SpriteID> spriteMap = new SpriteMap<>(SpriteID.class);
    private final Image image;

    private ArcadeMsPacMan_SpriteSheet() {
        final ResourceManager moduleResources = () -> ArcadeMsPacMan_UIConfig.class;
        image = moduleResources.loadImage(SPRITESHEET_PNG);

        spriteMap.add(FULL_MAPS,
            rect(0, 0, 224, 248),
            rect(0, 248, 224, 248),
            rect(0, 2 * 248, 224, 248),
            rect(0, 3 * 248, 224, 248),
            rect(0, 4 * 248, 224, 248),
            rect(0, 5 * 248, 224, 248)
        );
        spriteMap.add(EMPTY_MAPS,
            rect(228, 0, 224, 248),
            rect(228, 248, 224, 248),
            rect(228, 2 * 248, 224, 248),
            rect(228, 3 * 248, 224, 248),
            rect(228, 4 * 248, 224, 248),
            rect(228, 5 * 248, 224, 248)
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
        spriteMap.add(STORK, rect(489, 176, 32, 16), rect(521, 176, 32, 16));
        spriteMap.add(CLAPPERBOARD,
            rect(456, 208, 32, 32),  // open
            rect(488, 208, 32, 32),  // middle
            rect(520, 208, 32, 32)   // closed
        );
        spriteMap.add(HEART, tile(2, 10));
        spriteMap.add(BLUE_BAG, rect(488, 199, 8, 8));
        spriteMap.add(JUNIOR_PAC, rect(509, 200, 8, 8));

        spriteMap.checkCompleteness();
    }

    @Override
    public Image sourceImage() {
        return image;
    }

    @Override
    public RectShort sprite(SpriteID id) {
        return spriteMap.sprite(id);
    }

    @Override
    public RectShort[] sprites(SpriteID id) {
        return spriteMap.spriteSequence(id);
    }

    // private methods

    private RectShort tile(int tileX, int tileY) {
        return rect(HORIZONTAL_SPLIT_X + 16 * tileX, 16 * tileY, 16, 16);
    }

    private RectShort[] tilesRightOf(int tileX, int tileY, int numTiles) {
        if (numTiles <= 0) {
            throw new IllegalArgumentException("Number of tiles must be positive but is " + numTiles);
        }
        return IntStream.range(tileX, tileX + numTiles)
            .mapToObj(x -> rect(HORIZONTAL_SPLIT_X + 16 * x, 16 * tileY, 16, 16))
            .toArray(RectShort[]::new);
    }

    private RectShort fullMsPacMan() {
        return tile(2, 1);
    }

    private RectShort[] makeMsPacManMunchingSpriteSeq(int dir) {
        RectShort wide = tile(0, dir), open = tile(1, dir), closed = tile(2, dir);
        return new RectShort[] {open, open, wide, wide, open, open, open, closed, closed};
    }

    private RectShort[] makeMsPacManDyingSpriteSeq() {
        RectShort right = tile(1, 0), left = tile(1, 1), up = tile(1, 2), down = tile(1, 3);
        // TODO: this is not yet 100% correct
        return new RectShort[] {down, left, up, right, down, left, up, right, down, left, up};
    }
}