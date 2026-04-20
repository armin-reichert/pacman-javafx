/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Recolors maze images from source NES color schemes to target schemes.
 * Generates flashing maze variants (single black/white or multiple random colors)
 * used during energizer periods in the Tengen NES Ms. Pac-Man port.
 */
public class MapColoringService {

    private static class LazyThreadSafeSingletonHolder {
        static MapColoringService SINGLETON = new MapColoringService();
    }

    public static MapColoringService instance() { return LazyThreadSafeSingletonHolder.SINGLETON; }

    private MapColoringService() {}

    public ColorSchemedImage recolorMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_MapColorScheme sourceColorScheme,
        NES_MapColorScheme targetColorScheme)
    {
        return targetColorScheme.equals(sourceColorScheme)
            ? new ColorSchemedImage(spriteSheet.sourceImage(), mapSprite, sourceColorScheme)
            : computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                sourceColorScheme, targetColorScheme);
    }

    public List<ColorSchemedImage> createFlashingMapImages(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_MapColorScheme sourceColorScheme,
        NES_MapColorScheme targetColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final var flashingMapImages = new ArrayList<ColorSchemedImage>();
        if (multipleFlashColors) {
            final List<NES_MapColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, targetColorScheme);
            for (NES_MapColorScheme randomColorScheme : randomColorSchemes) {
                final ColorSchemedImage maze = computeRecoloredMapImage(
                    mapCategory, mapID,
                    spriteSheet, mapSprite,
                    sourceColorScheme, randomColorScheme
                );
                flashingMapImages.add(maze);
            }
        } else {
            final ColorSchemedImage blackWhiteMapImage = computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                sourceColorScheme, NES_MapColorScheme._0F_20_0F_BLACK_WHITE_BLACK
            );
            for (int i = 0; i < flashCount; ++i) {
                flashingMapImages.add(blackWhiteMapImage);
            }
        }
        return flashingMapImages;
    }

    public MapImageSet createMazeSet(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_MapColorScheme sourceColorScheme,
        NES_MapColorScheme targetColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final ColorSchemedImage recoloredMapImage = recolorMapImage(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            sourceColorScheme, targetColorScheme
        );
        final List<ColorSchemedImage> flashingMapImages = createFlashingMapImages(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            sourceColorScheme, targetColorScheme,
            multipleFlashColors, flashCount
        );
        return new MapImageSet(recoloredMapImage, flashingMapImages);
    }

    private ColorSchemedImage computeRecoloredMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_MapColorScheme sourceColorScheme,
        NES_MapColorScheme targetColorScheme)
    {
        // Make copy of spritesheet image region
        final Image existingMapImage = spriteSheet.image(mapSprite);
        final Image recoloredMapImage = UfxImages.replaceImageColors(
            existingMapImage,
            Color.valueOf(sourceColorScheme.wallFill()),
            Color.valueOf(sourceColorScheme.wallStroke()),
            Color.valueOf(sourceColorScheme.pellet()),
            Color.valueOf(targetColorScheme.wallFill()),
            Color.valueOf(targetColorScheme.wallStroke()),
            Color.valueOf(targetColorScheme.pellet())
        );
        final var coloredMapImage = new ColorSchemedImage(
            recoloredMapImage,
            RectShort.of(0, 0, mapSprite.width(), mapSprite.height()),
            targetColorScheme);
        Logger.info("{} map ({}) recolored to {}", mapCategory, mapID, targetColorScheme);
        return coloredMapImage;
    }

    private List<NES_MapColorScheme> randomColorSchemesOtherThan(int count, NES_MapColorScheme colorScheme) {
        final var randomColorSchemes = new HashSet<NES_MapColorScheme>();
        while (randomColorSchemes.size() < count) {
            final NES_MapColorScheme randomColorScheme = NES_MapColorScheme.randomScheme();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }
}
