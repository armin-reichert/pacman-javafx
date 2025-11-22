/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene1;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

public class ArcadePacMan_CutScene1_Renderer extends GameScene2DRenderer {

    private final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene1_Renderer(ArcadePacMan_CutScene1 scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = configureRendererForGameScene(
            uiConfig.createActorRenderer(canvas), scene);

        debugInfoRenderer = configureRendererForGameScene(new BaseDebugInfoRenderer(scene, canvas, uiConfig.spriteSheet()) {
            @Override
            public void draw() {
                ArcadePacMan_CutScene1 cutScene = scene();
                super.draw();
                String text = cutScene.frame < ArcadePacMan_CutScene1.ANIMATION_START
                    ? String.format("Wait %d", ArcadePacMan_CutScene1.ANIMATION_START - cutScene.frame)
                    : String.format("Frame %d", cutScene.frame);
                fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
            }
        }, scene);
    }

    public void draw() {
        clearCanvas();

        ArcadePacMan_CutScene1 cutScene = scene();
        actorRenderer.drawActor(cutScene.blinky);
        actorRenderer.drawActor(cutScene.pac);
        if (cutScene.debugInfoVisible()) {
            debugInfoRenderer.draw();
        }
    }
}