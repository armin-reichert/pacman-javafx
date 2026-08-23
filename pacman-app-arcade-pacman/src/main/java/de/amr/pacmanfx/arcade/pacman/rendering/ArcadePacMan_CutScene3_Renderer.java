/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene3;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadePacMan_CutScene3_Renderer extends ArcadePacMan_CutScene_Renderer {

    public ArcadePacMan_CutScene3_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(gameScene, animSystem, canvas);

        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);

        debugRenderer = r2D.configureRenderer(new BaseDebugInfoRenderer(canvas) {
            @Override
            public void draw(GameScene scene, long tick) {
                super.draw(scene, tick);
                if (scene instanceof ArcadePacMan_CutScene3 cutScene) {
                    final long sceneTick = cutScene.sceneTick;
                    String text = sceneTick < ArcadePacMan_CutScene3.TICK_ANIMATION_START
                        ? String.format("Wait %d", ArcadePacMan_CutScene3.TICK_ANIMATION_START - sceneTick)
                        : String.format("Frame %d", sceneTick);
                    fillText(text, debugTextFill, debugTextFont, tilesPx(1), tilesPx(5));
                }
            }
        });
    }

    @Override
    protected void drawSceneContent(GameScene scene) {
        final ArcadePacMan_CutScene3 cutScene = (ArcadePacMan_CutScene3) scene;
        actorRenderer.drawActor(cutScene.pacMan);
        actorRenderer.drawActor(cutScene.blinky);
    }
}