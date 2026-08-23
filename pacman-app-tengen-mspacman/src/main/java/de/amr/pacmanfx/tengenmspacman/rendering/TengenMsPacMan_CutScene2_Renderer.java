/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene2_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_CutScene2_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        actorRenderer = r2D.configureRenderer((TengenMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void draw(GameScene scene, long tick) {
        clearCanvas();
        if (scene instanceof TengenMsPacMan_CutScene2 cutScene) {
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
        }
        if (scene.app().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}
