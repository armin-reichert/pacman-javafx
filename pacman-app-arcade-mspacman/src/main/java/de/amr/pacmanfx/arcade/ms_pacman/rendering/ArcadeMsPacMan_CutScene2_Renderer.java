/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene2;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene2_Renderer extends GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene2_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.adaptRenderer((ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene, canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_CutScene2 cutScene = (ArcadeMsPacMan_CutScene2) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard(), cutScene.msPacMan(), cutScene.pacMan()).forEach(actorRenderer::drawActor);

        if (cutScene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}