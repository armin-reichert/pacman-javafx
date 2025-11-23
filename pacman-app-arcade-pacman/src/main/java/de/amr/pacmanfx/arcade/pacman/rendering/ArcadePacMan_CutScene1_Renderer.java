/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene1_Renderer extends GameScene2D_Renderer {

    private final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene1_Renderer(ArcadePacMan_CutScene1 scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene(
            uiConfig.createActorRenderer(canvas), scene);

        debugInfoRenderer = configureRendererForGameScene(new BaseDebugInfoRenderer(scene.ui(), canvas, uiConfig.spriteSheet()) {
            @Override
            public void draw(GameScene2D scene) {
                ArcadePacMan_CutScene1 cutScene = (ArcadePacMan_CutScene1) scene;
                super.draw(scene);
                String text = cutScene.frame() < ArcadePacMan_CutScene1.ANIMATION_START
                    ? String.format("Wait %d", ArcadePacMan_CutScene1.ANIMATION_START - cutScene.frame())
                    : String.format("Frame %d", cutScene.frame());
                fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
            }
        }, scene);
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadePacMan_CutScene1 cutScene = (ArcadePacMan_CutScene1) scene;
        actorRenderer.drawActor(cutScene.blinky());
        actorRenderer.drawActor(cutScene.pac());

        if (cutScene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }
}