/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_BootScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class TengenMsPacMan_BootScene_Renderer extends GameSceneRenderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_BootScene_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        actorRenderer = r2D.configureRenderer((TengenMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof TengenMsPacMan_BootScene bootScene)) {
            return;
        }

        if (bootScene.gray) {
            actorRenderer.fillCanvas(NES_Palette.color(0x10));
        } else {
            actorRenderer.fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, actorRenderer.arcadeFont8(),
                bootScene.movingText.pos().x(), bootScene.movingText.pos().y());
            actorRenderer.render(bootScene.ghost, tick);
        }

        if (bootScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(bootScene, tick);
        }
    }
}