/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
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

    private Renderer entityRenderer;
    private Renderer sceneRenderer;
    private Renderer miniViewRenderer;

    private final RenderQueue renderQueue = new RenderQueue();

    public RenderManager() {}

    public void updateRenderers(GameVariantPlayConfig playConfig, GameVariantRenderConfig renderConfig,
                                GameScene gameScene, MiniPlaySceneView miniView) {

        requireNonNull(playConfig);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        final GameSceneCanvasRenderingComp sceneCanvasRendering = gameScene.optCanvasRendering().orElse(null);
        if (sceneCanvasRendering == null) {
            return; // This scene cannot be rendered inside a canvas, most probably the 3D play scene
        }
        final Canvas sceneCanvas = sceneCanvasRendering.canvas();

        if (sceneCanvas != null) {
            final ActorSpriteAnimController animController = playConfig.systems().actorSpriteAnimController();

            entityRenderer = renderConfig.createEntityRenderer(animController, sceneCanvas);
            configureRenderer(entityRenderer, sceneCanvasRendering);

            sceneRenderer = renderConfig.createGameSceneRenderer(gameScene, animController, sceneCanvas); // may return null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, sceneCanvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, sceneCanvasRendering));
            }

            //TODO This is just a temporary solution
            miniViewRenderer = new MiniPlaySceneViewRenderer(miniView, animController, renderConfig);
            // Mini view renderer has its own scaling and background
        }
        else {
            Logger.error("Cannot create renderers: no canvas has been defined!");
        }
    }

    public RenderQueue renderQueue() {
        return renderQueue;
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.entriesInOrder().forEach(r -> {
            switch (r.layer()) {
                case SCENE    -> renderScene(r, tick, debugMode);
                case OVERLAY  -> renderOverlay(r, tick);
                default       -> renderEntity(r, tick);
            }
        });
    }

    public void clearSceneCanvas(GameScene gameScene) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            final Canvas canvas = canvasRendering.canvas();
            if (canvas != null) {
                final var ctx = canvas.getGraphicsContext2D();
                ctx.setFill(canvasRendering.backgroundColor());
                ctx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });
    }

    private void renderEntity(Renderable r, long tick) {
        if (entityRenderer != null) {
            if (r instanceof RenderableWrapper wrapper) {
                renderEntity(wrapper.wrappedRenderable(), tick);
            } else {
                entityRenderer.render(r, tick);
            }
        }
    }

    private void renderScene(Renderable r, long tick, boolean debugMode) {
        if (sceneRenderer != null) {
            sceneRenderer.render(r, tick);
            if (debugMode) {
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(r, tick));
            }
        }
    }

    private void renderOverlay(Renderable r, long tick) {
        if (miniViewRenderer != null) {
            miniViewRenderer.render(r, tick);
        }
    }

    private void configureRenderer(Renderer renderer, GameSceneCanvasRenderingComp rendering) {
        renderer.backgroundColorProperty().bind(rendering.backgroundColorProperty());
        renderer.scalingProperty().bind(rendering.scalingProperty());
    }
}
