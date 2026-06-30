/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

public abstract class ArcadePacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRendererMixin {

    protected final ActorRenderer actorRenderer;
    protected BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_CutScene_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        final GameVariant currentConfig = scene.game().currentGameVariant();
        actorRenderer = scene.configureRenderer(currentConfig.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(AbstractGameScene2D scene) {
        clearCanvas();
        drawSceneContent(scene);
        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene);
        }
    }

    protected abstract void drawSceneContent(AbstractGameScene2D scene);
}