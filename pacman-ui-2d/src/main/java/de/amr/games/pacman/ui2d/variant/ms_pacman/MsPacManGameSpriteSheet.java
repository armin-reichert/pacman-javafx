/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.variant.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.games.pacman.ui2d.rendering.RectArea.rect;
import static de.amr.games.pacman.ui2d.rendering.GameSpriteSheet.rectArray;

/**
 * @author Armin Reichert
 */
public class MsPacManGameSpriteSheet implements GameSpriteSheet {

    private static final byte RASTER_SIZE = 16;
    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    private static final RectArea[] GHOST_NUMBER_SPRITES = rectArray(
        spriteAt(0, 8), spriteAt(1, 8), spriteAt(2, 8), spriteAt(3, 8));

    private static final RectArea[] BONUS_SYMBOL_SPRITES = IntStream.range(0, 8)
        .mapToObj(symbol -> spriteAt(3 + symbol, 0))
        .toArray(RectArea[]::new);

    private static final RectArea[] BONUS_VALUE_SPRITES = IntStream.range(0, 8)
        .mapToObj(symbol -> spriteAt(3 + symbol, 1))
        .toArray(RectArea[]::new);

    private static final RectArea LIVES_COUNTER_SPRITE = spriteAt(1, 0);

    private static final RectArea[][] MUNCHING_SPRITES = new RectArea[4][];
    static {
        for (byte d = 0; d < 4; ++d) {
            var wide = spriteAt(0, d);
            var open = spriteAt(1, d);
            var closed = spriteAt(2, d);
            MUNCHING_SPRITES[d] = rectArray(open, open, wide, wide, open, open, open, closed, closed);
        }
    }

    private static final RectArea[] MS_PAC_MAN_DYING_SPRITES;
    static {
        var right = spriteAt(1, 0);
        var left = spriteAt(1, 1);
        var up = spriteAt(1, 2);
        var down = spriteAt(1, 3);
        // TODO: this is not yet 100% correct
        MS_PAC_MAN_DYING_SPRITES = rectArray(down, left, up, right, down, left, up, right, down, left, up);
    }

    private static final RectArea[][][] GHOSTS_NORMAL_SPRITES = new RectArea[4][4][];
    static {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                GHOSTS_NORMAL_SPRITES[id][d] = rectArray(spriteAt(2 * d, 4 + id), spriteAt(2 * d + 1, 4 + id));
            }
        }
    }

    private static final RectArea[] GHOST_FRIGHTENED_SPRITES = rectArray(spriteAt(8, 4), spriteAt(9, 4));

    private static final RectArea[] GHOST_FLASHING_SPRITES = rectArray(
            spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));

    private static final RectArea[][] GHOST_EYES_SPRITES = new RectArea[4][];
    static {
        for (byte d = 0; d < 4; ++d) {
            GHOST_EYES_SPRITES[d] = rectArray(spriteAt(8 + d, 5));
        }
    }

    private static final RectArea[][] PAC_MAN_MUNCHING_SPRITES = new RectArea[4][];
    static {
        for (byte d = 0; d < 4; ++d) {
            PAC_MAN_MUNCHING_SPRITES[d] = rectArray(spriteAt(0, 9 + d), spriteAt(1, 9 + d), spriteAt(2, 9));
        }
    }

    private static final RectArea HEART_SPRITE = spriteAt(2, 10);
    private static final RectArea BLUE_BAG_SPRITE = rect(488, 199, 8, 8);
    private static final RectArea JUNIOR_PAC_SPRITE = rect(509, 200, 8, 8);

    private static final RectArea[] CLAPPERBOARD_SPRITES = rectArray(
        rect(456, 208, 32, 32),  // open
        rect(488, 208, 32, 32),  // middle
        rect(520, 208, 32, 32)); // closed

    // third "column" contains the sprites (first two columns the maze images)
    private static RectArea spriteAt(int tileX, int tileY) {
        return rect(456 + RASTER_SIZE * tileX, RASTER_SIZE * tileY, RASTER_SIZE, RASTER_SIZE);
    }

    private final Image source;

    public MsPacManGameSpriteSheet(Image source) {
        this.source = source;
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return GHOST_NUMBER_SPRITES;
    }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) {
        return BONUS_SYMBOL_SPRITES[symbol];
    }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        return BONUS_VALUE_SPRITES[symbol];
    }

    @Override
    public RectArea livesCounterSprite() {
        return LIVES_COUNTER_SPRITE;
    }

    @Override
    public RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return GHOSTS_NORMAL_SPRITES[id][ORDER.indexOf(dir)];
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
        return GHOST_EYES_SPRITES[ORDER.indexOf(dir)];
    }

    @Override
    public RectArea[] pacManMunchingSprites(Direction dir) {
        return PAC_MAN_MUNCHING_SPRITES[ORDER.indexOf(dir)];
    }

    @Override
    public RectArea[] pacMunchingSprites(Direction dir) {
        return MUNCHING_SPRITES[ORDER.indexOf(dir)];
    }

    @Override
    public RectArea[] pacDyingSprites() {
        return MS_PAC_MAN_DYING_SPRITES;
    }

    @Override
    public RectArea heartSprite() {
        return HEART_SPRITE;
    }

    @Override
    public RectArea blueBagSprite() {
        return BLUE_BAG_SPRITE;
    }

    @Override
    public RectArea juniorPacSprite() {
        return JUNIOR_PAC_SPRITE;
    }

    @Override
    public RectArea[] clapperboardSprites() {
        return CLAPPERBOARD_SPRITES;
    }

    @Override
    public SpriteAnimation createStorkFlyingAnimation() {
        return SpriteAnimation
            .use(this)
            .sprites(rect(489, 176, 32, 16), rect(521, 176, 32, 16))
            .frameTicks(8)
            .loop();
    }
}