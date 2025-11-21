/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_CutScene2_Renderer extends GameScene2DRenderer {

    private final ArcadePacMan_CutScene2 scene;
    private final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene2_Renderer(ArcadePacMan_CutScene2 scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
        this.scene = requireNonNull(scene);
        actorRenderer = configureRendererForGameScene(scene.ui().currentConfig().createActorRenderer(canvas), scene);
    }

    public void draw() {
        drawSprite(scene.nailDressRaptureAnimation().currentSprite(), TS(14), TS(19) + 3, true);
        scene.actorsInZOrder().forEach(actorRenderer::drawActor);
    }
}
