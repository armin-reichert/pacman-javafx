package de.amr.pacmanfx.arcade.ms_pacman.scenes.startscene;

import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public record StartSceneText(int tileX, int tileY) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
