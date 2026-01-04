/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MapColoringService {

    public ColorSchemedImage recolorMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme srcColorScheme,
        NES_ColorScheme tgtColorScheme)
    {
        return tgtColorScheme.equals(srcColorScheme)
            ? new ColorSchemedImage(spriteSheet.sourceImage(), mapSprite, srcColorScheme)
            : computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                srcColorScheme, tgtColorScheme);
    }

    public List<ColorSchemedImage> createFlashingMapImages(
        MapCategory mapCategory, Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme srcColorScheme,
        NES_ColorScheme tgtColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final var flashingMapImages = new ArrayList<ColorSchemedImage>();
        if (multipleFlashColors) {
            final List<NES_ColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, tgtColorScheme);
            for (NES_ColorScheme randomColorScheme : randomColorSchemes) {
                final ColorSchemedImage maze = computeRecoloredMapImage(
                    mapCategory, mapID,
                    spriteSheet, mapSprite,
                    srcColorScheme, randomColorScheme
                );
                flashingMapImages.add(maze);
            }
        } else {
            final ColorSchemedImage blackWhiteMapImage = computeRecoloredMapImage(
                mapCategory, mapID,
                spriteSheet, mapSprite,
                srcColorScheme, NES_ColorScheme._0F_20_0F_BLACK_WHITE_BLACK
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
        NES_ColorScheme srcColorScheme,
        NES_ColorScheme tgtColorScheme,
        boolean multipleFlashColors,
        int flashCount)
    {
        final ColorSchemedImage recoloredMapImage = recolorMapImage(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            srcColorScheme, tgtColorScheme
        );
        final List<ColorSchemedImage> flashingMapImages = createFlashingMapImages(
            mapCategory, mapID,
            spriteSheet, mapSprite,
            srcColorScheme, tgtColorScheme,
            multipleFlashColors, flashCount
        );
        return new MapImageSet(recoloredMapImage, flashingMapImages);
    }

    private ColorSchemedImage computeRecoloredMapImage(
        MapCategory mapCategory,
        Object mapID,
        SpriteSheet<?> spriteSheet,
        RectShort mapSprite,
        NES_ColorScheme srcColorScheme,
        NES_ColorScheme tgtColorScheme)
    {
        // Make copy of spritesheet image region
        final Image existingMapImage = spriteSheet.image(mapSprite);
        final Image recoloredMapImage = Ufx.replaceImageColors(
            existingMapImage,
            Color.valueOf(srcColorScheme.fillColorRGB()),
            Color.valueOf(srcColorScheme.strokeColorRGB()),
            Color.valueOf(srcColorScheme.pelletColorRGB()),
            Color.valueOf(tgtColorScheme.fillColorRGB()),
            Color.valueOf(tgtColorScheme.strokeColorRGB()),
            Color.valueOf(tgtColorScheme.pelletColorRGB())
        );
        final var coloredMapImage = new ColorSchemedImage(
            recoloredMapImage,
            new RectShort(0, 0, mapSprite.width(), mapSprite.height()),
            tgtColorScheme);
        Logger.info("{} map ({}) recolored to {}", mapCategory, mapID, tgtColorScheme);
        return coloredMapImage;
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
}
