/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene2_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_CutScene2_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.configureRenderer((ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        if (scene instanceof ArcadeMsPacMan_CutScene2 cutScene) {
            cutScene.clapperboard.setFont(arcadeFont8());
            Stream.of(
                cutScene.clapperboard,
                cutScene.msPacMan,
                cutScene.pacMan).forEach(actorRenderer::drawActor);
        }

        if (scene.game().ui().settings().debugInfoVisibleProperty.get()) {
            debugRenderer.draw(scene);
        }
    }
}