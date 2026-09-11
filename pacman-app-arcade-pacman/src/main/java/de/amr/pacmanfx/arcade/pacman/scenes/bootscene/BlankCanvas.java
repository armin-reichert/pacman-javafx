package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public class BlankCanvas implements Renderable {
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
