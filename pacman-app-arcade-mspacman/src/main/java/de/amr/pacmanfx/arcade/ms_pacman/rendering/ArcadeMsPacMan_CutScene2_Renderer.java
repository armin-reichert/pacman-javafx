/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene2_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_CutScene2_Renderer(
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        ActorSpriteAnimController animController,
        Canvas canvas) {

        super(canvas);

        final CanvasRenderingComp r2D = gameScene.componentsRegistry().reqComp(CanvasRenderingComp.class);

        actorRenderer = r2D.configureRenderer((ArcadeMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animController, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void draw(GameScene scene, long tick) {
        clearCanvas();

        if (scene instanceof ArcadeMsPacMan_CutScene2 cutScene) {
            Stream.of(
                cutScene.clapperboard,
                cutScene.msPacMan,
                cutScene.pacMan).forEach(actorRenderer::drawActor);
        }

        if (scene.app().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}