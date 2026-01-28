/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene3_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene3_Renderer(GameScene2D scene, Canvas canvas) {
        super(scene, canvas);
        debugRenderer = scene.adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas) {
            @Override
            public void draw(GameScene2D scene) {
                super.draw(scene);
                if (scene instanceof ArcadePacMan_CutScene3 cutScene3) {
                    String text = cutScene3.tick() < ArcadePacMan_CutScene3.ANIMATION_START_TICK
                        ? String.format("Wait %d", ArcadePacMan_CutScene3.ANIMATION_START_TICK - cutScene3.tick())
                        : String.format("Frame %d", cutScene3.tick());
                    fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(GameScene2D scene) {
        final ArcadePacMan_CutScene3 cutScene = (ArcadePacMan_CutScene3) scene;
        actorRenderer.drawActor(cutScene.pac());
        actorRenderer.drawActor(cutScene.blinky());
    }
}