/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.Named;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_ResourceManager;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.basics.math.RectShort.sprite;

public final class ArcadeMapsSpriteSheet implements SpriteSheet {

    private static class LazyThreadSafeSingletonHolder {
        static final ArcadeMapsSpriteSheet SINGLETON = new ArcadeMapsSpriteSheet();
    }

    public static ArcadeMapsSpriteSheet instance() {
        return LazyThreadSafeSingletonHolder.SINGLETON;
    }

    public enum MapID implements Named {
        MAP1, MAP2, MAP3, MAP4, MAP5, MAP6, MAP7, MAP8, MAP9
    }

    // Size of Arcade maze (without the 3 empty rows above and the 2 below the maze!)
    private static final int MAP_SPRITE_WIDTH  = 28 * WorldMap.TS;
    private static final int MAP_SPRITE_HEIGHT = 31 * WorldMap.TS;

    private static RectShort spriteAtCell(int row, int col) {
        return sprite(col * MAP_SPRITE_WIDTH, row * MAP_SPRITE_HEIGHT, MAP_SPRITE_WIDTH, MAP_SPRITE_HEIGHT);
    }

    private Image image;

    private final SpriteMap spriteMap = new SpriteMap();

    private ArcadeMapsSpriteSheet() {
        spriteMap.add(MapID.MAP1, spriteAtCell(0, 0));
        spriteMap.add(MapID.MAP2, spriteAtCell(0, 1));
        spriteMap.add(MapID.MAP3, spriteAtCell(0, 2));
        spriteMap.add(MapID.MAP4, spriteAtCell(1, 0));
        spriteMap.add(MapID.MAP5, spriteAtCell(1, 1));
        spriteMap.add(MapID.MAP6, spriteAtCell(1, 2));
        spriteMap.add(MapID.MAP7, spriteAtCell(2, 0));
        spriteMap.add(MapID.MAP8, spriteAtCell(2, 1));
        spriteMap.add(MapID.MAP9, spriteAtCell(2, 2));
    }

    @Override
    public SpriteMap spriteMap() {
        return spriteMap;
    }

    @Override
    public Image sourceImage() {
        if (image == null) {
            image = TengenMsPacMan_ResourceManager.instance().loadImage(TengenMsPacMan_UIConfig.REL_PATH_ARCADE_MAPS_IMAGE);
        }
        return image;
    }
}