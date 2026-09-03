/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene1;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene2;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene_Renderer extends BaseRenderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_CutScene_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);
        actorRenderer = r2D.configureRenderer((TengenMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameScene gameScene)) {
            return;
        }

        switch (gameScene) {
            case TengenMsPacMan_CutScene1 cutScene1 -> drawCutScene1(cutScene1, tick);
            case TengenMsPacMan_CutScene2 cutScene2 -> drawCutScene2(cutScene2, tick);
            case TengenMsPacMan_CutScene3 cutScene3 -> drawCutScene3(cutScene3, tick);
            case TengenMsPacMan_CutScene4 cutScene4 -> drawCutScene4(cutScene4, tick);
            default -> throw new IllegalArgumentException("No cut scene!");
        }
        if (gameScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(gameScene, tick);
        }
    }

    private void drawCutScene1(TengenMsPacMan_CutScene1 cutScene, long tick) {
        actorRenderer.render(cutScene.clapperboard(), tick);
        actorRenderer.render(cutScene.msPacMan(), tick);
        actorRenderer.render(cutScene.pacMan(), tick);
        actorRenderer.render(cutScene.inky(), tick);
        actorRenderer.render(cutScene.pinky(), tick);
        actorRenderer.render(cutScene.heart(), tick);
    }

    private void drawCutScene2(TengenMsPacMan_CutScene2 cutScene, long tick) {
        actorRenderer.render(cutScene.clapperboard(), tick);
        actorRenderer.render(cutScene.msPacMan(), tick);
        actorRenderer.render(cutScene.pacMan(), tick);
    }

    private void drawCutScene3(TengenMsPacMan_CutScene3 cutScene, long tick) {
        if (!cutScene.darkness()) {
            actorRenderer.render(cutScene.clapperboard(), tick);
            actorRenderer.render(cutScene.stork(), tick);
            actorRenderer.render(cutScene.flyingBag(), tick);
            actorRenderer.render(cutScene.msPacMan(), tick);
            actorRenderer.render(cutScene.pacMan(), tick);
        }
    }

    private void drawCutScene4(TengenMsPacMan_CutScene4 cutScene, long tick) {
        actorRenderer.render(cutScene.clapperboard(), tick);
        actorRenderer.render(cutScene.msPacMan(), tick);
        actorRenderer.render(cutScene.pacMan(), tick);
        cutScene.juniors().forEach(junior -> actorRenderer.render(junior, tick));
    }
}