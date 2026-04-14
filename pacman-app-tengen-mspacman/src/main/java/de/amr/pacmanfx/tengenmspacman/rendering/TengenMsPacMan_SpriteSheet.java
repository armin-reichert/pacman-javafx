/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.*;

public final class TengenMsPacMan_SpriteSheet implements SpriteSheet<SpriteID> {

    private static class Holder {
        static final TengenMsPacMan_SpriteSheet INSTANCE = new TengenMsPacMan_SpriteSheet();
    }

    public static TengenMsPacMan_SpriteSheet instance() {
        return Holder.INSTANCE;
    }

    // Bonus symbols/values: x-position, width, y-delta
    private static final int[] BONUS_X  = {8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272};
    private static final int[] BONUS_W  = {16, 15, 16, 18, 18, 20, 18, 18, 18, 18, 18, 18, 18, 18};
    private static final int[] BONUS_DY = {3, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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

    private final SpriteMap<SpriteID> spriteMap = new SpriteMap<>(SpriteID.class);
    private final Image image;

    private TengenMsPacMan_SpriteSheet() {
        final ResourceManager moduleResources = () -> TengenMsPacMan_UIConfig.class;
        image = moduleResources.loadImage(TengenMsPacMan_UIConfig.REL_PATH_SPRITE_SHEET_IMAGE);

        spriteMap.add(LARGE_MS_PAC_MAN_TEXT, RectShort.of(15, 191, 152, 40));
        spriteMap.add(INFO_FRAME,            RectShort.of(175, 125, 126, 7));
        spriteMap.add(INFO_BOOSTER,          RectShort.of(190, 134, 7, 5));
        spriteMap.add(INFO_CATEGORY_BIG,     RectShort.of(261, 141, 26, 7));
        spriteMap.add(INFO_CATEGORY_MINI,    RectShort.of(261, 149, 26, 7));
        spriteMap.add(INFO_CATEGORY_STRANGE, RectShort.of(261, 133, 26, 7));
        spriteMap.add(INFO_DIFFICULTY_CRAZY, RectShort.of(229, 133, 18, 7));
        spriteMap.add(INFO_DIFFICULTY_EASY,  RectShort.of(229, 149, 18, 7));
        spriteMap.add(INFO_DIFFICULTY_HARD,  RectShort.of(229, 141, 18, 7));
        spriteMap.add(HEART,                 RectShort.of(162, 270, 18, 18));
        spriteMap.add(BLUE_BAG,              RectShort.of(241, 363, 7, 8));
        spriteMap.add(JUNIOR_PAC,            RectShort.of(176, 304, 7, 8));
        spriteMap.add(GHOST_EYES_RIGHT,      RectShort.of(140, 173, 10, 5));
        spriteMap.add(GHOST_EYES_LEFT,       RectShort.of(140, 166, 10, 5));
        spriteMap.add(GHOST_EYES_UP,         RectShort.of(153, 166, 10, 5));
        spriteMap.add(GHOST_EYES_DOWN,       RectShort.of(153, 173, 10, 5));
        spriteMap.add(CONTINUES_0,           RectShort.of(180, 243, 40, 8));
        spriteMap.add(CONTINUES_1,           RectShort.of(180, 234, 40, 8));
        spriteMap.add(CONTINUES_2,           RectShort.of(180, 225, 40, 8));
        spriteMap.add(CONTINUES_3,           RectShort.of(180, 216, 40, 8));
        spriteMap.add(LEVEL_NUMBER_BOX,      RectShort.of(200, 164, 16, 16));
        spriteMap.add(LIVES_COUNTER_SYMBOL,  RectShort.of(241, 15, 16, 16));
        spriteMap.add(DIGIT_1,               RectShort.of(185 + 5,      184, 4, 5));
        spriteMap.add(DIGIT_2,               RectShort.of(185 + 5 *  2, 184, 4, 5));
        spriteMap.add(DIGIT_3,               RectShort.of(185 + 5 *  3, 184, 4, 5));
        spriteMap.add(DIGIT_4,               RectShort.of(185 + 5 *  4, 184, 4, 5));
        spriteMap.add(DIGIT_5,               RectShort.of(185 + 5 *  5, 184, 4, 5));
        spriteMap.add(DIGIT_6,               RectShort.of(185 + 5 *  6, 184, 4, 5));
        spriteMap.add(DIGIT_7,               RectShort.of(185 + 5 *  7, 184, 4, 5));
        spriteMap.add(DIGIT_8,               RectShort.of(185 + 5 *  8, 184, 4, 5));
        spriteMap.add(DIGIT_9,               RectShort.of(185 + 5 *  9, 184, 4, 5));
        spriteMap.add(DIGIT_0,               RectShort.of(185 + 5 * 10, 184, 4, 5));
        spriteMap.add(MS_PAC_FULL,           RectShort.of(32, 15, 15, 15));
        spriteMap.add(MS_PAC_MUNCHING,
            RectShort.of(51, 15, 15, 15), // open
            RectShort.of(66, 15, 15, 15), // wide open
            RectShort.of(51, 15, 15, 15), // open
            RectShort.of(32, 15, 15, 15)  // closed
        );
        spriteMap.add(MS_PAC_MUNCHING_BOOSTER,
            RectShort.of(105, 15, 15, 15), // open
            RectShort.of(120, 15, 15, 15), // wide open
            RectShort.of(105, 15, 15, 15), // open
            RectShort.of(86, 15, 15, 15)   // closed
        );
        spriteMap.add(MS_PAC_WAVING_HAND,
            RectShort.of(140, 10, 20, 20),
            RectShort.of(163, 10, 20, 20)
        );
        spriteMap.add(MS_PAC_TURNING_AWAY,
            RectShort.of(186, 15, 15, 15),
            RectShort.of(205, 20, 8, 8),
            RectShort.of(218, 21, 5, 5)
        );
        spriteMap.add(MR_PAC_MUNCHING,
            RectShort.of(51, 41, 15, 15), // open
            RectShort.of(66, 41, 15, 15), // wide open
            RectShort.of(51, 41, 15, 15), // open
            RectShort.of(32, 41, 15, 15)  // closed
        );
        spriteMap.add(MR_PAC_MUNCHING_BOOSTER,
            RectShort.of(105, 41, 15, 15), // open
            RectShort.of(120, 41, 15, 15), // wide open
            RectShort.of(86, 4, 15, 15),   // closed
            RectShort.of(120, 41, 15, 15)  // wide open
        );
        spriteMap.add(MR_PAC_WAVING_HAND,
            RectShort.of(140, 36, 20, 20),
            RectShort.of(163, 36, 20, 20)
        );
        spriteMap.add(MR_PAC_TURNING_AWAY,
            RectShort.of(186, 42, 15, 15),
            RectShort.of(205, 46, 8, 8),
            RectShort.of(218, 47, 5, 5)
        );
        spriteMap.add(RED_GHOST_RIGHT,
            RectShort.of(10, 120, 14, 13),
            RectShort.of(26, 120, 14, 13)
        );
        spriteMap.add(RED_GHOST_LEFT,
            RectShort.of(42, 120, 14, 13),
            RectShort.of(58, 120, 14, 13)
        );
        spriteMap.add(RED_GHOST_UP,
            RectShort.of(74, 120, 14, 13),
            RectShort.of(90, 120, 14, 13)
        );
        spriteMap.add(RED_GHOST_DOWN,
            RectShort.of(106, 120, 14, 13),
            RectShort.of(122, 120, 14, 13)
        );
        spriteMap.add(PINK_GHOST_RIGHT,
            RectShort.of(10, 135, 14, 13),
            RectShort.of(26, 135, 14, 13)
        );
        spriteMap.add(PINK_GHOST_LEFT,
            RectShort.of(42, 135, 14, 13),
            RectShort.of(58, 135, 14, 13)
        );
        spriteMap.add(PINK_GHOST_UP,
            RectShort.of(74, 135, 14, 13),
            RectShort.of(90, 135, 14, 13)
        );
        spriteMap.add(PINK_GHOST_DOWN,
            RectShort.of(106, 135, 14, 13),
            RectShort.of(122, 135, 14, 13)
        );
        spriteMap.add(CYAN_GHOST_RIGHT,
            RectShort.of(10, 150, 14, 13),
            RectShort.of(26, 150, 14, 13)
        );
        spriteMap.add(CYAN_GHOST_LEFT,
            RectShort.of(42, 150, 14, 13),
            RectShort.of(58, 150, 14, 13)
        );
        spriteMap.add(CYAN_GHOST_UP,
            RectShort.of(74, 150, 14, 13),
            RectShort.of(90, 150, 14, 13)
        );
        spriteMap.add(CYAN_GHOST_DOWN,
            RectShort.of(106, 150, 14, 13),
            RectShort.of(122, 150, 14, 13)
        );
        spriteMap.add(ORANGE_GHOST_RIGHT,
            RectShort.of(10, 165, 14, 13),
            RectShort.of(26, 165, 14, 13)
        );
        spriteMap.add(ORANGE_GHOST_LEFT,
            RectShort.of(42, 165, 14, 13),
            RectShort.of(58, 165, 14, 13)
        );
        spriteMap.add(ORANGE_GHOST_UP,
            RectShort.of(74, 165, 14, 13),
            RectShort.of(90, 165, 14, 13)
        );
        spriteMap.add(ORANGE_GHOST_DOWN,
            RectShort.of(106, 165, 14, 13),
            RectShort.of(122, 165, 14, 13)
        );
        spriteMap.add(GHOST_FRIGHTENED,
            RectShort.of(138, 120, 14, 13),
            RectShort.of(154, 120, 14, 13)
        );
        spriteMap.add(GHOST_FLASHING,
            //TODO when are the white-red sprites used?
            //RectShort.of(138, 120, 14, 13), RectShort.of(154, 120, 14, 13), // blue
            //RectShort.of(138, 135, 14, 13),  RectShort.of(154, 135, 14, 13), // white/red eyes+mouth
            RectShort.of(138, 120, 14, 13),
            RectShort.of(154, 120, 14, 13), // blue
            RectShort.of(138, 150, 14, 13),
            RectShort.of(154, 150, 14, 13) // white/blue eyes+mouth
        );
        spriteMap.add(GHOST_NUMBERS,
            RectShort.of(259, 172, 16, 10),
            RectShort.of(279, 172, 16, 10),
            RectShort.of(259, 183, 16, 10),
            RectShort.of(279, 183, 16, 10)
        );

        spriteMap.add(BONUS_SYMBOLS, IntStream.range(0, 14)
            .mapToObj(i -> RectShort.of(BONUS_X[i], 66 + BONUS_DY[i], BONUS_W[i], 20 - BONUS_DY[i]))
            .toArray(RectShort[]::new));

        spriteMap.add(BONUS_VALUES, IntStream.range(0, 14)
            .mapToObj(i -> RectShort.of(BONUS_X[i], 85, BONUS_W[i], 18))
            .toArray(RectShort[]::new));

        spriteMap.add(CLAPPERBOARD,
            // wide-open, open, closed
            RectShort.of(91, 361, 32, 32),
            RectShort.of(53, 361, 32, 32),
            RectShort.of(14, 361, 32, 32)
        );
        spriteMap.add(STORK,
            RectShort.of(157, 355, 33, 16),
            RectShort.of(198, 355, 33, 16)
        );

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

    public RectShort[] ghostNormalSprites(byte personality, Direction dir) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> switch (dir) {
                case Direction.RIGHT -> sprites(SpriteID.RED_GHOST_RIGHT);
                case Direction.LEFT  -> sprites(SpriteID.RED_GHOST_LEFT);
                case Direction.UP    -> sprites(SpriteID.RED_GHOST_UP);
                case Direction.DOWN  -> sprites(SpriteID.RED_GHOST_DOWN);
            };
            case PINK_GHOST_SPEEDY   -> switch (dir) {
                case Direction.RIGHT -> sprites(SpriteID.PINK_GHOST_RIGHT);
                case Direction.LEFT  -> sprites(SpriteID.PINK_GHOST_LEFT);
                case Direction.UP    -> sprites(SpriteID.PINK_GHOST_UP);
                case Direction.DOWN  -> sprites(SpriteID.PINK_GHOST_DOWN);
            };
            case CYAN_GHOST_BASHFUL  -> switch (dir) {
                case Direction.RIGHT -> sprites(SpriteID.CYAN_GHOST_RIGHT);
                case Direction.LEFT  -> sprites(SpriteID.CYAN_GHOST_LEFT);
                case Direction.UP    -> sprites(SpriteID.CYAN_GHOST_UP);
                case Direction.DOWN  -> sprites(SpriteID.CYAN_GHOST_DOWN);
            };
            case ORANGE_GHOST_POKEY  -> switch (dir) {
                case Direction.RIGHT -> sprites(SpriteID.ORANGE_GHOST_RIGHT);
                case Direction.LEFT  -> sprites(SpriteID.ORANGE_GHOST_LEFT);
                case Direction.UP    -> sprites(SpriteID.ORANGE_GHOST_UP);
                case Direction.DOWN  -> sprites(SpriteID.ORANGE_GHOST_DOWN);
            };
            default -> throw new IllegalArgumentException();
        };
    }

    public RectShort ghostEyesSprite(Direction dir) {
        return switch (dir) {
            case RIGHT -> sprite(SpriteID.GHOST_EYES_RIGHT);
            case LEFT  -> sprite(SpriteID.GHOST_EYES_LEFT);
            case UP    -> sprite(SpriteID.GHOST_EYES_UP);
            case DOWN  -> sprite(SpriteID.GHOST_EYES_DOWN);
        };
    }
}