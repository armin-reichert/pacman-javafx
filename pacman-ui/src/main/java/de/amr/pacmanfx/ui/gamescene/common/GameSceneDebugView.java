/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.engine.GameScene;

public record GameSceneDebugView(GameScene gameScene) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.DEBUG;
    }
}
