/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.lib.RectArea.ra;
import static de.amr.pacmanfx.tengen.ms_pacman.SpriteID.*;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    // Bonus symbols/values: x-position, width, y-delta
    private static final int[] BONUS_X  = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] BONUS_W  = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] BONUS_DY = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(TITLE_TEXT, ra(15, 191, 152, 40));
        SPRITE_MAP.put(INFO_FRAME, ra(175, 125, 126, 7));
        SPRITE_MAP.put(INFO_BOOSTER, ra(190, 134, 7, 5));
        SPRITE_MAP.put(INFO_CATEGORY_BIG, ra(261, 141, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_MINI, ra(261, 149, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_STRANGE, ra(261, 133, 26, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_CRAZY, ra(229, 133, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_EASY, ra(229, 149, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_HARD, ra(229, 141, 18, 7));
        SPRITE_MAP.put(HEART, ra(162, 270, 18, 18));
        SPRITE_MAP.put(BLUE_BAG, ra(241, 363, 7, 8));
        SPRITE_MAP.put(JUNIOR_PAC, ra(176, 304, 7, 8));
        SPRITE_MAP.put(GHOST_EYES_RIGHT, ra(140, 173, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_LEFT, ra(140, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_UP, ra(153, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_DOWN, ra(153, 173, 10, 5));
        SPRITE_MAP.put(CONTINUES_0, ra(180, 243, 40, 8));
        SPRITE_MAP.put(CONTINUES_1, ra(180, 234, 40, 8));
        SPRITE_MAP.put(CONTINUES_2, ra(180, 225, 40, 8));
        SPRITE_MAP.put(CONTINUES_3, ra(180, 216, 40, 8));
        SPRITE_MAP.put(LEVEL_NUMBER_BOX, ra(200, 164, 16, 16));
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, ra(241, 15, 16, 16));
        SPRITE_MAP.put(DIGIT_1, ra(185 + 5,      184, 4, 5));
        SPRITE_MAP.put(DIGIT_2, ra(185 + 5 *  2, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_3, ra(185 + 5 *  3, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_4, ra(185 + 5 *  4, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_5, ra(185 + 5 *  5, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_6, ra(185 + 5 *  6, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_7, ra(185 + 5 *  7, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_8, ra(185 + 5 *  8, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_9, ra(185 + 5 *  9, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_0, ra(185 + 5 * 10, 184, 4, 5));
        SPRITE_MAP.put(MS_PAC_MUNCHING, new RectArea[] {
            ra(51, 15, 15, 15), // open
            ra(66, 15, 15, 15), // wide open
            ra(51, 15, 15, 15), // open
            ra(32, 15, 15, 15)  // closed
        });
        SPRITE_MAP.put(MS_PAC_MUNCHING_BOOSTER, new RectArea[] {
            ra(105, 15, 15, 15), // open
            ra(120, 15, 15, 15), // wide open
            ra(105, 15, 15, 15), // open
            ra(86, 15, 15, 15)   // closed
        });
        SPRITE_MAP.put(MS_PAC_WAVING_HAND, new RectArea[] {
            ra(140, 10, 20, 20),
            ra(163, 10, 20, 20),
        });
        SPRITE_MAP.put(MS_PAC_TURNING_AWAY, new RectArea[] {
            ra(186, 15, 15, 15),
            ra(205, 20, 8, 8),
            ra(218, 21, 5, 5),
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING, new RectArea[] {
            ra(51, 41, 15, 15), // open
            ra(66, 41, 15, 15), // wide open
            ra(51, 41, 15, 15), // open
            ra(32, 41, 15, 15), // closed
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING_BOOSTER, new RectArea[] {
            ra(105, 41, 15, 15), // open
            ra(120, 41, 15, 15), // wide open
            ra(86, 4, 15, 15), // closed
            ra(120, 41, 15, 15), // wide open
        });
        SPRITE_MAP.put(MR_PAC_WAVING_HAND,  new RectArea[] {ra(140, 36, 20, 20), ra(163, 36, 20, 20),});
        SPRITE_MAP.put(MR_PAC_TURNING_AWAY, new RectArea[] {ra(186, 42, 15, 15), ra(205, 46, 8, 8), ra(218, 47, 5, 5),});
        SPRITE_MAP.put(RED_GHOST_RIGHT,     new RectArea[] {ra(10, 120, 14, 13), ra(26, 120, 14, 13),});
        SPRITE_MAP.put(RED_GHOST_LEFT,      new RectArea[] {ra(42, 120, 14, 13), ra(58, 120, 14, 13)});
        SPRITE_MAP.put(RED_GHOST_UP,        new RectArea[] {ra(74, 120, 14, 13), ra(90, 120, 14, 13)});
        SPRITE_MAP.put(RED_GHOST_DOWN,      new RectArea[] {ra(106, 120, 14, 13), ra(122, 120, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_RIGHT,    new RectArea[] {ra(10, 135, 14, 13), ra(26, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_LEFT,     new RectArea[] {ra(42, 135, 14, 13), ra(58, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_UP,       new RectArea[] {ra(74, 135, 14, 13), ra(90, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_DOWN,     new RectArea[] {ra(106, 135, 14, 13), ra(122, 135, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,    new RectArea[] {ra(10, 150, 14, 13), ra(26, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_LEFT,     new RectArea[] {ra(42, 150, 14, 13), ra(58, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_UP,       new RectArea[] {ra(74, 150, 14, 13), ra(90, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_DOWN,     new RectArea[] {ra(106, 150, 14, 13), ra(122, 150, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT,  new RectArea[] {ra(10, 165, 14, 13), ra(26, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,   new RectArea[] {ra(42, 165, 14, 13), ra(58, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_UP,     new RectArea[] {ra(74, 165, 14, 13), ra(90, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,   new RectArea[] {ra(106, 165, 14, 13), ra(122, 165, 14, 13)});
        SPRITE_MAP.put(GHOST_FRIGHTENED,    new RectArea[] {ra(138, 120, 14, 13), ra(154, 120, 14, 13)});
        SPRITE_MAP.put(GHOST_FLASHING, new RectArea[] {
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            ra(138, 120, 14, 13),
            ra(154, 120, 14, 13), // blue
            ra(138, 150, 14, 13),
            ra(154, 150, 14, 13), // white/blue eyes+mouth
        });
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            ra(259, 172, 16, 10),
            ra(279, 172, 16, 10),
            ra(259, 183, 16, 10),
            ra(279, 183, 16, 10)
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 14)
            .mapToObj(i -> new RectArea(BONUS_X[i], 66 + BONUS_DY[i], BONUS_W[i], 20 - BONUS_DY[i]))
            .toArray(RectArea[]::new));
        SPRITE_MAP.put(BONUS_VALUES, IntStream.range(0, 14)
            .mapToObj(i -> new RectArea(BONUS_X[i], 85, BONUS_W[i], 18))
            .toArray(RectArea[]::new));
        SPRITE_MAP.put(CLAPPERBOARD, new RectArea[] {
            // wide-open, open, closed
            ra(91, 361, 32, 32), ra(53, 361, 32, 32), ra(14, 361, 32, 32),
        });
        SPRITE_MAP.put(STORK, new RectArea[] {ra(157, 355, 33, 16), ra(198, 355, 33, 16)});
    }

    public static RectArea   sprite(SpriteID spriteID)  { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] sprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        //TODO should this logic be implemented here?
        // 0=100,1=200,2=500,3=700,4=1000,5=2000,6=3000,7=4000,8=5000,9=6000,10=7000,11=8000,12=9000, 13=10_000
        int index = switch (symbol) {
            case TengenMsPacMan_GameModel.BONUS_BANANA -> 8;    // 5000!
            case TengenMsPacMan_GameModel.BONUS_MILK -> 6;      // 3000!
            case TengenMsPacMan_GameModel.BONUS_ICE_CREAM -> 7; // 4000!
            default -> symbol;
        };
        return sprites(SpriteID.BONUS_VALUES)[index];
    }
}