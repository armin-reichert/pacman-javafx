/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.lib.RectShort.rect;

public record NonArcadeMapsSpriteSheet(Image sourceImage) implements SpriteSheet<NonArcadeMapsSpriteSheet.NonArcadeMazeID> {

    public enum NonArcadeMazeID {
        MAZE15;
    }

    private static final SpriteMap<NonArcadeMazeID> SPRITE_MAP = new SpriteMap<>(NonArcadeMazeID.class);

    static {
        SPRITE_MAP.add(NonArcadeMazeID.MAZE15,
            rect(1568, 840, 224, 248), rect(1568, 1088, 224, 248), rect(1568, 1336, 224, 248)
        );
    }

    @Override
    public RectShort sprite(NonArcadeMazeID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(NonArcadeMazeID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}
