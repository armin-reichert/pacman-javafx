/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene2_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene2_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = adaptRenderer(
            (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);

        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_CutScene2 cutScene = (TengenMsPacMan_CutScene2) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        actorRenderer.drawActor(cutScene.clapperboard());
        actorRenderer.drawActor(cutScene.msPacMan());
        actorRenderer.drawActor(cutScene.pacMan());

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}
