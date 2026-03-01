/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene1_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas) {
        super(scene, canvas);
        debugRenderer = scene.adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas) {
            @Override
            public void draw(GameScene2D scene) {
                super.draw(scene);
                if (scene instanceof ArcadePacMan_CutScene1 cutScene1) {
                    final String text = cutScene1.tick() < ArcadePacMan_CutScene1.ANIMATION_START_TICK
                        ? String.format("Wait %d", ArcadePacMan_CutScene1.ANIMATION_START_TICK - cutScene1.tick())
                        : String.format("Frame %d", cutScene1.tick());
                    fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(GameScene2D scene) {
        if (scene instanceof ArcadePacMan_CutScene1 cutScene) {
            actorRenderer.drawActor(cutScene.blinky());
            actorRenderer.drawActor(cutScene.pac());
        }
    }
}