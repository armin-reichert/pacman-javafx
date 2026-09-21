/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.rendering;

import de.amr.basics.InfoMap;
import de.amr.basics.rendering.RenderingLayer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.basics.rendering.Renderable;

public record RenderableGameLevel(GameLevel level, InfoMap renderInfo) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.WORLD;
    }
}
