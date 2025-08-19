package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.pacmanfx.uilib.Ufx.exchangeNES_ColorScheme;

public class MapRecoloringService implements Disposable {
    private record CacheKey(MapCategory mapCategory, Object mazeID, NES_ColorScheme colorScheme) {}

    private Map<CacheKey, RecoloredSpriteImage> recoloredMazeImageCache = new WeakHashMap<>();

    private RecoloredSpriteImage recoloredMazeImage(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedColorScheme);
        RecoloredSpriteImage mazeImage = recoloredMazeImageCache.get(key);
        if (mazeImage == null) {
            mazeImage = new RecoloredSpriteImage(
                exchangeNES_ColorScheme(spriteSheet.image(mazeSprite), originalColorScheme, requestedColorScheme),
                new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                requestedColorScheme);
            recoloredMazeImageCache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedColorScheme, recoloredMazeImageCache.size());
        }
        return mazeImage;
    }

    public RecoloredSpriteImage recolor(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort originalMazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme)
    {
        return requestedColorScheme.equals(originalColorScheme)
            ? new RecoloredSpriteImage(spriteSheet.sourceImage(), originalMazeSprite, originalColorScheme)
            : recoloredMazeImage(
                mapCategory, mazeID,
                spriteSheet, originalMazeSprite,
                originalColorScheme, requestedColorScheme);
    }

    public List<RecoloredSpriteImage> recolorFlashingMazes(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme originalColorScheme,
        NES_ColorScheme requestedColorScheme,
        boolean multipleFlashColors,
        int flashCount
    ) {
        var flashingMazes = new ArrayList<RecoloredSpriteImage>();
        if (multipleFlashColors) {
            List<NES_ColorScheme> randomColorSchemes = randomColorSchemesOtherThan(flashCount, requestedColorScheme);
            for (NES_ColorScheme randomColorScheme : randomColorSchemes) {
                RecoloredSpriteImage maze = recoloredMazeImage(
                    mapCategory, mazeID,
                    spriteSheet, mazeSprite,
                    originalColorScheme, randomColorScheme
                );
                flashingMazes.add(maze);
            }
        } else {
            RecoloredSpriteImage blackWhiteMaze = recoloredMazeImage(
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
        if (recoloredMazeImageCache != null) {
            recoloredMazeImageCache.clear();
            recoloredMazeImageCache = null;
        }
    }
}
