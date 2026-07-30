/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

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

import static de.amr.basics.math.RectShort.sprite;

/**
 * Recolors maze images from source NES color schemes to target schemes.
 * Generates flashing maze variants (single black/white or multiple random colors)
 * used during energizer periods in the Tengen NES Ms. Pac-Man port.
 */
public class MapColoringService {

    private static class SingletonHolder {
        static MapColoringService SINGLETON = new MapColoringService();
    }

    public static MapColoringService instance() { return SingletonHolder.SINGLETON; }

    private MapColoringService() {}

    public ColorSchemedMapSprite recolorMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_WorldMapColorScheme sourceColorScheme,
        NES_WorldMapColorScheme targetColorScheme)
    {
        return targetColorScheme.equals(sourceColorScheme)
            ? new ColorSchemedMapSprite(spriteSheet.sourceImage(), mapSprite, sourceColorScheme)
            : computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                sourceColorScheme, targetColorScheme);
    }

    public List<ColorSchemedMapSprite> createFlashingMapImages(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_WorldMapColorScheme sourceColorScheme,
        NES_WorldMapColorScheme targetColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final var flashingMapImages = new ArrayList<ColorSchemedMapSprite>();
        if (multipleFlashColors) {
            final List<NES_WorldMapColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, targetColorScheme);
            for (NES_WorldMapColorScheme randomColorScheme : randomColorSchemes) {
                final ColorSchemedMapSprite maze = computeRecoloredMapImage(
                    mapCategory, mapID,
                    spriteSheet, mapSprite,
                    sourceColorScheme, randomColorScheme
                );
                flashingMapImages.add(maze);
            }
        } else {
            final ColorSchemedMapSprite blackWhiteMapImage = computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                sourceColorScheme, NES_WorldMapColorScheme._0F_20_0F_BLACK_WHITE_BLACK
            );
            for (int i = 0; i < flashCount; ++i) {
                flashingMapImages.add(blackWhiteMapImage);
            }
        }
        return flashingMapImages;
    }

    public MapImageSet createMapImageSet(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_WorldMapColorScheme sourceColorScheme,
        NES_WorldMapColorScheme targetColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final ColorSchemedMapSprite recoloredMapImage = recolorMapImage(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            sourceColorScheme, targetColorScheme
        );
        final List<ColorSchemedMapSprite> flashingMapImages = createFlashingMapImages(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            sourceColorScheme, targetColorScheme,
            multipleFlashColors, flashCount
        );
        return new MapImageSet(recoloredMapImage, flashingMapImages);
    }

    private ColorSchemedMapSprite computeRecoloredMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_WorldMapColorScheme sourceColorScheme,
        NES_WorldMapColorScheme targetColorScheme)
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
        final var coloredMapImage = new ColorSchemedMapSprite(
            recoloredMapImage,
            sprite(0, 0, mapSprite.width(), mapSprite.height()),
            targetColorScheme);
        Logger.info("{} map ({}) recolored to {}", mapCategory, mapID, targetColorScheme);
        return coloredMapImage;
    }

    private List<NES_WorldMapColorScheme> randomColorSchemesOtherThan(int count, NES_WorldMapColorScheme colorScheme) {
        final var randomColorSchemes = new HashSet<NES_WorldMapColorScheme>();
        while (randomColorSchemes.size() < count) {
            final NES_WorldMapColorScheme randomColorScheme = NES_WorldMapColorScheme.randomScheme();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }
}
