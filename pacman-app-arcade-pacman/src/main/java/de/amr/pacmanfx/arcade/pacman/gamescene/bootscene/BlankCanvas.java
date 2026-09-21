package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.pacmanfx.core.rendering.RenderingLayer;
import de.amr.pacmanfx.core.rendering.Renderable;

public class BlankCanvas implements Renderable {
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
