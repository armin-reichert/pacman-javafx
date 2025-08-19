package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import org.tinylog.Logger;

import java.util.Map;
import java.util.WeakHashMap;

import static de.amr.pacmanfx.uilib.Ufx.exchangeNES_ColorScheme;

public class MapRecoloringService implements Disposable {
    private record CacheKey(MapCategory mapCategory, Object mazeID, NES_ColorScheme colorScheme) {}

    private Map<CacheKey, RecoloredSpriteImage> recoloredMazeImageCache = new WeakHashMap<>();

    public RecoloredSpriteImage recoloredMazeImage(
        MapCategory mapCategory, Object mazeID,
        SpriteSheet<?> spriteSheet,
        RectShort mazeSprite,
        NES_ColorScheme requestedScheme, NES_ColorScheme existingScheme) {

        var key = new CacheKey(mapCategory, mazeID, requestedScheme);
        RecoloredSpriteImage mazeImage = recoloredMazeImageCache.get(key);
        if (mazeImage == null) {
            mazeImage = new RecoloredSpriteImage(
                exchangeNES_ColorScheme(spriteSheet.image(mazeSprite), existingScheme, requestedScheme),
                new RectShort(0, 0, mazeSprite.width(), mazeSprite.height()),
                requestedScheme);
            recoloredMazeImageCache.put(key, mazeImage);
            Logger.info("{} maze ({}) recolored to {}, cache size: {}", mapCategory, mazeID, requestedScheme, recoloredMazeImageCache.size());
        }
        return mazeImage;
    }

    @Override
    public void dispose() {
        if (recoloredMazeImageCache != null) {
            recoloredMazeImageCache.clear();
            recoloredMazeImageCache = null;
        }
    }
}
