/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.Rendering2DSupport;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadePacMan_CutScene1_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene1_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(gameScene, animSystem, canvas);

        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);
        debugRenderer = r2D.configureRenderer(new BaseDebugInfoRenderer(canvas) {
            @Override
            public void draw(GameScene scene, long tick) {
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
    protected void drawSceneContent(GameScene scene) {
        if (scene instanceof ArcadePacMan_CutScene1 cutScene) {
            actorRenderer.drawActor(cutScene.blinky);
            actorRenderer.drawActor(cutScene.pacMan);
        }
    }
}