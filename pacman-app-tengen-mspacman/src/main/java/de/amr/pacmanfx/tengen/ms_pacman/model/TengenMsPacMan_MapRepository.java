/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ArcadeMapsSpriteSheet;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColoredMazeSpriteSet;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.NonArcadeMapsSpriteSheet;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.RecoloredSpriteImage;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.uilib.Ufx.exchangeNES_ColorScheme;
import static java.util.Objects.requireNonNull;

/**
 * API to access the maze images stored in files {@code non_arcade_mazes.png} and {@code arcade_mazes.png}.
 * These files contain the images for all mazes used in the different map categories, but only in the colors
 * used by the STRANGE maps through levels 1-32 (for levels 28-31, random color schemes are used.)
 * <p>The MINI and BIG maps use different color schemes.
 * <p>Because the map images do not cover all required map/color-scheme combinations, an image cache is provided where
 * the recolored maze images are stored.
 */
public class TengenMsPacMan_MapRepository implements Disposable {

    private record CacheKey(MapCategory mapCategory, Object mazeID, NES_ColorScheme colorScheme) {}

    private static NES_ColorScheme colorSchemeFromNonArcadeMapsSpriteSheet(NonArcadeMapsSpriteSheet.MazeID mazeID){
        return switch (mazeID) {
            case MAZE1  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE2  -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case MAZE3  -> NES_ColorScheme._16_20_15_ORANGE_WHITE_RED;
            case MAZE4  -> NES_ColorScheme._01_38_20_BLUE_YELLOW_WHITE;
            case MAZE5  -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case MAZE6  -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case MAZE7  -> NES_ColorScheme._17_20_20_BROWN_WHITE_WHITE;
            case MAZE8  -> NES_ColorScheme._13_20_28_VIOLET_WHITE_YELLOW;
            case MAZE9  -> NES_ColorScheme._0F_20_28_BLACK_WHITE_YELLOW;
            case MAZE10_BIG -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case MAZE11 -> NES_ColorScheme._14_25_20_VIOLET_ROSE_WHITE;
            case MAZE12 -> NES_ColorScheme._15_20_20_RED_WHITE_WHITE;
            case MAZE13 -> NES_ColorScheme._1B_20_20_GREEN_WHITE_WHITE;
            case MAZE14_BIG -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case MAZE15 -> NES_ColorScheme._1A_20_28_GREEN_WHITE_YELLOW;
            case MAZE16_MINI -> NES_ColorScheme._18_20_20_KHAKI_WHITE_WHITE;
            case MAZE17_BIG -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case MAZE18 -> NES_ColorScheme._12_20_28_BLUE_WHITE_YELLOW;
            case MAZE19_BIG -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case MAZE20_BIG -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case MAZE21_BIG -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case MAZE22_BIG -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case MAZE23_BIG -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case MAZE24 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            case MAZE25_BIG -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case MAZE26_BIG -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case MAZE27 -> NES_ColorScheme._04_20_20_VIOLET_WHITE_WHITE;
            case MAZE28_MINI -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case MAZE29 -> NES_ColorScheme._21_35_20_BLUE_PINK_WHITE;
            case MAZE30_MINI -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case MAZE31 -> NES_ColorScheme._12_16_20_BLUE_RED_WHITE;
            case MAZE32_ANIMATED -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            default -> throw new IllegalArgumentException("Illegal non-Arcade maze ID: " + mazeID);
        };
    }

    private static List<NES_ColorScheme> randomColorSchemes(int count, NES_ColorScheme colorScheme) {
        var randomColorSchemes = new HashSet<NES_ColorScheme>();
        while (randomColorSchemes.size() < count) {
            NES_ColorScheme randomColorScheme = NES_ColorScheme.randomScheme();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }

    private Map<CacheKey, RecoloredSpriteImage> recoloredMazeImageCache = new WeakHashMap<>();
    private ArcadeMapsSpriteSheet arcadeMazesSpriteSheet;
    private NonArcadeMapsSpriteSheet nonArcadeMazesSpriteSheet;

    public TengenMsPacMan_MapRepository(ArcadeMapsSpriteSheet arcadeMapsSpriteSheet, NonArcadeMapsSpriteSheet nonArcadeMapsSpriteSheet) {
        this.arcadeMazesSpriteSheet = requireNonNull(arcadeMapsSpriteSheet);
        this.nonArcadeMazesSpriteSheet = requireNonNull(nonArcadeMapsSpriteSheet);
    }

    @Override
    public void dispose() {
        if (recoloredMazeImageCache != null) {
            recoloredMazeImageCache.clear();
            recoloredMazeImageCache = null;
        }
        if (arcadeMazesSpriteSheet != null) {
            arcadeMazesSpriteSheet = null;
        }
        if (nonArcadeMazesSpriteSheet != null) {
            nonArcadeMazesSpriteSheet = null;
        }
    }

    public ColoredMazeSpriteSet createMazeSpriteSet(WorldMap worldMap, int flashCount) {
        MapCategory mapCategory = worldMap.getConfigValue("mapCategory");
        int mapNumber = worldMap.getConfigValue("mapNumber");
        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
        // for randomly colored maps (levels 28-31, non-ARCADE map categories), multiple flash colors appear
        boolean multipleFlashColors = worldMap.getConfigValue("multipleFlashColors");
        return switch (mapCategory) {
            case ARCADE  -> arcadeMazeSpriteSet(mapNumber, nesColorScheme, flashCount);
            case MINI    -> miniMazeSpriteSet(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case BIG     -> bigMazeSpriteSet(mapNumber, nesColorScheme, flashCount, multipleFlashColors);
            case STRANGE -> strangeMazeSpriteSet(
                worldMap.getConfigValue("mazeID"), // set by map selector!
                multipleFlashColors ? worldMap.getConfigValue("nesColorScheme") : null,
                flashCount,
                multipleFlashColors);
        };
    }

    private ColoredMazeSpriteSet arcadeMazeSpriteSet(int mapNumber, NES_ColorScheme colorScheme, int flashCount) {
        ArcadeMapsSpriteSheet.MazeID id = switch (mapNumber) {
            case 1 -> ArcadeMapsSpriteSheet.MazeID.MAZE1;
            case 2 -> ArcadeMapsSpriteSheet.MazeID.MAZE2;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> ArcadeMapsSpriteSheet.MazeID.MAZE3;
                case _35_28_20_PINK_YELLOW_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE5;
                case _17_20_20_BROWN_WHITE_WHITE  -> ArcadeMapsSpriteSheet.MazeID.MAZE7;
                case _0F_20_28_BLACK_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE9;
                default -> throw new IllegalArgumentException("No maze image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> ArcadeMapsSpriteSheet.MazeID.MAZE4;
                case _36_15_20_PINK_RED_WHITE      -> ArcadeMapsSpriteSheet.MazeID.MAZE6;
                case _13_20_28_VIOLET_WHITE_YELLOW -> ArcadeMapsSpriteSheet.MazeID.MAZE8;
                default -> throw new IllegalArgumentException("No maze image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };
        RectShort originalMazeSprite = arcadeMazesSpriteSheet.sprite(id);
        var mazeSprite = new RecoloredSpriteImage(arcadeMazesSpriteSheet.sourceImage(), arcadeMazesSpriteSheet.sprite(id), colorScheme);
        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        //TODO: Handle case when color scheme is already black & white
        RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(
            MapCategory.ARCADE, mapNumber, originalMazeSprite, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorScheme);
        for (int i = 0; i < flashCount; ++i) {
            flashingMazeSprites.add(blackWhiteMazeSprite);
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet miniMazeSpriteSet(
        int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors)
    {
        NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case 1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE34_MINI;
            case 2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE35_MINI;
            case 3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE36_MINI;
            case 4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE30_MINI;
            case 5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE28_MINI;
            case 6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE37_MINI;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        NES_ColorScheme availableColorScheme = switch (mapNumber) {
            case 1 -> NES_ColorScheme._36_15_20_PINK_RED_WHITE;
            case 2 -> NES_ColorScheme._21_20_28_BLUE_WHITE_YELLOW;
            case 3 -> NES_ColorScheme._35_28_20_PINK_YELLOW_WHITE;
            case 4 -> NES_ColorScheme._28_16_20_YELLOW_RED_WHITE;
            case 5 -> NES_ColorScheme._00_2A_24_GRAY_GREEN_PINK;
            case 6 -> NES_ColorScheme._23_20_2B_VIOLET_WHITE_GREEN;
            default -> null;
        };
        RectShort originalMazeSprite = nonArcadeMazesSpriteSheet.sprite(mazeID);
        RecoloredSpriteImage mazeSprite = colorScheme.equals(availableColorScheme)
            ? new RecoloredSpriteImage(nonArcadeMazesSpriteSheet.sourceImage(), originalMazeSprite, colorScheme)
            : recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite, colorScheme, availableColorScheme);

        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemes(flashCount, colorScheme)) {
                RecoloredSpriteImage randomMazeSprite = recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite,
                    randomScheme, availableColorScheme);
                flashingMazeSprites.add(randomMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.MINI, mazeID, originalMazeSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, availableColorScheme);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet bigMazeSpriteSet(
        int mapNumber, NES_ColorScheme colorScheme, int flashCount, boolean multipleFlashColors) {
        NonArcadeMapsSpriteSheet.MazeID mazeID = switch (mapNumber) {
            case  1 -> NonArcadeMapsSpriteSheet.MazeID.MAZE19_BIG;
            case  2 -> NonArcadeMapsSpriteSheet.MazeID.MAZE20_BIG;
            case  3 -> NonArcadeMapsSpriteSheet.MazeID.MAZE21_BIG;
            case  4 -> NonArcadeMapsSpriteSheet.MazeID.MAZE22_BIG;
            case  5 -> NonArcadeMapsSpriteSheet.MazeID.MAZE23_BIG;
            case  6 -> NonArcadeMapsSpriteSheet.MazeID.MAZE17_BIG;
            case  7 -> NonArcadeMapsSpriteSheet.MazeID.MAZE10_BIG;
            case  8 -> NonArcadeMapsSpriteSheet.MazeID.MAZE14_BIG;
            case  9 -> NonArcadeMapsSpriteSheet.MazeID.MAZE26_BIG;
            case 10 -> NonArcadeMapsSpriteSheet.MazeID.MAZE25_BIG;
            case 11 -> NonArcadeMapsSpriteSheet.MazeID.MAZE33_BIG;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };
        NES_ColorScheme colorSchemeInSpriteSheet = switch (mapNumber) {
            case  1 -> NES_ColorScheme._07_20_20_BROWN_WHITE_WHITE;
            case  2 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            case  3 -> NES_ColorScheme._0F_20_1C_BLACK_WHITE_GREEN;
            case  4 -> NES_ColorScheme._19_20_20_GREEN_WHITE_WHITE;
            case  5 -> NES_ColorScheme._0C_20_14_GREEN_WHITE_VIOLET;
            case  6 -> NES_ColorScheme._25_20_20_ROSE_WHITE_WHITE;
            case  7 -> NES_ColorScheme._0F_01_20_BLACK_BLUE_WHITE;
            case  8 -> NES_ColorScheme._28_20_2A_YELLOW_WHITE_GREEN;
            case  9 -> NES_ColorScheme._03_20_20_BLUE_WHITE_WHITE;
            case 10 -> NES_ColorScheme._10_20_28_GRAY_WHITE_YELLOW;
            case 11 -> NES_ColorScheme._15_25_20_RED_ROSE_WHITE;
            default -> null;
        };
        RectShort originalMazeSprite = nonArcadeMazesSpriteSheet.sprite(mazeID);
        RecoloredSpriteImage mazeSprite = colorScheme.equals(colorSchemeInSpriteSheet)
            ? new RecoloredSpriteImage(nonArcadeMazesSpriteSheet.sourceImage(), originalMazeSprite, colorScheme)
            : recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite, colorScheme, colorSchemeInSpriteSheet);

        var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (var randomScheme : randomColorSchemes(flashCount, colorScheme)) {
                RecoloredSpriteImage randomColorMazeSprite = recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite,
                    randomScheme, colorSchemeInSpriteSheet);
                flashingMazeSprites.add(randomColorMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.BIG, mazeID, originalMazeSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, colorSchemeInSpriteSheet);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private ColoredMazeSpriteSet strangeMazeSpriteSet(
        NonArcadeMapsSpriteSheet.MazeID mazeID, NES_ColorScheme randomColorScheme, int flashCount, boolean multipleFlashColors)
    {
        final RectShort originalMazeSprite = mazeID == NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED
            ? nonArcadeMazesSpriteSheet.spriteSequence(mazeID)[0]
            : nonArcadeMazesSpriteSheet.sprite(mazeID);
        final NES_ColorScheme originalColorScheme = colorSchemeFromNonArcadeMapsSpriteSheet(mazeID);
        final NES_ColorScheme requestedColorScheme = randomColorScheme != null ? randomColorScheme : originalColorScheme;
        final RecoloredSpriteImage mazeSprite = requestedColorScheme.equals(originalColorScheme)
            ? new RecoloredSpriteImage(nonArcadeMazesSpriteSheet.sourceImage(), originalMazeSprite, originalColorScheme)
            : recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite, requestedColorScheme, originalColorScheme);

        final var flashingMazeSprites = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            for (NES_ColorScheme colorScheme : randomColorSchemes(flashCount, requestedColorScheme)) {
                RecoloredSpriteImage randomColorMazeSprite = recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite,
                    colorScheme, originalColorScheme);
                flashingMazeSprites.add(randomColorMazeSprite);
            }
        } else {
            RecoloredSpriteImage blackWhiteMazeSprite = recoloredMazeImage(MapCategory.STRANGE, mazeID, originalMazeSprite,
                NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK, originalColorScheme);
            for (int i = 0; i < flashCount; ++i) {
                flashingMazeSprites.add(blackWhiteMazeSprite);
            }
        }
        return new ColoredMazeSpriteSet(mazeSprite, flashingMazeSprites);
    }

    private RecoloredSpriteImage recoloredMazeImage(
        MapCategory mapCategory, Object mazeID, RectShort mazeSprite,
        NES_ColorScheme requestedScheme, NES_ColorScheme existingScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedScheme);
        RecoloredSpriteImage mazeImage = recoloredMazeImageCache.get(key);
        if (mazeImage == null) {
            SpriteSheet<?> spriteSheet = mapCategory == MapCategory.ARCADE ? arcadeMazesSpriteSheet : nonArcadeMazesSpriteSheet;
            mazeImage = new RecoloredSpriteImage(
                exchangeNES_ColorScheme(spriteSheet.image(mazeSprite), existingScheme, requestedScheme),
                new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                requestedScheme);
            recoloredMazeImageCache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedScheme, recoloredMazeImageCache.size());
        }
        return mazeImage;
    }
}