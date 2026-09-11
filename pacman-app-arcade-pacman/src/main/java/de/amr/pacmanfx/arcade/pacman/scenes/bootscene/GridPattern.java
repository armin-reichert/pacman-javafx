package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public record GridPattern(int width, int height) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
