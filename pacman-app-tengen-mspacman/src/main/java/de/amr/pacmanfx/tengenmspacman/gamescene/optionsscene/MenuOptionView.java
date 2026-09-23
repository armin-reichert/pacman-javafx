/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public record MenuOptionView(
    boolean selected,
    String label,
    String value,
    int separatorTileX,
    Vector2f offset
) implements Renderable
{
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.HUD;
    }
}
