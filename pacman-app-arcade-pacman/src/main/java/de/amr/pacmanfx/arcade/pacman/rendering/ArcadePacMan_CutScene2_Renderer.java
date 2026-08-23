/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadePacMan_CutScene2_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene2_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(gameScene, animSystem, canvas);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        debugRenderer = r2D.configureRenderer(new BaseDebugInfoRenderer(canvas) {
            @Override
            public void draw(GameScene scene, long tick) {
                super.draw(scene, tick);
                if (scene instanceof ArcadePacMan_CutScene2 cutScene2) {
                    final String text = cutScene2.sceneTick < ArcadePacMan_CutScene2.TICK_ANIMATION_START
                        ? String.format("Wait %d", ArcadePacMan_CutScene2.TICK_ANIMATION_START - cutScene2.sceneTick)
                        : String.format("Frame %d", cutScene2.sceneTick);
                    fillText(text, debugTextFill, debugTextFont, tilesPx(1), tilesPx(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(GameScene scene) {
        if (scene instanceof ArcadePacMan_CutScene2 cutScene) {
            drawSprite(cutScene.nailDressAnimation.sprite(), cutScene.nailX, cutScene.nailY, true);
            actorRenderer.drawActor(cutScene.pacMan);
            actorRenderer.drawActor(cutScene.blinky);
        }
    }
}