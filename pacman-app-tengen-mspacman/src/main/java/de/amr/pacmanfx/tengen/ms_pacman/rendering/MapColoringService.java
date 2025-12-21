/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
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
            Color.web(fromScheme.fillColorRGB()), Color.web(fromScheme.strokeColorRGB()), Color.web(fromScheme.pelletColorRGB()),
            Color.web(toScheme.fillColorRGB()),   Color.web(toScheme.strokeColorRGB()),   Color.web(toScheme.pelletColorRGB())
        );
    }

    private final Map<CacheKey, ColoredSpriteImage> cache = new WeakHashMap<>();

    private ColoredSpriteImage recoloredMazeImage(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedColorScheme);
        ColoredSpriteImage mazeImage = cache.get(key);
        if (mazeImage == null) {
            mazeImage = new ColoredSpriteImage(
                replaceColors(spriteSheet.image(mazeSprite), originalColorScheme, requestedColorScheme),
                new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                requestedColorScheme);
            cache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedColorScheme, cache.size());
        }
        return mazeImage;
    }

    public ColoredSpriteImage recolor(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort originalMazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme)
    {
        return requestedColorScheme.equals(originalColorScheme)
            ? new ColoredSpriteImage(spriteSheet.sourceImage(), originalMazeSprite, originalColorScheme)
            : recoloredMazeImage(
                mapCategory, mazeID,
                spriteSheet, originalMazeSprite,
                originalColorScheme, requestedColorScheme);
    }

    public List<ColoredSpriteImage> createFlashingMazeList(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount
    ) {
        var flashingMazes = new ArrayList<ColoredSpriteImage>();
        if (multipleFlashColors) {
            List<NES_ColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, requestedColorScheme);
            for (NES_ColorScheme randomColorScheme : randomColorSchemes) {
                ColoredSpriteImage maze = recoloredMazeImage(
                    mapCategory, mazeID,
                    spriteSheet, mazeSprite,
                    originalColorScheme, randomColorScheme
                );
                flashingMazes.add(maze);
            }
        } else {
            ColoredSpriteImage blackWhiteMaze = recoloredMazeImage(
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

    public MazeSpriteSet createMazeSet(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount) {

        final ColoredSpriteImage recoloredMaze = recolor(
            mapCategory, mazeID,
            spriteSheet, mazeSprite,
            originalColorScheme, requestedColorScheme
        );
        final List<ColoredSpriteImage> flashingMazes = createFlashingMazeList(
            mapCategory, mazeID,
            spriteSheet, mazeSprite,
            originalColorScheme, requestedColorScheme,
            multipleFlashColors, flashCount
        );
        return new MazeSpriteSet(recoloredMaze, flashingMazes);
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
