/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene1_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene1_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.adaptRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene.ui(), scene, canvas);
    }

    public void draw(GameScene2D scene) {
        clearCanvas();
        if (scene instanceof TengenMsPacMan_CutScene1 cutScene) {
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
            actorRenderer.drawActor(cutScene.inky());
            actorRenderer.drawActor(cutScene.pinky());
            actorRenderer.drawActor(cutScene.heart());
        }
        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}