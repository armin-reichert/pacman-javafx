/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene;

import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

public record StartSceneText(int tileX, int tileY) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
