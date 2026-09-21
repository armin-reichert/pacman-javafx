package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.rendering.RenderingLayer;
import de.amr.basics.rendering.Renderable;

public record GridPattern(int cellSize, int width, int height) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
