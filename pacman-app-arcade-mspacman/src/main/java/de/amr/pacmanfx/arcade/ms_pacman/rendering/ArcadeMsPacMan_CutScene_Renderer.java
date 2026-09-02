/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class ArcadeMsPacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_CutScene_Renderer(GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp canvasRendering = gameScene.components().reqComp(CanvasRenderingComp.class);
        actorRenderer = canvasRendering.configureRenderer((ArcadeMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animController, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void draw(GameScene gameScene, long tick) {
        switch (gameScene) {
            case ArcadeMsPacMan_CutScene1 cutScene -> cutScene.entitiesInRenderOrder().forEach(actorRenderer::drawActor);
            case ArcadeMsPacMan_CutScene2 cutScene -> cutScene.entitiesInRenderOrder().forEach(actorRenderer::drawActor);
            case ArcadeMsPacMan_CutScene3 cutScene -> cutScene.entitiesInRenderOrder().forEach(actorRenderer::drawActor);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        }
        if (gameScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.draw(gameScene, tick);
        }
    }
}