/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene4;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene4_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_CutScene4_Renderer(GameVariantConfig gameVariant, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.configureRenderer((TengenMsPacMan_ActorRenderer) gameVariant.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    public void draw(AbstractGameScene2D scene) {
        clearCanvas();
        if (scene instanceof TengenMsPacMan_CutScene4 cutScene) {
            actorRenderer.drawActor(cutScene.clapperboard());
            actorRenderer.drawActor(cutScene.msPacMan());
            actorRenderer.drawActor(cutScene.pacMan());
            cutScene.juniors().forEach(actorRenderer::drawActor);
        }
        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene);
        }
    }
}