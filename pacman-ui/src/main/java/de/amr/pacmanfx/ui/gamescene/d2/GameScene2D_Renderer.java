/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public interface GameScene2D_Renderer extends Renderer {

    void draw(AbstractGameScene gameScene, long tick);

    static BaseDebugInfoRenderer createDefaultSceneDebugRenderer(AbstractGameScene gameScene, Canvas canvas) {
        return gameScene.rendering2D().configureRenderer(new BaseDebugInfoRenderer(canvas));
    }
}