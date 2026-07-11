/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CutScene3;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class TengenMsPacMan_CutScene3_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_CutScene3_Renderer(GameVariantConfig gameVariant, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.configureRenderer((TengenMsPacMan_ActorRenderer) gameVariant.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        clearCanvas();
        if (scene instanceof TengenMsPacMan_CutScene3 cutScene) {
            if (!cutScene.darkness()) {
                actorRenderer.drawActor(cutScene.clapperboard());
                actorRenderer.drawActor(cutScene.stork());
                actorRenderer.drawActor(cutScene.flyingBag());
                actorRenderer.drawActor(cutScene.msPacMan());
                actorRenderer.drawActor(cutScene.pacMan());
            }
        }
        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}