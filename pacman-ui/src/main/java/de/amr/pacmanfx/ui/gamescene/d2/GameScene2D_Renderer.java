/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public interface GameScene2D_Renderer extends Renderer {

    void draw(GameScene gameScene, long tick);

    static BaseDebugInfoRenderer createDefaultSceneDebugRenderer(GameScene gameScene, Canvas canvas) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        return r2D.configureRenderer(new BaseDebugInfoRenderer(canvas));
    }
}