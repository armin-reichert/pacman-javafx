/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public abstract class ArcadePacMan_CutScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRenderer {

    protected final ActorSpriteAnimController animSystem;
    protected final ActorRenderer actorRenderer;
    protected BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_CutScene_Renderer(GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);
        this.animSystem = requireNonNull(animSystem);
        final GameVariantRenderConfig renderConfig = gameScene.app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        actorRenderer = r2D.configureRenderer(renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameScene scene, long tick) {
        drawSceneContent(scene);
        if (scene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.draw(scene, tick);
        }
    }

    protected abstract void drawSceneContent(GameScene scene);
}