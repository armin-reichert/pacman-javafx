/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.model.world.WorldMap.tilesPx;

public class ArcadePacMan_CutScene1_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene1_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(scene, canvas);
        debugRenderer = scene.configureRenderer(new BaseDebugInfoRenderer(canvas) {
            @Override
            public void draw(AbstractGameScene2D scene, long tick) {
                super.draw(scene, tick);
                if (scene instanceof ArcadePacMan_CutScene1 cutScene1) {
                    final String text = cutScene1.sceneTick < ArcadePacMan_CutScene1.ANIMATION_START_TICK
                        ? String.format("Wait %d", ArcadePacMan_CutScene1.ANIMATION_START_TICK - cutScene1.sceneTick)
                        : String.format("Frame %d", cutScene1.sceneTick);
                    fillText(text, debugTextFill, debugTextFont, tilesPx(1), tilesPx(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(AbstractGameScene2D scene) {
        if (scene instanceof ArcadePacMan_CutScene1 cutScene) {
            actorRenderer.drawActor(cutScene.blinky);
            actorRenderer.drawActor(cutScene.pacMan);
        }
    }
}