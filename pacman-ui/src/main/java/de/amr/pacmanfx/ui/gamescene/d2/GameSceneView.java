package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

public record GameSceneView(GameScene gameScene) implements Renderable {

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }
}
