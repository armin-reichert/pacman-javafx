package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

public class BlankCanvas implements Renderable {
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
