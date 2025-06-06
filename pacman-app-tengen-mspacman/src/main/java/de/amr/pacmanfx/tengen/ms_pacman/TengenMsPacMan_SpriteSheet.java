/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import javafx.scene.image.Image;

import java.util.Arrays;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.RectArea.rect;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.createSpriteAnimation;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    public static final RectArea INFO_FRAME_SPRITE = rect(175, 125, 126, 7);

    public static final RectArea STRANGE_SPRITE = rect(261, 133, 26, 7);
    public static final RectArea BIG_SPRITE = rect(261, 141, 26, 7);
    public static final RectArea MINI_SPRITE = rect(261, 149, 26, 7);

    public static final RectArea CRAZY_SPRITE = rect(229, 133, 18, 7);
    public static final RectArea HARD_SPRITE = rect(229, 141, 18, 7);
    public static final RectArea EASY_SPRITE = rect(229, 149, 18, 7);

    public static final RectArea[] MS_PAC_MUNCHING_SPRITES_LEFT = {
            rect(51, 15, 15, 15), // open
            rect(66, 15, 15, 15), // wide open
            rect(51, 15, 15, 15), // open
            rect(32, 15, 15, 15), // closed
    };

    public static final RectArea[] MS_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER = {
            rect(105, 15, 15, 15), // open
            rect(120, 15, 15, 15), // wide open
            rect(105, 15, 15, 15), // open
            rect(86, 15, 15, 15), // closed
    };

    public static final RectArea[] MS_PAC_WAVING_HAND = {
            rect(140, 10, 20, 20),
            rect(163, 10, 20, 20),
    };

    public static final RectArea[] MS_PAC_TURNING_AWAY = {
            rect(186, 15, 15, 15),
            rect(205, 20, 8, 8),
            rect(218, 21, 5, 5),
    };

    public static final RectArea[] MR_PAC_MUNCHING_SPRITES_LEFT = {
            rect(51, 41, 15, 15), // open
            rect(66, 41, 15, 15), // wide open
            rect(51, 41, 15, 15), // open
            rect(32, 41, 15, 15), // closed
    };

    public static final RectArea[] MR_PAC_MUNCHING_SPRITES_LEFT_POWER_BOOSTER = {
            rect(105, 41, 15, 15), // open
            rect(120, 41, 15, 15), // wide open
            rect(86, 4, 15, 15), // closed
            rect(120, 41, 15, 15), // wide open
    };

    public static final RectArea[] MR_PAC_WAVING_HAND = {
            rect(140, 36, 20, 20),
            rect(163, 36, 20, 20),
    };

    public static final RectArea[] MR_PAC_TURNING_AWAY = {
            rect(186, 42, 15, 15),
            rect(205, 46, 8, 8),
            rect(218, 47, 5, 5),
    };

    // there is only a sprite pointing left in the sprite sheet, renderer makes the animation
    public static final RectArea[] MS_PAC_ROTATING_SPRITES = new RectArea[11];

    static {
        RectArea pacSprite = rect(51, 15, 15, 15);
        Arrays.fill(MS_PAC_ROTATING_SPRITES, pacSprite);
    }

    // directions: rr ll uu dd
    public static final RectArea[][] RED_GHOST_SPRITES = {
            {rect(10, 120, 14, 13), rect(26, 120, 14, 13)},
            {rect(42, 120, 14, 13), rect(58, 120, 14, 13)},
            {rect(74, 120, 14, 13), rect(90, 120, 14, 13)},
            {rect(106, 120, 14, 13), rect(122, 120, 14, 13)},
    };

    public static final RectArea[][] PINK_GHOST_SPRITES = {
            {rect(10, 135, 14, 13), rect(26, 135, 14, 13)},
            {rect(42, 135, 14, 13), rect(58, 135, 14, 13)},
            {rect(74, 135, 14, 13), rect(90, 135, 14, 13)},
            {rect(106, 135, 14, 13), rect(122, 135, 14, 13)},
    };

    public static final RectArea[][] CYAN_GHOST_SPRITES = {
            {rect(10, 150, 14, 13), rect(26, 150, 14, 13)},
            {rect(42, 150, 14, 13), rect(58, 150, 14, 13)},
            {rect(74, 150, 14, 13), rect(90, 150, 14, 13)},
            {rect(106, 150, 14, 13), rect(122, 150, 14, 13)},
    };

    public static final RectArea[][] ORANGE_GHOST_SPRITES = {
            {rect(10, 165, 14, 13), rect(26, 165, 14, 13)},
            {rect(42, 165, 14, 13), rect(58, 165, 14, 13)},
            {rect(74, 165, 14, 13), rect(90, 165, 14, 13)},
            {rect(106, 165, 14, 13), rect(122, 165, 14, 13)},
    };

    public static final RectArea[] GHOST_FRIGHTENED_SPRITES = {
            rect(138, 120, 14, 13), rect(154, 120, 14, 13)
    };

    public static final RectArea[] GHOST_FLASHING_SPRITES = {
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            rect(138, 120, 14, 13),
            rect(154, 120, 14, 13), // blue
            rect(138, 150, 14, 13),
            rect(154, 150, 14, 13), // white/blue eyes+mouth
    };

    public static final RectArea[] GHOST_EYES_SPRITES = {
            rect(140, 173, 10, 5), // right
            rect(140, 166, 10, 5), // left
            rect(153, 166, 10, 5), // up
            rect(153, 173, 10, 5), // down
    };

    public static final RectArea[] GHOST_NUMBER_SPRITES = {
            rect(259, 172, 16, 10),
            rect(279, 172, 16, 10),
            rect(259, 183, 16, 10),
            rect(279, 183, 16, 10)
    };

    public static final RectArea[] BONUS_SYMBOL_SPRITES = new RectArea[14];

    public static final RectArea[] BONUS_VALUE_SPRITES = new RectArea[14];

    static {
        int[] xs = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
        int[] ws = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
        int[] dy = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < 14; ++i) {
            BONUS_SYMBOL_SPRITES[i] = new RectArea(xs[i], 66 + dy[i], ws[i], 20 - dy[i]);
            BONUS_VALUE_SPRITES[i] = new RectArea(xs[i], 85, ws[i], 18);
        }
    }

    public static final RectArea[] CONTINUES_SPRITES = {
            rect(180, 243, 40, 8), // Continues:0
            rect(180, 234, 40, 8), // Continues:1
            rect(180, 225, 40, 8), // Continues:2
            rect(180, 216, 40, 8), // Continues:3
    };

    public static final RectArea LEVEL_BOX_SPRITE = rect(200, 164, 16, 16);

    public static final RectArea BOOSTER_SPRITE = rect(190, 134, 7, 5);

    public static final RectArea MS_PAC_MAN_TITLE_SPRITE = rect(15, 191, 152, 40);

    public static final RectArea[] CLAPPERBOARD_SPRITES = {
            // wide open, open, closed
            rect(91, 361, 32, 32), rect(53, 361, 32, 32), rect(14, 361, 32, 32),
    };

    public static final RectArea HEART_SPRITE = rect(162, 270, 18, 18);
    public static final RectArea BLUE_BAG_SPRITE = rect(241, 363, 7, 8);
    public static final RectArea JUNIOR_PAC_SPRITE = rect(176, 304, 7, 8);

    @Override
    public RectArea[] pacMunchingSprites(Direction dir) {
        return MS_PAC_MUNCHING_SPRITES_LEFT;
    }

    @Override
    public RectArea[] pacDyingSprites() {
        return MS_PAC_ROTATING_SPRITES;
    }

    @Override
    public RectArea[] ghostEyesSprites(Direction dir) {
        return new RectArea[]{GHOST_EYES_SPRITES[dirIndex(dir)]};
    }

    @Override
    public RectArea[] ghostFlashingSprites() {
        return GHOST_FLASHING_SPRITES;
    }

    @Override
    public RectArea[] ghostFrightenedSprites() {
        return GHOST_FRIGHTENED_SPRITES;
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return GHOST_NUMBER_SPRITES;
    }

    @Override
    public RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return switch (id) {
            case RED_GHOST_SHADOW -> RED_GHOST_SPRITES[dirIndex(dir)];
            case PINK_GHOST_SPEEDY -> PINK_GHOST_SPRITES[dirIndex(dir)];
            case CYAN_GHOST_BASHFUL -> CYAN_GHOST_SPRITES[dirIndex(dir)];
            case ORANGE_GHOST_POKEY -> ORANGE_GHOST_SPRITES[dirIndex(dir)];
            default -> throw new IllegalArgumentException();
        };
    }

    private int dirIndex(Direction dir) {
        return switch (dir) {
            case Direction.RIGHT -> 0;
            case Direction.LEFT -> 1;
            case Direction.UP -> 2;
            case Direction.DOWN -> 3;
        };
    }

    @Override
    public RectArea livesCounterSprite() {
        return rect(241, 15, 16, 16);
    }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) {
        return BONUS_SYMBOL_SPRITES[symbol];
    }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
        int index = switch (symbol) {
            case TengenMsPacMan_GameModel.BONUS_BANANA -> 8;    // 5000!
            case TengenMsPacMan_GameModel.BONUS_MILK -> 6;      // 3000!
            case TengenMsPacMan_GameModel.BONUS_ICE_CREAM -> 7; // 4000!
            default -> symbol;
        };
        return BONUS_VALUE_SPRITES[index];
    }

    public RectArea[] pacManMunchingSprites(Direction dir) {
        return MR_PAC_MUNCHING_SPRITES_LEFT;
    }

    public SpriteAnimation createStorkFlyingAnimation() {
        return createSpriteAnimation().sprites(rect(157, 355, 33, 16), rect(198, 355, 33, 16)).frameTicks(8).endless();
    }

    // Tengen-specific
    public RectArea digit(int d) {
        return d == 0 ? rect(235, 184, 4, 5) : rect(185 + 5 * d, 184, 4, 5);
    }
}