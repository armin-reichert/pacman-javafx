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
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_CutScene1_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    private final ArcadePacMan_SpriteSheet spriteSheet;
    private final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene1_Renderer(GameScene2D scene, Canvas canvas, ArcadePacMan_SpriteSheet spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);

        final GameUI_Config uiConfig = scene.ui().currentConfig();

        actorRenderer = adaptRenderer(
            uiConfig.createActorRenderer(canvas), scene);

        debugRenderer = adaptRenderer(new BaseDebugInfoRenderer(scene.ui(), canvas) {
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
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final ArcadePacMan_CutScene1 cutScene = (ArcadePacMan_CutScene1) scene;
        actorRenderer.drawActor(cutScene.blinky());
        actorRenderer.drawActor(cutScene.pac());

        if (cutScene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}