/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneViewRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private Renderer variantRenderer;
    private Renderer sceneRenderer;
    private Renderer sceneDebugRenderer;
    private Renderer miniViewRenderer;

    private final RenderQueue renderQueue = new RenderQueue();

    public RenderManager() {}

    public void updateRenderers(
        GameVariantPlayConfig playConfig,
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        MiniPlaySceneView miniView)
    {
        requireNonNull(playConfig);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        if (!(gameScene instanceof AbstractGameScene abstractGameScene)) {
            Logger.error("GameScene is not an instance of AbstractGameScene");
            return;
        }

        final GameSceneCanvasRenderingComp sceneCanvasRendering = abstractGameScene.optCanvasRendering().orElse(null);
        if (sceneCanvasRendering == null) {
            return; // This scene cannot be rendered inside a canvas, most probably the 3D play scene
        }

        final Canvas sceneCanvas = sceneCanvasRendering.canvas();
        if (sceneCanvas != null) {
            final ActorSpriteAnimController animController = playConfig.systems().actorSpriteAnimController();

            variantRenderer    = renderConfig.createVariantRenderer(animController, sceneCanvas);
            sceneRenderer      = renderConfig.createGameSceneRenderer(gameScene, animController, sceneCanvas); // may return null!
            sceneDebugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneCanvas);

            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, sceneCanvasRendering);
            }
            configureRenderer(variantRenderer, sceneCanvasRendering);
            configureRenderer(sceneDebugRenderer, sceneCanvasRendering);

            //TODO This is just a temporary solution
            // Mini view renderer has its own scaling and background
            miniViewRenderer = new MiniPlaySceneViewRenderer(miniView, animController, renderConfig);
        }
        else {
            Logger.error("Cannot create renderers: no canvas has been assigned to game scene!");
        }
    }

    public RenderQueue renderQueue() {
        return renderQueue;
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort();

        renderQueue.renderables().forEach(r -> {
            switch (r.layer()) {
                case SCENE    -> renderScene(r, tick);
                case OVERLAY  -> renderOverlay(r, tick);
                default       -> render(r, tick);
            }
        });

        if (debugMode) {
            renderQueue.renderables()
                .filter(r -> r.layer() == RenderingLayer.SCENE)
                .forEach(r -> sceneDebugRenderer.render(r, tick));
        }
    }

    public void clearSceneCanvas(AbstractGameScene gameScene) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            final Canvas canvas = canvasRendering.canvas();
            if (canvas != null) {
                final var ctx = canvas.getGraphicsContext2D();
                ctx.setFill(canvasRendering.backgroundColor());
                ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });
    }

    private void render(Renderable r, long tick) {
        switch (r) {
            case RenderableWrapper wrapper -> render(wrapper.content(), tick);
            default -> variantRenderer.render(r, tick);
        }
    }

    private void renderScene(Renderable r, long tick) {
        if (sceneRenderer != null) {
            sceneRenderer.render(r, tick);
        }
    }

    private void renderOverlay(Renderable r, long tick) {
        if (miniViewRenderer != null) {
            miniViewRenderer.render(r, tick);
        }
    }

    private static void configureRenderer(Renderer renderer, GameSceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
