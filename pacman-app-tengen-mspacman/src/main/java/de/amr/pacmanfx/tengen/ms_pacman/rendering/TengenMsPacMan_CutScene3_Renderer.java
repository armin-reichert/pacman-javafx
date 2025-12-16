/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene3_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene3_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = GameScene2D_Renderer.adaptRenderer(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_CutScene3 cutScene = (TengenMsPacMan_CutScene3) scene;
        if (!cutScene.darkness()) {
            cutScene.clapperboard().setFont(arcadeFont8());
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.stork());
            actorRenderer.drawActor(cutScene.flyingBag());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
        }

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}