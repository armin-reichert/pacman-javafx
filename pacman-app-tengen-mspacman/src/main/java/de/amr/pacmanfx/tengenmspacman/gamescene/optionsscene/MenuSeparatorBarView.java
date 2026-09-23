/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public record MenuSeparatorBarView(
    float width,
    float height,
    Vector2f offset
) implements Renderable {
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }
}
