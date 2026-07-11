/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene1_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_CutScene1_Renderer(GameVariantConfig gameVariant, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.configureRenderer((ArcadeMsPacMan_ActorRenderer) gameVariant.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        clearCanvas();

        if (scene instanceof ArcadeMsPacMan_CutScene1 cutScene) {
            cutScene.clapperboard.setFont(arcadeFont8());
            Stream.of(
                cutScene.clapperboard,
                cutScene.msPacMan,
                cutScene.pacMan,
                cutScene.inky,
                cutScene.pinky,
                cutScene.heart).forEach(actorRenderer::drawActor);
        }

        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}