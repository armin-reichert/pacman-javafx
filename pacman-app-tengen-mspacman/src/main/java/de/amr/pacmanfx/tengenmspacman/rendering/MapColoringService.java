/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.*;

public class MapColoringService implements Disposable {

    private record CacheKey(MapCategory mapCategory, Object mazeID, NES_ColorScheme colorScheme) {}

    private static Image replaceColors(Image image, NES_ColorScheme fromScheme, NES_ColorScheme toScheme) {
        return Ufx.replaceImageColors(
            image,
            Color.valueOf(fromScheme.fillColorRGB()), Color.valueOf(fromScheme.strokeColorRGB()), Color.valueOf(fromScheme.pelletColorRGB()),
            Color.valueOf(toScheme.fillColorRGB()),   Color.valueOf(toScheme.strokeColorRGB()),   Color.valueOf(toScheme.pelletColorRGB())
        );
    }

    private final Map<CacheKey, ColorSchemedImage> cache = new WeakHashMap<>();

    private ColorSchemedImage recoloredMapImage(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme) {

        final var key = new CacheKey(mapCategory, mapID, requestedColorScheme);
        ColorSchemedImage mapImage = cache.get(key);
        if (mapImage == null) {
            mapImage = new ColorSchemedImage(
                replaceColors(spriteSheet.image(mapSprite), originalColorScheme, requestedColorScheme),
                new RectShort(0, 0, mapSprite.width(), mapSprite.height()),
                requestedColorScheme);
            cache.put(key, mapImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mapID, requestedColorScheme, cache.size());
        }
        return mapImage;
    }

    public ColorSchemedImage recolor(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort originalMapSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme)
    {
        return requestedColorScheme.equals(originalColorScheme)
            ? new ColorSchemedImage(spriteSheet.sourceImage(), originalMapSprite, originalColorScheme)
            : recoloredMapImage(
                mapCategory, mazeID,
                spriteSheet, originalMapSprite,
                originalColorScheme, requestedColorScheme);
    }

    public List<ColorSchemedImage> createFlashingMapImages(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final var flashingMapImages = new ArrayList<ColorSchemedImage>();
        if (multipleFlashColors) {
            final List<NES_ColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, requestedColorScheme);
            for (NES_ColorScheme randomColorScheme : randomColorSchemes) {
                final ColorSchemedImage maze = recoloredMapImage(
                    mapCategory, mapID,
                    spriteSheet, mapSprite,
                    originalColorScheme, randomColorScheme
                );
                flashingMapImages.add(maze);
            }
        } else {
            final ColorSchemedImage blackWhiteMapImage = recoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                originalColorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK
            );
            for (int i = 0; i < flashCount; ++i) {
                flashingMapImages.add(blackWhiteMapImage);
            }
        }
        return flashingMapImages;
    }

    public MapImageSet createMazeSet(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final ColorSchemedImage recoloredMapImage = recolor(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            originalColorScheme, requestedColorScheme
        );
        final List<ColorSchemedImage> flashingMapImages = createFlashingMapImages(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            originalColorScheme, requestedColorScheme,
            multipleFlashColors, flashCount
        );
        return new MapImageSet(recoloredMapImage, flashingMapImages);
    }

    private List<NES_ColorScheme> randomColorSchemesOtherThan(int count, NES_ColorScheme colorScheme) {
        final var randomColorSchemes = new HashSet<NES_ColorScheme>();
        while (randomColorSchemes.size() < count) {
            final NES_ColorScheme randomColorScheme = NES_ColorScheme.randomScheme();
            if (!randomColorScheme.equals(colorScheme)) {
                randomColorSchemes.add(randomColorScheme);
            }
        }
        return randomColorSchemes.stream().toList();
    }

    @Override
    public void dispose() {
        cache.clear();
    }
}
