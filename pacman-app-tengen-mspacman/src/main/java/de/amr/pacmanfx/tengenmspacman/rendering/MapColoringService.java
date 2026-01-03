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

    private ColorSchemedImage recoloredMazeImage(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedColorScheme);
        ColorSchemedImage mazeImage = cache.get(key);
        if (mazeImage == null) {
            mazeImage = new ColorSchemedImage(
                replaceColors(spriteSheet.image(mazeSprite), originalColorScheme, requestedColorScheme),
                new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                requestedColorScheme);
            cache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedColorScheme, cache.size());
        }
        return mazeImage;
    }

    public ColorSchemedImage recolor(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort originalMazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme)
    {
        return requestedColorScheme.equals(originalColorScheme)
            ? new ColorSchemedImage(spriteSheet.sourceImage(), originalMazeSprite, originalColorScheme)
            : recoloredMazeImage(
                mapCategory, mazeID,
                spriteSheet, originalMazeSprite,
                originalColorScheme, requestedColorScheme);
    }

    public List<ColorSchemedImage> createFlashingMazeList(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount
    ) {
        var flashingMazes = new ArrayList<ColorSchemedImage>();
        if (multipleFlashColors) {
            List<NES_ColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, requestedColorScheme);
            for (NES_ColorScheme randomColorScheme : randomColorSchemes) {
                ColorSchemedImage maze = recoloredMazeImage(
                    mapCategory, mazeID,
                    spriteSheet, mazeSprite,
                    originalColorScheme, randomColorScheme
                );
                flashingMazes.add(maze);
            }
        } else {
            ColorSchemedImage blackWhiteMaze = recoloredMazeImage(
                mapCategory, mazeID,
                spriteSheet, mazeSprite,
                originalColorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK
            );
            for (int i = 0; i < flashCount; ++i) {
                flashingMazes.add(blackWhiteMaze);
            }
        }
        return flashingMazes;
    }

    public MapImageSet createMazeSet(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount) {

        final ColorSchemedImage recoloredMaze = recolor(
            mapCategory, mazeID,
            spriteSheet, mazeSprite,
            originalColorScheme, requestedColorScheme
        );
        final List<ColorSchemedImage> flashingMazes = createFlashingMazeList(
            mapCategory, mazeID,
            spriteSheet, mazeSprite,
            originalColorScheme, requestedColorScheme,
            multipleFlashColors, flashCount
        );
        return new MapImageSet(recoloredMaze, flashingMazes);
    }

    private List<NES_ColorScheme> randomColorSchemesOtherThan(int count, NES_ColorScheme colorScheme) {
        var randomColorSchemes = new HashSet<NES_ColorScheme>();
        while (randomColorSchemes.size() < count) {
            NES_ColorScheme randomColorScheme = NES_ColorScheme.randomScheme();
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
