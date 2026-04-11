/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public interface GameScene2D_Renderer extends Renderer {

    void draw(GameScene2D scene);

    static BaseDebugInfoRenderer createDefaultSceneDebugRenderer(GameScene2D scene, Canvas canvas) {
        return scene.adaptRenderer(new BaseDebugInfoRenderer(canvas));
    }
}