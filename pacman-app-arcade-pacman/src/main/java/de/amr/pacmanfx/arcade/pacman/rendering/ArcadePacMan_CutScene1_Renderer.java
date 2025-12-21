/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene1_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(scene, canvas, spriteSheet);
        debugRenderer = adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas) {
            @Override
            public void draw(GameScene2D scene) {
                super.draw(scene);
                if (scene instanceof ArcadePacMan_CutScene1 cutScene1) {
                    String text = cutScene1.tick() < ArcadePacMan_CutScene1.ANIMATION_START_TICK
                        ? String.format("Wait %d", ArcadePacMan_CutScene1.ANIMATION_START_TICK - cutScene1.tick())
                        : String.format("Frame %d", cutScene1.tick());
                    fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
                }
            }
        }, scene);
    }

    @Override
    protected void drawSceneContent(GameScene2D scene) {
        final ArcadePacMan_CutScene1 cutScene = (ArcadePacMan_CutScene1) scene;
        actorRenderer.drawActor(cutScene.blinky());
        actorRenderer.drawActor(cutScene.pac());
    }
}