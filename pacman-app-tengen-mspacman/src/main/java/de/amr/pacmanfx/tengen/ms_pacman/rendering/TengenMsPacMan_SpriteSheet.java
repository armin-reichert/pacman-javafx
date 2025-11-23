/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.pacmanfx.lib.RectShort.rect;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements SpriteSheet<SpriteID> {

    // Bonus symbols/values: x-position, width, y-delta
    private static final int[] BONUS_X  = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] BONUS_W  = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] BONUS_DY = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final SpriteMap<SpriteID> SPRITE_MAP = new SpriteMap<>(SpriteID.class);

    static {
        SPRITE_MAP.add(LARGE_MS_PAC_MAN_TEXT, rect(15, 191, 152, 40));
        SPRITE_MAP.add(INFO_FRAME,            rect(175, 125, 126, 7));
        SPRITE_MAP.add(INFO_BOOSTER,          rect(190, 134, 7, 5));
        SPRITE_MAP.add(INFO_CATEGORY_BIG,     rect(261, 141, 26, 7));
        SPRITE_MAP.add(INFO_CATEGORY_MINI,    rect(261, 149, 26, 7));
        SPRITE_MAP.add(INFO_CATEGORY_STRANGE, rect(261, 133, 26, 7));
        SPRITE_MAP.add(INFO_DIFFICULTY_CRAZY, rect(229, 133, 18, 7));
        SPRITE_MAP.add(INFO_DIFFICULTY_EASY,  rect(229, 149, 18, 7));
        SPRITE_MAP.add(INFO_DIFFICULTY_HARD,  rect(229, 141, 18, 7));
        SPRITE_MAP.add(HEART,                 rect(162, 270, 18, 18));
        SPRITE_MAP.add(BLUE_BAG,              rect(241, 363, 7, 8));
        SPRITE_MAP.add(JUNIOR_PAC,            rect(176, 304, 7, 8));
        SPRITE_MAP.add(GHOST_EYES_RIGHT,      rect(140, 173, 10, 5));
        SPRITE_MAP.add(GHOST_EYES_LEFT,       rect(140, 166, 10, 5));
        SPRITE_MAP.add(GHOST_EYES_UP,         rect(153, 166, 10, 5));
        SPRITE_MAP.add(GHOST_EYES_DOWN,       rect(153, 173, 10, 5));
        SPRITE_MAP.add(CONTINUES_0,           rect(180, 243, 40, 8));
        SPRITE_MAP.add(CONTINUES_1,           rect(180, 234, 40, 8));
        SPRITE_MAP.add(CONTINUES_2,           rect(180, 225, 40, 8));
        SPRITE_MAP.add(CONTINUES_3,           rect(180, 216, 40, 8));
        SPRITE_MAP.add(LEVEL_NUMBER_BOX,      rect(200, 164, 16, 16));
        SPRITE_MAP.add(LIVES_COUNTER_SYMBOL,  rect(241, 15, 16, 16));
        SPRITE_MAP.add(DIGIT_1,               rect(185 + 5,      184, 4, 5));
        SPRITE_MAP.add(DIGIT_2,               rect(185 + 5 *  2, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_3,               rect(185 + 5 *  3, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_4,               rect(185 + 5 *  4, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_5,               rect(185 + 5 *  5, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_6,               rect(185 + 5 *  6, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_7,               rect(185 + 5 *  7, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_8,               rect(185 + 5 *  8, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_9,               rect(185 + 5 *  9, 184, 4, 5));
        SPRITE_MAP.add(DIGIT_0,               rect(185 + 5 * 10, 184, 4, 5));
        SPRITE_MAP.add(MS_PAC_FULL,           rect(32, 15, 15, 15));
        SPRITE_MAP.add(MS_PAC_MUNCHING,
            rect(51, 15, 15, 15), // open
            rect(66, 15, 15, 15), // wide open
            rect(51, 15, 15, 15), // open
            rect(32, 15, 15, 15)  // closed
        );
        SPRITE_MAP.add(MS_PAC_MUNCHING_BOOSTER,
            rect(105, 15, 15, 15), // open
            rect(120, 15, 15, 15), // wide open
            rect(105, 15, 15, 15), // open
            rect(86, 15, 15, 15)   // closed
        );
        SPRITE_MAP.add(MS_PAC_WAVING_HAND,
            rect(140, 10, 20, 20),
            rect(163, 10, 20, 20)
        );
        SPRITE_MAP.add(MS_PAC_TURNING_AWAY,
            rect(186, 15, 15, 15),
            rect(205, 20, 8, 8),
            rect(218, 21, 5, 5)
        );
        SPRITE_MAP.add(MR_PAC_MUNCHING,
            rect(51, 41, 15, 15), // open
            rect(66, 41, 15, 15), // wide open
            rect(51, 41, 15, 15), // open
            rect(32, 41, 15, 15)  // closed
        );
        SPRITE_MAP.add(MR_PAC_MUNCHING_BOOSTER,
            rect(105, 41, 15, 15), // open
            rect(120, 41, 15, 15), // wide open
            rect(86, 4, 15, 15),   // closed
            rect(120, 41, 15, 15)  // wide open
        );
        SPRITE_MAP.add(MR_PAC_WAVING_HAND,
            rect(140, 36, 20, 20),
            rect(163, 36, 20, 20)
        );
        SPRITE_MAP.add(MR_PAC_TURNING_AWAY,
            rect(186, 42, 15, 15),
            rect(205, 46, 8, 8),
            rect(218, 47, 5, 5)
        );
        SPRITE_MAP.add(RED_GHOST_RIGHT,
            rect(10, 120, 14, 13),
            rect(26, 120, 14, 13)
        );
        SPRITE_MAP.add(RED_GHOST_LEFT,
            rect(42, 120, 14, 13),
            rect(58, 120, 14, 13)
        );
        SPRITE_MAP.add(RED_GHOST_UP,
            rect(74, 120, 14, 13),
            rect(90, 120, 14, 13)
        );
        SPRITE_MAP.add(RED_GHOST_DOWN,
            rect(106, 120, 14, 13),
            rect(122, 120, 14, 13)
        );
        SPRITE_MAP.add(PINK_GHOST_RIGHT,
            rect(10, 135, 14, 13),
            rect(26, 135, 14, 13)
        );
        SPRITE_MAP.add(PINK_GHOST_LEFT,
            rect(42, 135, 14, 13),
            rect(58, 135, 14, 13)
        );
        SPRITE_MAP.add(PINK_GHOST_UP,
            rect(74, 135, 14, 13),
            rect(90, 135, 14, 13)
        );
        SPRITE_MAP.add(PINK_GHOST_DOWN,
            rect(106, 135, 14, 13),
            rect(122, 135, 14, 13)
        );
        SPRITE_MAP.add(CYAN_GHOST_RIGHT,
            rect(10, 150, 14, 13),
            rect(26, 150, 14, 13)
        );
        SPRITE_MAP.add(CYAN_GHOST_LEFT,
            rect(42, 150, 14, 13),
            rect(58, 150, 14, 13)
        );
        SPRITE_MAP.add(CYAN_GHOST_UP,
            rect(74, 150, 14, 13),
            rect(90, 150, 14, 13)
        );
        SPRITE_MAP.add(CYAN_GHOST_DOWN,
            rect(106, 150, 14, 13),
            rect(122, 150, 14, 13)
        );
        SPRITE_MAP.add(ORANGE_GHOST_RIGHT,
            rect(10, 165, 14, 13),
            rect(26, 165, 14, 13)
        );
        SPRITE_MAP.add(ORANGE_GHOST_LEFT,
            rect(42, 165, 14, 13),
            rect(58, 165, 14, 13)
        );
        SPRITE_MAP.add(ORANGE_GHOST_UP,
            rect(74, 165, 14, 13),
            rect(90, 165, 14, 13)
        );
        SPRITE_MAP.add(ORANGE_GHOST_DOWN,
            rect(106, 165, 14, 13),
            rect(122, 165, 14, 13)
        );
        SPRITE_MAP.add(GHOST_FRIGHTENED,
            rect(138, 120, 14, 13),
            rect(154, 120, 14, 13)
        );
        SPRITE_MAP.add(GHOST_FLASHING,
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            rect(138, 120, 14, 13),
            rect(154, 120, 14, 13), // blue
            rect(138, 150, 14, 13),
            rect(154, 150, 14, 13) // white/blue eyes+mouth
        );
        SPRITE_MAP.add(GHOST_NUMBERS,
            rect(259, 172, 16, 10),
            rect(279, 172, 16, 10),
            rect(259, 183, 16, 10),
            rect(279, 183, 16, 10)
        );
        SPRITE_MAP.add(BONUS_SYMBOLS, IntStream.range(0, 14)
            .mapToObj(i -> rect(BONUS_X[i], 66 + BONUS_DY[i], BONUS_W[i], 20 - BONUS_DY[i]))
            .toArray(RectShort[]::new));
        SPRITE_MAP.add(BONUS_VALUES, IntStream.range(0, 14)
            .mapToObj(i -> rect(BONUS_X[i], 85, BONUS_W[i], 18))
            .toArray(RectShort[]::new));
        SPRITE_MAP.add(CLAPPERBOARD,
            // wide-open, open, closed
            rect(91, 361, 32, 32),
            rect(53, 361, 32, 32),
            rect(14, 361, 32, 32)
        );
        SPRITE_MAP.add(STORK,
            rect(157, 355, 33, 16),
            rect(198, 355, 33, 16)
        );

        SPRITE_MAP.checkCompleteness();
    }

    public RectShort digitSprite(int digit) {
        return sprite(switch (digit) {
            case 0 -> SpriteID.DIGIT_0;
            case 1 -> SpriteID.DIGIT_1;
            case 2 -> SpriteID.DIGIT_2;
            case 3 -> SpriteID.DIGIT_3;
            case 4 -> SpriteID.DIGIT_4;
            case 5 -> SpriteID.DIGIT_5;
            case 6 -> SpriteID.DIGIT_6;
            case 7 -> SpriteID.DIGIT_7;
            case 8 -> SpriteID.DIGIT_8;
            case 9 -> SpriteID.DIGIT_9;
            default -> throw new IllegalArgumentException("Illegal digit value " + digit);
        });
    }


    public TengenMsPacMan_SpriteSheet {
        requireNonNull(sourceImage);
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