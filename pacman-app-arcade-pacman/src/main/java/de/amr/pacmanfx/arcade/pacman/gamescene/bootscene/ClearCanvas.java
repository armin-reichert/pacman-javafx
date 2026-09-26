package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;

public class ClearCanvas implements Renderable {
    @Override
    public RenderingLayer layer() {
        return RenderingLayer.BACKGROUND;
    }
}
