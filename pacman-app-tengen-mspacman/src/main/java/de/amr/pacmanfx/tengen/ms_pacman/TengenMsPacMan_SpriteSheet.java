/*
 * Copyright (c) 2021-2025 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import javafx.scene.image.Image;

import java.util.EnumMap;

import static de.amr.pacmanfx.lib.RectArea.rect;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.SpriteID.*;

public record TengenMsPacMan_SpriteSheet(Image sourceImage) implements GameSpriteSheet {

    public enum SpriteID {
        TITLE_TEXT,
        INFO_FRAME,
        INFO_BOOSTER,
        INFO_CATEGORY_BIG, INFO_CATEGORY_MINI, INFO_CATEGORY_STRANGE,
        INFO_DIFFICULTY_CRAZY, INFO_DIFFICULTY_EASY, INFO_DIFFICULTY_HARD,
        HEART, BLUE_BAG, JUNIOR_PAC,
        MS_PAC_MUNCHING, MS_PAC_MUNCHING_BOOSTER,
        MS_PAC_WAVING_HAND,
        MS_PAC_TURNING_AWAY,
        MR_PAC_MUNCHING, MR_PAC_MUNCHING_BOOSTER,
        MR_PAC_WAVING_HAND,
        MR_PAC_TURNING_AWAY,
        RED_GHOST_RIGHT, RED_GHOST_LEFT, RED_GHOST_UP, RED_GHOST_DOWN,
        PINK_GHOST_RIGHT, PINK_GHOST_LEFT, PINK_GHOST_UP, PINK_GHOST_DOWN,
        CYAN_GHOST_RIGHT, CYAN_GHOST_LEFT, CYAN_GHOST_UP, CYAN_GHOST_DOWN,
        ORANGE_GHOST_RIGHT, ORANGE_GHOST_LEFT, ORANGE_GHOST_UP, ORANGE_GHOST_DOWN,
        GHOST_FRIGHTENED,
        GHOST_FLASHING,
        GHOST_EYES_RIGHT, GHOST_EYES_LEFT, GHOST_EYES_UP, GHOST_EYES_DOWN,
        GHOST_NUMBERS,
        BONUS_SYMBOLS,
        BONUS_VALUES,
        CONTINUES_0, CONTINUES_1, CONTINUES_2, CONTINUES_3,
        LEVEL_NUMBER_BOX,
        CLAPPERBOARD,
        STORK,
        LIVES_COUNTER_SYMBOL,
        DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4, DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9, DIGIT_0,
    }

    private static final int[] xs = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] ws = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] dy = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static RectArea[] sillyBonusSymbolSpriteExtraction() {
        RectArea[] symbolSprites = new RectArea[14];
        for (int i = 0; i < 14; ++i) {
            symbolSprites[i] = new RectArea(xs[i], 66 + dy[i], ws[i], 20 - dy[i]);
        }
        return symbolSprites;
    }

    private static RectArea[] sillyBonusValueSpriteExtraction() {
        RectArea[] valueSprites = new RectArea[14];
        for (int i = 0; i < 14; ++i) {
            valueSprites[i] = new RectArea(xs[i], 85, ws[i], 18);
        }
        return valueSprites;
    }

    private static final EnumMap<SpriteID, Object> SPRITE_MAP = new EnumMap<>(SpriteID.class);
    static {
        SPRITE_MAP.put(TITLE_TEXT, rect(15, 191, 152, 40));
        SPRITE_MAP.put(INFO_FRAME, rect(175, 125, 126, 7));
        SPRITE_MAP.put(INFO_BOOSTER, rect(190, 134, 7, 5));
        SPRITE_MAP.put(INFO_CATEGORY_BIG, rect(261, 141, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_MINI, rect(261, 149, 26, 7));
        SPRITE_MAP.put(INFO_CATEGORY_STRANGE, rect(261, 133, 26, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_CRAZY, rect(229, 133, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_EASY, rect(229, 149, 18, 7));
        SPRITE_MAP.put(INFO_DIFFICULTY_HARD, rect(229, 141, 18, 7));
        SPRITE_MAP.put(HEART, rect(162, 270, 18, 18));
        SPRITE_MAP.put(BLUE_BAG, rect(241, 363, 7, 8));
        SPRITE_MAP.put(JUNIOR_PAC, rect(176, 304, 7, 8));
        SPRITE_MAP.put(GHOST_EYES_RIGHT, rect(140, 173, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_LEFT, rect(140, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_UP, rect(153, 166, 10, 5));
        SPRITE_MAP.put(GHOST_EYES_DOWN, rect(153, 173, 10, 5));
        SPRITE_MAP.put(CONTINUES_0, rect(180, 243, 40, 8));
        SPRITE_MAP.put(CONTINUES_1, rect(180, 234, 40, 8));
        SPRITE_MAP.put(CONTINUES_2, rect(180, 225, 40, 8));
        SPRITE_MAP.put(CONTINUES_3, rect(180, 216, 40, 8));
        SPRITE_MAP.put(LEVEL_NUMBER_BOX, rect(200, 164, 16, 16));
        SPRITE_MAP.put(LIVES_COUNTER_SYMBOL, rect(241, 15, 16, 16));
        SPRITE_MAP.put(DIGIT_1, rect(185 + 5,      184, 4, 5));
        SPRITE_MAP.put(DIGIT_2, rect(185 + 5 *  2, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_3, rect(185 + 5 *  3, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_4, rect(185 + 5 *  4, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_5, rect(185 + 5 *  5, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_6, rect(185 + 5 *  6, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_7, rect(185 + 5 *  7, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_8, rect(185 + 5 *  8, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_9, rect(185 + 5 *  9, 184, 4, 5));
        SPRITE_MAP.put(DIGIT_0, rect(185 + 5 * 10, 184, 4, 5));
        SPRITE_MAP.put(MS_PAC_MUNCHING, new RectArea[] {
            rect(51, 15, 15, 15), // open
            rect(66, 15, 15, 15), // wide open
            rect(51, 15, 15, 15), // open
            rect(32, 15, 15, 15)  // closed
        });
        SPRITE_MAP.put(MS_PAC_MUNCHING_BOOSTER, new RectArea[] {
            rect(105, 15, 15, 15), // open
            rect(120, 15, 15, 15), // wide open
            rect(105, 15, 15, 15), // open
            rect(86, 15, 15, 15)   // closed
        });
        SPRITE_MAP.put(MS_PAC_WAVING_HAND, new RectArea[] {
            rect(140, 10, 20, 20),
            rect(163, 10, 20, 20),
        });
        SPRITE_MAP.put(MS_PAC_TURNING_AWAY, new RectArea[] {
            rect(186, 15, 15, 15),
            rect(205, 20, 8, 8),
            rect(218, 21, 5, 5),
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING, new RectArea[] {
            rect(51, 41, 15, 15), // open
            rect(66, 41, 15, 15), // wide open
            rect(51, 41, 15, 15), // open
            rect(32, 41, 15, 15), // closed
        });
        SPRITE_MAP.put(MR_PAC_MUNCHING_BOOSTER, new RectArea[] {
            rect(105, 41, 15, 15), // open
            rect(120, 41, 15, 15), // wide open
            rect(86, 4, 15, 15), // closed
            rect(120, 41, 15, 15), // wide open
        });
        SPRITE_MAP.put(MR_PAC_WAVING_HAND, new RectArea[] {
            rect(140, 36, 20, 20), rect(163, 36, 20, 20),
        });
        SPRITE_MAP.put(MR_PAC_TURNING_AWAY, new RectArea[] {
            rect(186, 42, 15, 15), rect(205, 46, 8, 8), rect(218, 47, 5, 5),
        });
        SPRITE_MAP.put(RED_GHOST_RIGHT, new RectArea[] {
            rect(10, 120, 14, 13), rect(26, 120, 14, 13),
        });
        SPRITE_MAP.put(RED_GHOST_LEFT, new RectArea[] {
            rect(42, 120, 14, 13), rect(58, 120, 14, 13)
        });
        SPRITE_MAP.put(RED_GHOST_UP, new RectArea[] {
            rect(74, 120, 14, 13), rect(90, 120, 14, 13)
        });
        SPRITE_MAP.put(RED_GHOST_DOWN, new RectArea[] {
            rect(106, 120, 14, 13), rect(122, 120, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_RIGHT, new RectArea[] {
            rect(10, 135, 14, 13), rect(26, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_LEFT, new RectArea[] {
            rect(42, 135, 14, 13), rect(58, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_UP, new RectArea[] {
            rect(74, 135, 14, 13), rect(90, 135, 14, 13)
        });
        SPRITE_MAP.put(PINK_GHOST_DOWN, new RectArea[] {
            rect(106, 135, 14, 13), rect(122, 135, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_RIGHT, new RectArea[] {
            rect(10, 150, 14, 13), rect(26, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_LEFT, new RectArea[] {
            rect(42, 150, 14, 13), rect(58, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_UP, new RectArea[] {
            rect(74, 150, 14, 13), rect(90, 150, 14, 13)
        });
        SPRITE_MAP.put(CYAN_GHOST_DOWN, new RectArea[] {
            rect(106, 150, 14, 13), rect(122, 150, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_RIGHT, new RectArea[] {
            rect(10, 165, 14, 13), rect(26, 165, 14, 13)            });
        SPRITE_MAP.put(ORANGE_GHOST_LEFT, new RectArea[] {
            rect(42, 165, 14, 13), rect(58, 165, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_UP, new RectArea[] {
            rect(74, 165, 14, 13), rect(90, 165, 14, 13)
        });
        SPRITE_MAP.put(ORANGE_GHOST_DOWN, new RectArea[] {
            rect(106, 165, 14, 13), rect(122, 165, 14, 13)
        });
        SPRITE_MAP.put(GHOST_FRIGHTENED, new RectArea[] {
            rect(138, 120, 14, 13), rect(154, 120, 14, 13)
        });
        SPRITE_MAP.put(GHOST_FLASHING, new RectArea[] {
            //TODO when are the white-red sprites used?
            //rect(138, 120, 14, 13), rect(154, 120, 14, 13), // blue
            //rect(138, 135, 14, 13),  rect(154, 135, 14, 13), // white/red eyes+mouth
            rect(138, 120, 14, 13),
            rect(154, 120, 14, 13), // blue
            rect(138, 150, 14, 13),
            rect(154, 150, 14, 13), // white/blue eyes+mouth
        });
        SPRITE_MAP.put(GHOST_NUMBERS, new RectArea[] {
            rect(259, 172, 16, 10),
            rect(279, 172, 16, 10),
            rect(259, 183, 16, 10),
            rect(279, 183, 16, 10)
        });
        SPRITE_MAP.put(BONUS_SYMBOLS, sillyBonusSymbolSpriteExtraction());
        SPRITE_MAP.put(BONUS_VALUES, sillyBonusValueSpriteExtraction());
        SPRITE_MAP.put(CLAPPERBOARD, new RectArea[] {
            // wide open, open, closed
            rect(91, 361, 32, 32), rect(53, 361, 32, 32), rect(14, 361, 32, 32),
        });
        SPRITE_MAP.put(STORK, new RectArea[] {
            rect(157, 355, 33, 16), rect(198, 355, 33, 16)}
        );
    }

    public static RectArea getSprite(SpriteID spriteID) { return (RectArea) SPRITE_MAP.get(spriteID); }
    public static RectArea[] getSprites(SpriteID spriteID) { return (RectArea[]) SPRITE_MAP.get(spriteID); }

    @Override
    public RectArea[] ghostEyesSprites(Direction dir) {
        return new RectArea[] {
            switch (dir) {
                case RIGHT -> getSprite(SpriteID.GHOST_EYES_RIGHT);
                case LEFT -> getSprite(SpriteID.GHOST_EYES_LEFT);
                case UP -> getSprite(SpriteID.GHOST_EYES_UP);
                case DOWN -> getSprite(SpriteID.GHOST_EYES_DOWN);
            }
        };
    }

    @Override
    public RectArea[] ghostFlashingSprites() { return getSprites(SpriteID.GHOST_FLASHING); }

    @Override
    public RectArea[] ghostNumberSprites() { return getSprites(SpriteID.GHOST_NUMBERS); }

    @Override
    public RectArea livesCounterSprite() { return getSprite(SpriteID.LIVES_COUNTER_SYMBOL); }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) { return getSprites(SpriteID.BONUS_SYMBOLS)[symbol]; }

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
        return getSprites(SpriteID.BONUS_VALUES)[index];
    }
}