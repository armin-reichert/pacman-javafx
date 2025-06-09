/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.SpriteID.*;
import static de.amr.pacmanfx.lib.RectArea.rect;

public record ArcadeMsPacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    private static final byte R16 = 16;
    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    public enum SpriteID {
        MS_PACMAN_MUNCHING_RIGHT, MS_PACMAN_MUNCHING_LEFT, MS_PACMAN_MUNCHING_UP, MS_PACMAN_MUNCHING_DOWN,
        MS_PACMAN_DYING,
        GHOST_NUMBERS,
        BONUS_SYMBOLS,
        BONUS_VALUES,
        LIVES_COUNTER_SYMBOL,
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_RIGHT, crappyPacManMunchingSpritesExtraction(0));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_LEFT,  crappyPacManMunchingSpritesExtraction(1));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_UP,    crappyPacManMunchingSpritesExtraction(2));
        SPRITE_MAP.put(MS_PACMAN_MUNCHING_DOWN,  crappyPacManMunchingSpritesExtraction(3));
        SPRITE_MAP.put(MS_PACMAN_DYING,          crappyPacManDyingSpriteExtraction());
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            spriteAt(0, 8), spriteAt(1, 8), spriteAt(2, 8), spriteAt(3, 8)
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 8)
                .mapToObj(symbol -> spriteAt(3 + symbol, 0))
                .toArray(RectArea[]::new)
        );
        SPRITE_MAP.put(BONUS_VALUES, IntStream.range(0, 8)
                .mapToObj(symbol -> spriteAt(3 + symbol, 1))
                .toArray(RectArea[]::new)
        );
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, spriteAt(1, 0));
    }

    public static RectArea getSprite(SpriteID spriteID) { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] getSprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    private static RectArea[] crappyPacManMunchingSpritesExtraction(int dir) {
        RectArea wide = spriteAt(0, dir), open = spriteAt(1, dir), closed = spriteAt(2, dir);
        return new RectArea[] {open, open, wide, wide, open, open, open, closed, closed};
    }

    private static RectArea[] crappyPacManDyingSpriteExtraction() {
        RectArea right = spriteAt(1, 0), left = spriteAt(1, 1), up = spriteAt(1, 2), down = spriteAt(1, 3);
        // TODO: this is not yet 100% correct
        return new RectArea[] {down, left, up, right, down, left, up, right, down, left, up};
    }

    private static final RectArea[][][] GHOSTS_NORMAL_SPRITES = new RectArea[4][4][];

    static {
        for (byte id = 0; id < 4; ++id) {
            for (byte dir = 0; dir < 4; ++dir) {
                GHOSTS_NORMAL_SPRITES[id][dir] = SpriteSheet.rectAreaArray(spriteAt(2 * dir, 4 + id), spriteAt(2 * dir + 1, 4 + id));
            }
        }
    }

    private static final RectArea[] GHOST_FRIGHTENED_SPRITES = SpriteSheet.rectAreaArray(spriteAt(8, 4), spriteAt(9, 4));

    private static final RectArea[] GHOST_FLASHING_SPRITES = SpriteSheet.rectAreaArray(
            spriteAt(8, 4), spriteAt(9, 4), spriteAt(10, 4), spriteAt(11, 4));

    private static final RectArea[][] GHOST_EYES_SPRITES = new RectArea[4][];

    static {
        for (byte dir = 0; dir < 4; ++dir) {
            GHOST_EYES_SPRITES[dir] = SpriteSheet.rectAreaArray(spriteAt(8 + dir, 5));
        }
    }

    private static final RectArea[][] MR_PAC_MAN_MUNCHING_SPRITES = new RectArea[4][];

    static {
        for (byte dir = 0; dir < 4; ++dir) {
            MR_PAC_MAN_MUNCHING_SPRITES[dir] = SpriteSheet.rectAreaArray(spriteAt(0, 9 + dir), spriteAt(1, 9 + dir), spriteAt(2, 9));
        }
    }

    static final RectArea HEART_SPRITE = spriteAt(2, 10);
    static final RectArea BLUE_BAG_SPRITE = rect(488, 199, 8, 8);
    static final RectArea JUNIOR_PAC_SPRITE = rect(509, 200, 8, 8);

    static final RectArea[] CLAPPERBOARD_SPRITES = SpriteSheet.rectAreaArray(
            rect(456, 208, 32, 32),  // open
            rect(488, 208, 32, 32),  // middle
            rect(520, 208, 32, 32)); // closed

    // third "column" contains the sprites (first two columns the maze images)
    private static RectArea spriteAt(int tileX, int tileY) {
        return rect(456 + R16 * tileX, R16 * tileY, R16, R16);
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return getSprites(GHOST_NUMBERS);
    }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) {
        return getSprite(BONUS_SYMBOLS);
    }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        return getSprite(BONUS_VALUES);
    }

    @Override
    public RectArea livesCounterSprite() {
        return getSprite(LIVES_COUNTER_SYMBOL);
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
    public RectArea[] pacMunchingSprites(Direction dir) {
        return getSprites(switch (dir) {
            case RIGHT -> MS_PACMAN_MUNCHING_RIGHT;
            case LEFT -> MS_PACMAN_MUNCHING_LEFT;
            case UP -> MS_PACMAN_MUNCHING_UP;
            case DOWN -> MS_PACMAN_MUNCHING_DOWN;
        });
    }

    @Override
    public RectArea[] pacDyingSprites() {
        return getSprites(MS_PACMAN_DYING);
    }

    public RectArea[] mrPacManMunchingSprites(Direction dir) {
        return MR_PAC_MAN_MUNCHING_SPRITES[ORDER.indexOf(dir)];
    }

    public SpriteAnimation createStorkFlyingAnimation() {
        return SpriteAnimation.createAnimation()
                .sprites(new RectArea[] {rect(489, 176, 32, 16), rect(521, 176, 32, 16)})
                .frameTicks(8)
                .endless();
    }
}