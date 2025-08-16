/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.lib.RectShort.rect;
import static java.util.Objects.requireNonNull;

public record BrightMazesSpriteSheet(Image sourceImage) implements SpriteSheet<BrightMazesSpriteSheet.SpriteID> {

    public enum SpriteID { BRIGHT_MAZES }

    private static final SpriteMap<SpriteID> SPRITE_MAP = new SpriteMap<>(SpriteID.class);

    static {
        SPRITE_MAP.add(SpriteID.BRIGHT_MAZES,
            rect(0, 0, 224, 248),
            rect(0, 248, 224, 248),
            rect(0, 2 * 248, 224, 248),
            rect(0, 3 * 248, 224, 248),
            rect(0, 4 * 248, 224, 248),
            rect(0, 5 * 248, 224, 248)
        );
    }
    public BrightMazesSpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public Image sourceImage() {
        return sourceImage;
    }

    @Override
    public RectShort sprite(SpriteID id) {
        return SPRITE_MAP.sprite(id);
    }

    @Override
    public RectShort[] spriteSequence(SpriteID id) {
        return SPRITE_MAP.spriteSequence(id);
    }
}