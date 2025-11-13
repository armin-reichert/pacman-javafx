/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class BaseSpriteRenderer extends BaseRenderer implements SpriteRenderer {

    private final SpriteSheet<?> spriteSheet;

    public BaseSpriteRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(requireNonNull(canvas));
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }
}
