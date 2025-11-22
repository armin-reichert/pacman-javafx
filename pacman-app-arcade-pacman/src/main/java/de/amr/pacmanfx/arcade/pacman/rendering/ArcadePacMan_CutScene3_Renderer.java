/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;

public class ArcadePacMan_CutScene3_Renderer extends GameScene2DRenderer {

    private final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene3_Renderer(ArcadePacMan_CutScene3 scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        actorRenderer = configureRendererForGameScene(
            scene.ui().currentConfig().createActorRenderer(canvas), scene);
    }

    public void draw() {
        ArcadePacMan_CutScene3 cutScene = scene();
        actorRenderer.drawActor(cutScene.pac);
        actorRenderer.drawActor(cutScene.blinky);
    }
}