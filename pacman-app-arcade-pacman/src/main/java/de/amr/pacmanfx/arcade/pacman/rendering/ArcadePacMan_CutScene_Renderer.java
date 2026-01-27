/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

public abstract class ArcadePacMan_CutScene_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    protected final ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);
        final GameUI_Config uiConfig = scene.ui().currentConfig();
        actorRenderer = scene.adaptRenderer(uiConfig.createActorRenderer(canvas));
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        drawSceneContent(scene);
        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }

    protected abstract void drawSceneContent(GameScene2D scene);
}