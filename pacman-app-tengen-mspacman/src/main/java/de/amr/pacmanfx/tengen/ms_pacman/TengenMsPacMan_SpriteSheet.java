/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.tengen.ms_pacman.SpriteID.*;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements SpriteSheet {

    // Bonus symbols/values: x-position, width, y-delta
    private static final int[] BONUS_X  = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] BONUS_W  = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] BONUS_DY = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(TITLE_TEXT, Sprite.sprite(15, 191, 152, 40));
        SPRITE_MAP.put(INFO_FRAME, Sprite.sprite(175, 125, 126, 7));
        SPRITE_MAP.put(INFO_BOOSTER, Sprite.sprite(190, 134, 7, 5));
        SPRITE_MAP.put(INFO_CATEGORY_BIG, Sprite.sprite(261, 141, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_MINI, Sprite.sprite(261, 149, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_STRANGE, Sprite.sprite(261, 133, 26, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_CRAZY, Sprite.sprite(229, 133, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_EASY, Sprite.sprite(229, 149, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_HARD, Sprite.sprite(229, 141, 18, 7));
        SPRITE_MAP.put(HEART, Sprite.sprite(162, 270, 18, 18));
        SPRITE_MAP.put(BLUE_BAG, Sprite.sprite(241, 363, 7, 8));
        SPRITE_MAP.put(JUNIOR_PAC, Sprite.sprite(176, 304, 7, 8));
        SPRITE_MAP.put(GHOST_EYES_RIGHT, Sprite.sprite(140, 173, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_LEFT, Sprite.sprite(140, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_UP, Sprite.sprite(153, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_DOWN, Sprite.sprite(153, 173, 10, 5));
        SPRITE_MAP.put(CONTINUES_0, Sprite.sprite(180, 243, 40, 8));
        SPRITE_MAP.put(CONTINUES_1, Sprite.sprite(180, 234, 40, 8));
        SPRITE_MAP.put(CONTINUES_2, Sprite.sprite(180, 225, 40, 8));
        SPRITE_MAP.put(CONTINUES_3, Sprite.sprite(180, 216, 40, 8));
        SPRITE_MAP.put(LEVEL_NUMBER_BOX, Sprite.sprite(200, 164, 16, 16));
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, Sprite.sprite(241, 15, 16, 16));
        SPRITE_MAP.put(DIGIT_1, Sprite.sprite(185 + 5,      184, 4, 5));
        SPRITE_MAP.put(DIGIT_2, Sprite.sprite(185 + 5 *  2, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_3, Sprite.sprite(185 + 5 *  3, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_4, Sprite.sprite(185 + 5 *  4, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_5, Sprite.sprite(185 + 5 *  5, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_6, Sprite.sprite(185 + 5 *  6, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_7, Sprite.sprite(185 + 5 *  7, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_8, Sprite.sprite(185 + 5 *  8, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_9, Sprite.sprite(185 + 5 *  9, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_0, Sprite.sprite(185 + 5 * 10, 184, 4, 5));
        SPRITE_MAP.put(MS_PAC_MUNCHING, new Sprite[] {
            Sprite.sprite(51, 15, 15, 15), // open
            Sprite.sprite(66, 15, 15, 15), // wide open
            Sprite.sprite(51, 15, 15, 15), // open
            Sprite.sprite(32, 15, 15, 15)  // closed
        });
        SPRITE_MAP.put(MS_PAC_MUNCHING_BOOSTER, new Sprite[] {
            Sprite.sprite(105, 15, 15, 15), // open
            Sprite.sprite(120, 15, 15, 15), // wide open
            Sprite.sprite(105, 15, 15, 15), // open
            Sprite.sprite(86, 15, 15, 15)   // closed
        });
        SPRITE_MAP.put(MS_PAC_WAVING_HAND, new Sprite[] {
            Sprite.sprite(140, 10, 20, 20),
            Sprite.sprite(163, 10, 20, 20),
        });
        SPRITE_MAP.put(MS_PAC_TURNING_AWAY, new Sprite[] {
            Sprite.sprite(186, 15, 15, 15),
            Sprite.sprite(205, 20, 8, 8),
            Sprite.sprite(218, 21, 5, 5),
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING, new Sprite[] {
            Sprite.sprite(51, 41, 15, 15), // open
            Sprite.sprite(66, 41, 15, 15), // wide open
            Sprite.sprite(51, 41, 15, 15), // open
            Sprite.sprite(32, 41, 15, 15), // closed
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING_BOOSTER, new Sprite[] {
            Sprite.sprite(105, 41, 15, 15), // open
            Sprite.sprite(120, 41, 15, 15), // wide open
            Sprite.sprite(86, 4, 15, 15), // closed
            Sprite.sprite(120, 41, 15, 15), // wide open
        });
        SPRITE_MAP.put(MR_PAC_WAVING_HAND,  new Sprite[] {Sprite.sprite(140, 36, 20, 20), Sprite.sprite(163, 36, 20, 20),});
        SPRITE_MAP.put(MR_PAC_TURNING_AWAY, new Sprite[] {Sprite.sprite(186, 42, 15, 15), Sprite.sprite(205, 46, 8, 8), Sprite.sprite(218, 47, 5, 5),});
        SPRITE_MAP.put(RED_GHOST_RIGHT,     new Sprite[] {Sprite.sprite(10, 120, 14, 13), Sprite.sprite(26, 120, 14, 13),});
        SPRITE_MAP.put(RED_GHOST_LEFT,      new Sprite[] {Sprite.sprite(42, 120, 14, 13), Sprite.sprite(58, 120, 14, 13)});
        SPRITE_MAP.put(RED_GHOST_UP,        new Sprite[] {Sprite.sprite(74, 120, 14, 13), Sprite.sprite(90, 120, 14, 13)});
        SPRITE_MAP.put(RED_GHOST_DOWN,      new Sprite[] {Sprite.sprite(106, 120, 14, 13), Sprite.sprite(122, 120, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_RIGHT,    new Sprite[] {Sprite.sprite(10, 135, 14, 13), Sprite.sprite(26, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_LEFT,     new Sprite[] {Sprite.sprite(42, 135, 14, 13), Sprite.sprite(58, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_UP,       new Sprite[] {Sprite.sprite(74, 135, 14, 13), Sprite.sprite(90, 135, 14, 13)});
        SPRITE_MAP.put(PINK_GHOST_DOWN,     new Sprite[] {Sprite.sprite(106, 135, 14, 13), Sprite.sprite(122, 135, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_RIGHT,    new Sprite[] {Sprite.sprite(10, 150, 14, 13), Sprite.sprite(26, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_LEFT,     new Sprite[] {Sprite.sprite(42, 150, 14, 13), Sprite.sprite(58, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_UP,       new Sprite[] {Sprite.sprite(74, 150, 14, 13), Sprite.sprite(90, 150, 14, 13)});
        SPRITE_MAP.put(CYAN_GHOST_DOWN,     new Sprite[] {Sprite.sprite(106, 150, 14, 13), Sprite.sprite(122, 150, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT,  new Sprite[] {Sprite.sprite(10, 165, 14, 13), Sprite.sprite(26, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_LEFT,   new Sprite[] {Sprite.sprite(42, 165, 14, 13), Sprite.sprite(58, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_UP,     new Sprite[] {Sprite.sprite(74, 165, 14, 13), Sprite.sprite(90, 165, 14, 13)});
        SPRITE_MAP.put(ORANGE_GHOST_DOWN,   new Sprite[] {Sprite.sprite(106, 165, 14, 13), Sprite.sprite(122, 165, 14, 13)});
        SPRITE_MAP.put(GHOST_FRIGHTENED,    new Sprite[] {Sprite.sprite(138, 120, 14, 13), Sprite.sprite(154, 120, 14, 13)});
        SPRITE_MAP.put(GHOST_FLASHING, new Sprite[] {
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            Sprite.sprite(138, 120, 14, 13),
            Sprite.sprite(154, 120, 14, 13), // blue
            Sprite.sprite(138, 150, 14, 13),
            Sprite.sprite(154, 150, 14, 13), // white/blue eyes+mouth
        });
        SPRITE_MAP.put(GHOST_NUMBERS, new Sprite[] {
            Sprite.sprite(259, 172, 16, 10),
            Sprite.sprite(279, 172, 16, 10),
            Sprite.sprite(259, 183, 16, 10),
            Sprite.sprite(279, 183, 16, 10)
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 14)
            .mapToObj(i -> new Sprite(BONUS_X[i], 66 + BONUS_DY[i], BONUS_W[i], 20 - BONUS_DY[i]))
            .toArray(Sprite[]::new));
        SPRITE_MAP.put(BONUS_VALUES, IntStream.range(0, 14)
            .mapToObj(i -> new Sprite(BONUS_X[i], 85, BONUS_W[i], 18))
            .toArray(Sprite[]::new));
        SPRITE_MAP.put(CLAPPERBOARD, new Sprite[] {
            // wide-open, open, closed
            Sprite.sprite(91, 361, 32, 32), Sprite.sprite(53, 361, 32, 32), Sprite.sprite(14, 361, 32, 32),
        });
        SPRITE_MAP.put(STORK, new Sprite[] {Sprite.sprite(157, 355, 33, 16), Sprite.sprite(198, 355, 33, 16)});
    }

    public static Sprite sprite(SpriteID spriteID)  { return (Sprite) SPRITE_MAP.get(spriteID); }
    public static Sprite[] sprites(SpriteID spriteID) { return (Sprite[]) SPRITE_MAP.get(spriteID); }
}