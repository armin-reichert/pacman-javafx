/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.lib.Sprite.makeSprite;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {

    // Bonus symbols/values: x-position, width, y-delta
    private static final int[] BONUS_X  = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] BONUS_W  = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] BONUS_DY = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(TITLE_TEXT,            makeSprite(15, 191, 152, 40));
        SPRITE_MAP.put(INFO_FRAME,            makeSprite(175, 125, 126, 7));
        SPRITE_MAP.put(INFO_BOOSTER,          makeSprite(190, 134, 7, 5));
        SPRITE_MAP.put(INFO_CATEGORY_BIG,     makeSprite(261, 141, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_MINI,    makeSprite(261, 149, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_STRANGE, makeSprite(261, 133, 26, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_CRAZY, makeSprite(229, 133, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_EASY,  makeSprite(229, 149, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_HARD,  makeSprite(229, 141, 18, 7));
        SPRITE_MAP.put(HEART,                 makeSprite(162, 270, 18, 18));
        SPRITE_MAP.put(BLUE_BAG,              makeSprite(241, 363, 7, 8));
        SPRITE_MAP.put(JUNIOR_PAC,            makeSprite(176, 304, 7, 8));
        SPRITE_MAP.put(GHOST_EYES_RIGHT,      makeSprite(140, 173, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_LEFT,       makeSprite(140, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_UP,         makeSprite(153, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_DOWN,       makeSprite(153, 173, 10, 5));
        SPRITE_MAP.put(CONTINUES_0,           makeSprite(180, 243, 40, 8));
        SPRITE_MAP.put(CONTINUES_1,           makeSprite(180, 234, 40, 8));
        SPRITE_MAP.put(CONTINUES_2,           makeSprite(180, 225, 40, 8));
        SPRITE_MAP.put(CONTINUES_3,           makeSprite(180, 216, 40, 8));
        SPRITE_MAP.put(LEVEL_NUMBER_BOX,      makeSprite(200, 164, 16, 16));
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL,  makeSprite(241, 15, 16, 16));
        SPRITE_MAP.put(DIGIT_1,               makeSprite(185 + 5,      184, 4, 5));
        SPRITE_MAP.put(DIGIT_2,               makeSprite(185 + 5 *  2, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_3,               makeSprite(185 + 5 *  3, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_4,               makeSprite(185 + 5 *  4, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_5,               makeSprite(185 + 5 *  5, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_6,               makeSprite(185 + 5 *  6, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_7,               makeSprite(185 + 5 *  7, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_8,               makeSprite(185 + 5 *  8, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_9,               makeSprite(185 + 5 *  9, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_0,               makeSprite(185 + 5 * 10, 184, 4, 5));
        SPRITE_MAP.put(MS_PAC_MUNCHING, new Sprite[] {
            makeSprite(51, 15, 15, 15), // open
            makeSprite(66, 15, 15, 15), // wide open
            makeSprite(51, 15, 15, 15), // open
            makeSprite(32, 15, 15, 15)  // closed
        });
        SPRITE_MAP.put(MS_PAC_MUNCHING_BOOSTER, new Sprite[] {
            makeSprite(105, 15, 15, 15), // open
            makeSprite(120, 15, 15, 15), // wide open
            makeSprite(105, 15, 15, 15), // open
            makeSprite(86, 15, 15, 15)   // closed
        });
        SPRITE_MAP.put(MS_PAC_WAVING_HAND, new Sprite[] {
            makeSprite(140, 10, 20, 20),
            makeSprite(163, 10, 20, 20),
        });
        SPRITE_MAP.put(MS_PAC_TURNING_AWAY, new Sprite[] {
            makeSprite(186, 15, 15, 15),
            makeSprite(205, 20, 8, 8),
            makeSprite(218, 21, 5, 5),
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING, new Sprite[] {
            makeSprite(51, 41, 15, 15), // open
            makeSprite(66, 41, 15, 15), // wide open
            makeSprite(51, 41, 15, 15), // open
            makeSprite(32, 41, 15, 15), // closed
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING_BOOSTER, new Sprite[] {
            makeSprite(105, 41, 15, 15), // open
            makeSprite(120, 41, 15, 15), // wide open
            makeSprite(86, 4, 15, 15), // closed
            makeSprite(120, 41, 15, 15), // wide open
        });
        SPRITE_MAP.put(MR_PAC_WAVING_HAND, new Sprite[] {
            makeSprite(140, 36, 20, 20),
            makeSprite(163, 36, 20, 20),
        });
        SPRITE_MAP.put(MR_PAC_TURNING_AWAY, new Sprite[] {
            makeSprite(186, 42, 15, 15),
            makeSprite(205, 46, 8, 8),
            makeSprite(218, 47, 5, 5),
        });
        SPRITE_MAP.put(RED_GHOST_RIGHT, new Sprite[] {
            makeSprite(10, 120, 14, 13),
            makeSprite(26, 120, 14, 13),
        });
        SPRITE_MAP.put(RED_GHOST_LEFT, new Sprite[] {
            makeSprite(42, 120, 14, 13),
            makeSprite(58, 120, 14, 13)
        });
        SPRITE_MAP.put(RED_GHOST_UP, new Sprite[] {
            makeSprite(74, 120, 14, 13),
            makeSprite(90, 120, 14, 13)
        });
        SPRITE_MAP.put(RED_GHOST_DOWN, new Sprite[] {
            makeSprite(106, 120, 14, 13),
            makeSprite(122, 120, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_RIGHT, new Sprite[] {
            makeSprite(10, 135, 14, 13),
            makeSprite(26, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_LEFT, new Sprite[] {
            makeSprite(42, 135, 14, 13),
            makeSprite(58, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_UP, new Sprite[] {
            makeSprite(74, 135, 14, 13),
            makeSprite(90, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_DOWN, new Sprite[] {
            makeSprite(106, 135, 14, 13),
            makeSprite(122, 135, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_RIGHT, new Sprite[] {
            makeSprite(10, 150, 14, 13),
            makeSprite(26, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_LEFT, new Sprite[] {
            makeSprite(42, 150, 14, 13),
            makeSprite(58, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_UP, new Sprite[] {
            makeSprite(74, 150, 14, 13),
            makeSprite(90, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_DOWN, new Sprite[] {
            makeSprite(106, 150, 14, 13),
            makeSprite(122, 150, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT, new Sprite[] {
            makeSprite(10, 165, 14, 13),
            makeSprite(26, 165, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_LEFT, new Sprite[] {
            makeSprite(42, 165, 14, 13),
            makeSprite(58, 165, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_UP, new Sprite[] {
            makeSprite(74, 165, 14, 13),
            makeSprite(90, 165, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_DOWN, new Sprite[] {
            makeSprite(106, 165, 14, 13),
            makeSprite(122, 165, 14, 13)
        });
        SPRITE_MAP.put(GHOST_FRIGHTENED, new Sprite[] {
            makeSprite(138, 120, 14, 13),
            makeSprite(154, 120, 14, 13)
        });
        SPRITE_MAP.put(GHOST_FLASHING, new Sprite[] {
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            makeSprite(138, 120, 14, 13),
            makeSprite(154, 120, 14, 13), // blue
            makeSprite(138, 150, 14, 13),
            makeSprite(154, 150, 14, 13), // white/blue eyes+mouth
        });
        SPRITE_MAP.put(GHOST_NUMBERS, new Sprite[] {
            makeSprite(259, 172, 16, 10),
            makeSprite(279, 172, 16, 10),
            makeSprite(259, 183, 16, 10),
            makeSprite(279, 183, 16, 10)
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, IntStream.range(0, 14)
            .mapToObj(i -> makeSprite(BONUS_X[i], 66 + BONUS_DY[i], BONUS_W[i], 20 - BONUS_DY[i]))
            .toArray(Sprite[]::new));
        SPRITE_MAP.put(BONUS_VALUES, IntStream.range(0, 14)
            .mapToObj(i -> makeSprite(BONUS_X[i], 85, BONUS_W[i], 18))
            .toArray(Sprite[]::new));
        SPRITE_MAP.put(CLAPPERBOARD, new Sprite[] {
            // wide-open, open, closed
            makeSprite(91, 361, 32, 32),
            makeSprite(53, 361, 32, 32),
            makeSprite(14, 361, 32, 32),
        });
        SPRITE_MAP.put(STORK, new Sprite[] {
            makeSprite(157, 355, 33, 16),
            makeSprite(198, 355, 33, 16)
        });
    }

    public TengenMsPacMan_SpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public Map<SpriteID, Object> spriteMap() {
        return SPRITE_MAP;
    }
}