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
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class ArcadeMsPacMan_CutScene_Renderer extends BaseRenderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;
    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_CutScene_Renderer(GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp canvasRendering = gameScene.reqComp(CanvasRenderingComp.class);
        actorRenderer = canvasRendering.configureRenderer((ArcadeMsPacMan_ActorRenderer) renderConfig.createActorRenderer(animController, canvas));
        debugRenderer = createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameScene gameScene)) {
            return;
        }
        switch (gameScene) {
            case ArcadeMsPacMan_CutScene1 cutScene -> cutScene.entitiesInRenderOrder().forEach(actor -> actorRenderer.render(actor, tick));
            case ArcadeMsPacMan_CutScene2 cutScene -> cutScene.entitiesInRenderOrder().forEach(actor -> actorRenderer.render(actor, tick));
            case ArcadeMsPacMan_CutScene3 cutScene -> cutScene.entitiesInRenderOrder().forEach(actor -> actorRenderer.render(actor, tick));
            default -> throw new IllegalStateException("Unexpected value: " + gameScene);
        }
        if (gameScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(gameScene, tick);
        }
    }
}