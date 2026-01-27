/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;

public final class ArcadeMapsSpriteSheet implements SpriteSheet<ArcadeMapsSpriteSheet.MapID> {

    private static class Holder {
        static final ArcadeMapsSpriteSheet INSTANCE = new ArcadeMapsSpriteSheet();
    }

    public static ArcadeMapsSpriteSheet instance() {
        return Holder.INSTANCE;
    }

    public enum MapID {
        MAP1, MAP2, MAP3, MAP4, MAP5, MAP6, MAP7, MAP8, MAP9
    }

    // Size of Arcade maze (without the 3 empty rows above and the 2 below the maze!)
    private static final int MAP_SPRITE_WIDTH  = 28 * TS;
    private static final int MAP_SPRITE_HEIGHT = 31 * TS;

    private static RectShort spriteAtCell(int row, int col) {
        return new RectShort(col * MAP_SPRITE_WIDTH, row * MAP_SPRITE_HEIGHT, MAP_SPRITE_WIDTH, MAP_SPRITE_HEIGHT);
    }

    private final Image image;
    private final SpriteMap<MapID> spriteMap = new SpriteMap<>(MapID.class);

    private ArcadeMapsSpriteSheet() {
        final ResourceManager moduleResources = () -> TengenMsPacMan_UIConfig.class;
        image = moduleResources.loadImage(TengenMsPacMan_UIConfig.ARCADE_MAPS_IMAGE_PATH);

        spriteMap.add(MapID.MAP1, spriteAtCell(0, 0));
        spriteMap.add(MapID.MAP2, spriteAtCell(0, 1));
        spriteMap.add(MapID.MAP3, spriteAtCell(0, 2));
        spriteMap.add(MapID.MAP4, spriteAtCell(1, 0));
        spriteMap.add(MapID.MAP5, spriteAtCell(1, 1));
        spriteMap.add(MapID.MAP6, spriteAtCell(1, 2));
        spriteMap.add(MapID.MAP7, spriteAtCell(2, 0));
        spriteMap.add(MapID.MAP8, spriteAtCell(2, 1));
        spriteMap.add(MapID.MAP9, spriteAtCell(2, 2));

        spriteMap.checkCompleteness();
    }

    @Override
    public Image sourceImage() {
        return image;
    }

    @Override
    public RectShort sprite(MapID id) {
        return spriteMap.sprite(id);
    }

    @Override
    public RectShort[] sprites(MapID id) {
        return spriteMap.spriteSequence(id);
    }
}