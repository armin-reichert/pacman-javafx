/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene2_Renderer extends GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_CutScene2_Renderer(GameUI_Config uiConfig, UIPreferences prefs, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = adaptRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas), scene);
        createDefaultDebugInfoRenderer(prefs, scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        if (scene instanceof TengenMsPacMan_CutScene2 cutScene) {
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
        }
        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}
