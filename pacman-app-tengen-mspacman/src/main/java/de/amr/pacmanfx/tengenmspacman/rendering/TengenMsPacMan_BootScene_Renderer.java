/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;

public class TengenMsPacMan_BootScene_Renderer extends GameScene2D_Renderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_ActorRenderer actorRenderer;

    public TengenMsPacMan_BootScene_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.adaptRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_BootScene bootScene = (TengenMsPacMan_BootScene) scene;
        if (bootScene.gray) {
            actorRenderer.fillCanvas(nesColor(0x10));
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, actorRenderer.arcadeFont8(),
                bootScene.movingText.x(), bootScene.movingText.y());
            actorRenderer.drawActor(bootScene.ghost);
        }

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}