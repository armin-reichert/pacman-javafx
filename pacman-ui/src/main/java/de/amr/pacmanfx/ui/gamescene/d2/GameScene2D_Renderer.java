/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public interface GameScene2D_Renderer extends Renderer {

    void draw(AbstractGameScene2D scene, long tick);

    static BaseDebugInfoRenderer createDefaultSceneDebugRenderer(AbstractGameScene2D scene, Canvas canvas) {
        return scene.configureRenderer(new BaseDebugInfoRenderer(canvas));
    }
}