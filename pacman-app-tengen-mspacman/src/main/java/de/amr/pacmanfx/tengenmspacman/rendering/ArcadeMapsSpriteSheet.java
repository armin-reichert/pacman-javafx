/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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

    public static final ArcadeMapsSpriteSheet INSTANCE = new ArcadeMapsSpriteSheet();

    // Size of Arcade maze (without the 3 empty rows above and the 2 below the maze!)
    private static final int MAP_SPRITE_WIDTH  = 28 * TS;
    private static final int MAP_SPRITE_HEIGHT = 31 * TS;

    public enum MapID {
        MAP1, MAP2, MAP3, MAP4, MAP5, MAP6, MAP7, MAP8, MAP9
    }

    private static final SpriteMap<MapID> SPRITE_MAP = new SpriteMap<>(MapID.class);

    private static RectShort spriteAtCell(int row, int col) {
        return new RectShort(col * MAP_SPRITE_WIDTH, row * MAP_SPRITE_HEIGHT, MAP_SPRITE_WIDTH, MAP_SPRITE_HEIGHT);
    }

    static {
        SPRITE_MAP.add(MapID.MAP1, spriteAtCell(0, 0));
        SPRITE_MAP.add(MapID.MAP2, spriteAtCell(0, 1));
        SPRITE_MAP.add(MapID.MAP3, spriteAtCell(0, 2));
        SPRITE_MAP.add(MapID.MAP4, spriteAtCell(1, 0));
        SPRITE_MAP.add(MapID.MAP5, spriteAtCell(1, 1));
        SPRITE_MAP.add(MapID.MAP6, spriteAtCell(1, 2));
        SPRITE_MAP.add(MapID.MAP7, spriteAtCell(2, 0));
        SPRITE_MAP.add(MapID.MAP8, spriteAtCell(2, 1));
        SPRITE_MAP.add(MapID.MAP9, spriteAtCell(2, 2));

        SPRITE_MAP.checkCompleteness();
    }

    private static final ResourceManager LOCAL_RESOURCES = () -> TengenMsPacMan_UIConfig.class;

    private static final Image IMAGE = LOCAL_RESOURCES.loadImage(TengenMsPacMan_UIConfig.ARCADE_MAPS_IMAGE_PATH);

    private ArcadeMapsSpriteSheet() {}

    @Override
    public Image sourceImage() {
        return IMAGE;
    }

    @Override
    public RectShort sprite(MapID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] sprites(MapID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}