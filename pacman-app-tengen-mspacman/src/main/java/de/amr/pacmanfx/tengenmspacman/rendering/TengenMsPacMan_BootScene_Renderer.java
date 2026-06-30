/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_BootScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_BootScene_Renderer(GameVariantConfig uiConfig, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.configureRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(AbstractGameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_BootScene bootScene = (TengenMsPacMan_BootScene) scene;
        if (bootScene.gray) {
            actorRenderer.fillCanvas(NES_Palette.color(0x10));
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, actorRenderer.arcadeFont8(),
                bootScene.movingText.x(), bootScene.movingText.y());
            actorRenderer.drawActor(bootScene.ghost);
        }

        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene);
        }
    }
}