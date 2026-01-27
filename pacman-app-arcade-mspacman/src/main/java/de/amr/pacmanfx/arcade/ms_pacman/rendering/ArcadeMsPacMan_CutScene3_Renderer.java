/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene3;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene3_Renderer extends GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene3_Renderer(GameUI_Config uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.adaptRenderer((ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_CutScene3 cutScene = (ArcadeMsPacMan_CutScene3) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard(), cutScene.msPacMan(), cutScene.pacMan(), cutScene.stork(), cutScene.bag())
            .forEach(actorRenderer::drawActor);

        if (cutScene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}