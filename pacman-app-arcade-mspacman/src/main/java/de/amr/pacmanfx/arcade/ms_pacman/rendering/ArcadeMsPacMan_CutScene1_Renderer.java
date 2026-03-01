/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_CutScene1;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

public class ArcadeMsPacMan_CutScene1_Renderer extends GameScene2D_Renderer {

    private final ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene1_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        actorRenderer = scene.adaptRenderer((ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        createDefaultDebugInfoRenderer(scene.ui(), scene, canvas);
    }

    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadeMsPacMan_CutScene1 cutScene = (ArcadeMsPacMan_CutScene1) scene;
        cutScene.clapperboard().setFont(arcadeFont8());
        Stream.of(cutScene.clapperboard(), cutScene.msPacMan(), cutScene.pacMan(), cutScene.inky(), cutScene.pinky(), cutScene.heart())
            .forEach(actorRenderer::drawActor);

        if (cutScene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}