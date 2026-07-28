/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public abstract class ArcadePacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRenderer {

    protected final SpriteAnimSystem animSystem;
    protected final ActorRenderer actorRenderer;
    protected BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_CutScene_Renderer(AbstractGameScene2D scene, SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
        final GameVariantRenderConfig renderConfig = scene.appContext().variants().currentVariant().config().renderConfig();
        actorRenderer = scene.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public SpriteAnimSystem animSystem() {
        return animSystem;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        clearCanvas();
        drawSceneContent(scene);
        if (scene.appContext().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }

    protected abstract void drawSceneContent(AbstractGameScene2D scene);
}