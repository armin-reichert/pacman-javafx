/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.model.world.WorldMap.TS;

public class ArcadePacMan_CutScene3_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene3_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(scene, canvas);
        debugRenderer = scene.configureRenderer(new BaseDebugInfoRenderer(canvas) {
            @Override
            public void draw(AbstractGameScene2D scene) {
                super.draw(scene);
                if (scene instanceof ArcadePacMan_CutScene3 cutScene) {
                    final long tick = cutScene.sceneTick;
                    String text = tick < ArcadePacMan_CutScene3.TICK_ANIMATION_START
                        ? String.format("Wait %d", ArcadePacMan_CutScene3.TICK_ANIMATION_START - tick)
                        : String.format("Frame %d", tick);
                    fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(AbstractGameScene2D scene) {
        final ArcadePacMan_CutScene3 cutScene = (ArcadePacMan_CutScene3) scene;
        actorRenderer.drawActor(cutScene.pacMan);
        actorRenderer.drawActor(cutScene.blinky);
    }
}