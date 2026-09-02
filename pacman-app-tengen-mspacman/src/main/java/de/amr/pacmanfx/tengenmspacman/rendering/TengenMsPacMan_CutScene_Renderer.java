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
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_CutScene_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        actorRenderer = r2D.configureRenderer((TengenMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void draw(GameScene gameScene, long tick) {
        switch (gameScene) {
            case TengenMsPacMan_CutScene1 cutScene1 -> drawCutScene1(cutScene1);
            case TengenMsPacMan_CutScene2 cutScene2 -> drawCutScene2(cutScene2);
            case TengenMsPacMan_CutScene3 cutScene3 -> drawCutScene3(cutScene3);
            case TengenMsPacMan_CutScene4 cutScene4 -> drawCutScene4(cutScene4);
            default -> throw new IllegalArgumentException("No cut scene!");
        }
        if (gameScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.draw(gameScene, tick);
        }
    }

    private void drawCutScene1(TengenMsPacMan_CutScene1 cutScene) {
        actorRenderer.drawActor(cutScene.clapperboard());
        actorRenderer.drawActor(cutScene.msPacMan());
        actorRenderer.drawActor(cutScene.pacMan());
        actorRenderer.drawActor(cutScene.inky());
        actorRenderer.drawActor(cutScene.pinky());
        actorRenderer.drawActor(cutScene.heart());
    }

    private void drawCutScene2(TengenMsPacMan_CutScene2 cutScene) {
        actorRenderer.drawActor(cutScene.clapperboard());
        actorRenderer.drawActor(cutScene.msPacMan());
        actorRenderer.drawActor(cutScene.pacMan());
    }

    private void drawCutScene3(TengenMsPacMan_CutScene3 cutScene) {
        if (!cutScene.darkness()) {
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.stork());
            actorRenderer.drawActor(cutScene.flyingBag());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
        }
    }

    private void drawCutScene4(TengenMsPacMan_CutScene4 cutScene) {
        actorRenderer.drawActor(cutScene.clapperboard());
        actorRenderer.drawActor(cutScene.msPacMan());
        actorRenderer.drawActor(cutScene.pacMan());
        cutScene.juniors().forEach(actorRenderer::drawActor);
    }
}