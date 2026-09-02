/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public interface GameScene2D_Renderer extends Renderer {

    void draw(GameScene gameScene, long tick);

    static BaseDebugInfoRenderer createDefaultSceneDebugRenderer(GameScene gameScene, Canvas canvas) {
        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        final ActorSpriteAnimController animController = gameScene.game().variant().systems().actorSpriteAnimController();
        return r2D.configureRenderer(new BaseDebugInfoRenderer(animController, canvas));
    }
}