/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import javafx.scene.canvas.Canvas;

public abstract class BaseSpriteRenderer extends BaseRenderer implements SpriteRendererMixin {

    public BaseSpriteRenderer(Canvas canvas) {
        super(canvas);
    }
}
