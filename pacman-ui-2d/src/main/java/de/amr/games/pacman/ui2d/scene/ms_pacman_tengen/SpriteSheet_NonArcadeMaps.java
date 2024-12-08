/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.RectArea.rect;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;

class SpriteSheet_NonArcadeMaps {

    private record CacheKey(int spriteNumber, NES_ColorScheme colorScheme) {}

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    static final RectArea[] STRANGE_MAP_15_SPRITES = {
        rect(1568,  840, 224, 248),
        rect(1568, 1088, 224, 248),
        rect(1568, 1336, 224, 248),
    };

    // Pattern (0^8 1^8 2^8 1^8)+
    public static RectArea strangeMap15Sprite(long tick) {
        // numFrames = 4, frameDuration = 8
        int index = (int) ((tick % 32) / 8);
        // (0, 1, 2, 3) -> (0, 1, 2, 1)
        if (index == 3) index = 1;
        return STRANGE_MAP_15_SPRITES[index];
    }

    // Map row counts as they appear in the sprite sheet (row by row)
    static final byte[] MAP_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    static final RectArea BLINKY_AREA = new RectArea(105, 85, 14, 13);
    static final RectArea OTHER_GHOSTS_AREA = new RectArea(89, 113, 46, 13);
    static final RectArea MS_PAC_AREA = new RectArea(106, 180, 13, 15);

    private static final Map<CacheKey, ImageAreaWithColorScheme> MINI_MAP_IMAGE_CACHE = new HashMap<>();
    private static final Map<CacheKey, ImageAreaWithColorScheme> BIG_MAP_IMAGE_CACHE = new HashMap<>();

    private final Image sourceImage;


    public SpriteSheet_NonArcadeMaps(AssetStorage assets) {
        sourceImage = assets.image(GameAssets2D.PFX_MS_PACMAN_TENGEN + ".mazes.non_arcade");
    }

    private RectArea spriteArea(int spriteNumber) {
        int colIndex, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { colIndex = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { colIndex = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { colIndex = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { colIndex = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { colIndex = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        int width = 28 * TS, height = MAP_ROW_COUNTS[spriteNumber - 1] * TS;
        return new RectArea(colIndex * width, y, width, height);
    }

    public ImageAreaWithColorScheme miniMapSprite(int mapNumber, NES_ColorScheme colorScheme) {
        int spriteNumber = switch (mapNumber) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case 1 -> _36_15_20_PINK_RED_WHITE;
            case 2 -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> _35_28_20_PINK_YELLOW_WHITE;
            case 4 -> _28_16_20_YELLOW_RED_WHITE;
            case 5 -> _00_2A_24_GRAY_GREEN_PINK;
            case 6 -> _23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };

        Logger.debug("Get map #{} with color scheme {}", mapNumber, colorScheme);
        return colorScheme.equals(availableColorScheme)
            ? new ImageAreaWithColorScheme(sourceImage, spriteArea(spriteNumber), colorScheme)
            : getOrCreateMapImage(MINI_MAP_IMAGE_CACHE, spriteNumber, colorScheme, availableColorScheme);
    }

    private ImageAreaWithColorScheme getOrCreateMapImage(
            Map<CacheKey, ImageAreaWithColorScheme> cache,
            int spriteNumber,
            NES_ColorScheme requiredColorScheme,
            NES_ColorScheme availableColorScheme) {

        var cacheKey = new CacheKey(spriteNumber, requiredColorScheme);
        if (!cache.containsKey(cacheKey)) {
            RectArea spriteArea = spriteArea(spriteNumber);
            Image availableMapImage = Ufx.subImage(sourceImage, spriteArea);
            Color black = Color.valueOf(NES.Palette.color(0x0f));
            availableMapImage = Ufx.maskImage(availableMapImage,
                    (x, y) -> BLINKY_AREA.contains(x, y)  || OTHER_GHOSTS_AREA.contains(x, y) || MS_PAC_AREA.contains(x, y),
                    black);
            Image recoloredImage = Ufx.exchange_NESColorScheme(availableMapImage, availableColorScheme, requiredColorScheme);
            var cacheValue = new ImageAreaWithColorScheme(recoloredImage,
                    new RectArea(0, 0, spriteArea.width(), spriteArea.height()),
                    requiredColorScheme);
            cache.put(cacheKey, cacheValue);
            Logger.info("Map image recolored to {} and put into cache. Cache size: {}", requiredColorScheme, MINI_MAP_IMAGE_CACHE.size());
        } else {
            Logger.debug("Map image found in cache");
        }
        return cache.get(cacheKey);
    }

    public ImageAreaWithColorScheme bigMapSprite(int mapNumber, NES_ColorScheme colorScheme) {
        int spriteNumber = switch (mapNumber) {
            case  1 -> 19;
            case  2 -> 20;
            case  3 -> 21;
            case  4 -> 22;
            case  5 -> 23;
            case  6 -> 17;
            case  7 -> 10;
            case  8 -> 14;
            case  9 -> 26;
            case 10 -> 25;
            case 11 -> 33;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case  1 -> _07_20_20_BROWN_WHITE_WHITE;
            case  2 -> _15_25_20_RED_ROSE_WHITE;
            case  3 -> _0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> _19_20_20_GREEN_WHITE_WHITE;
            case  5 -> _0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> _25_20_20_ROSE_WHITE_WHITE;
            case  7 -> _0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> _28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> _03_20_20_VIOLET_WHITE_WHITE;
            case 10 -> _10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> _15_25_20_RED_ROSE_WHITE;
            default -> null;
        };

        Logger.debug("Get map #{} with color scheme {}", mapNumber, colorScheme);
        return colorScheme.equals(availableColorScheme)
            ? new ImageAreaWithColorScheme(sourceImage, spriteArea(spriteNumber), colorScheme)
            : getOrCreateMapImage(MINI_MAP_IMAGE_CACHE, spriteNumber, colorScheme, availableColorScheme);
    }

    public ImageAreaWithColorScheme strangeMapSprite(int levelNumber) {
        NES_ColorScheme colorScheme = switch (levelNumber) {
            case 1  -> _36_15_20_PINK_RED_WHITE;
            case 2  -> _21_20_28_BLUE_WHITE_YELLOW;
            case 3  ->  _16_20_15_ORANGE_WHITE_RED;
            case 4  -> _01_38_20_BLUE_YELLOW_WHITE;
            case 5  -> _35_28_20_PINK_YELLOW_WHITE;
            case 6  -> _36_15_20_PINK_RED_WHITE;
            case 7  -> _17_20_20_BROWN_WHITE_WHITE;
            case 8  -> _13_20_28_VIOLET_WHITE_YELLOW;
            case 9  -> _0F_20_28_BLACK_WHITE_YELLOW;
            case 10 -> _0F_01_20_BLACK_BLUE_WHITE;
            case 11 -> _14_25_20_VIOLET_ROSE_WHITE;
            case 12 -> _15_20_20_RED_WHITE_WHITE;
            case 13 -> _1B_20_20_GREEN_WHITE_WHITE;
            case 14 -> _28_20_2A_YELLOW_WHITE_GREEN;
            case 15 -> _1A_20_28_GREEN_WHITE_YELLOW;
            case 16 -> _18_20_20_KHAKI_WHITE_WHITE;
            case 17 -> _25_20_20_ROSE_WHITE_WHITE;
            case 18 -> _12_20_28_BLUE_WHITE_YELLOW;
            case 19 -> _07_20_20_BROWN_WHITE_WHITE;
            case 20 -> _15_25_20_RED_ROSE_WHITE;
            case 21 -> _0F_20_1C_BLACK_WHITE_GREEN;
            case 22 -> _19_20_20_GREEN_WHITE_WHITE;
            case 23 -> _0C_20_14_GREEN_WHITE_VIOLET;
            case 24 -> _23_20_2B_VIOLET_WHITE_GREEN;
            case 25 -> _10_20_28_GRAY_WHITE_YELLOW;
            case 26,27 -> _04_20_20_VIOLET_WHITE_WHITE;
            case 28,29,30,31 -> NES_ColorScheme.random();
            case 32 -> _15_25_20_RED_ROSE_WHITE;
            default -> throw new IllegalArgumentException("Illegal level number: " + levelNumber);
        };
        if (Globals.inClosedRange(levelNumber, 28, 31)) {
            //TODO compare also random color schemes with available schemes of corresponding maps
            return new ImageAreaWithColorScheme(sourceImage, spriteArea(levelNumber), null);
        } else {
            return new ImageAreaWithColorScheme(sourceImage, spriteArea(levelNumber), colorScheme);
        }
    }
}