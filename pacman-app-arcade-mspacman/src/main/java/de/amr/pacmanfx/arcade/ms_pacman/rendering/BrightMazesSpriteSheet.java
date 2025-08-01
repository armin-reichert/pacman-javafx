/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.image.Image;

import java.util.Map;

import static de.amr.pacmanfx.lib.RectShort.rect;
import static java.util.Objects.requireNonNull;

public record BrightMazesSpriteSheet(Image sourceImage) implements SpriteSheet<String> {

    public static final String BRIGHT_MAZES_ID = "BRIGHT_MAZES";

    private static final Map<String, Object> SPRITE_MAP = Map.of(
        BRIGHT_MAZES_ID, new RectShort[] {
            rect(0,       0, 224, 248),
            rect(0,     248, 224, 248),
            rect(0, 2 * 248, 224, 248),
            rect(0, 3 * 248, 224, 248),
            rect(0, 4 * 248, 224, 248),
            rect(0, 5 * 248, 224, 248)
        }
    );

    public BrightMazesSpriteSheet {
        requireNonNull(sourceImage);
    }

    @Override
    public Image sourceImage() {
        return sourceImage;
    }

    @Override
    public Map<String, Object> spriteMap() {
        return SPRITE_MAP;
    }
}