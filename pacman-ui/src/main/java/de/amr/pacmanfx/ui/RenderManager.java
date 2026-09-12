/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.GameVariantConfig;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneViewRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    public static final Comparator<Renderable> RENDERING_ORDER = Comparator
        .comparing(Renderable::layer)
        .thenComparingInt(Renderable::z);

    private Renderer entityRenderer;
    private Renderer sceneRenderer;
    private Renderer miniViewRenderer;

    private final List<Renderable> renderQueue = new ArrayList<>();

    public void updateRenderers(
        GameVariantConfig gameVariantConfig,
        GameVariantRenderConfig renderConfig,
        GameScene gameScene,
        MiniPlaySceneView miniView)
    {
        requireNonNull(gameVariantConfig);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        if (!gameScene.hasComp(SceneCanvasRenderingComp.class)) {
            return;
        }
        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            final ActorSpriteAnimController animController = gameVariantConfig.systems().actorSpriteAnimController();

            canvas.getGraphicsContext2D().setImageSmoothing(false);

            entityRenderer = renderConfig.createEntityRenderer(animController, canvas);
            configureRenderer(entityRenderer, canvasRendering);

            sceneRenderer = renderConfig.createGameSceneRenderer(gameScene, animController, canvas); // may be null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, canvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, canvasRendering));
            }

            //TODO temporary solution
            miniViewRenderer = new MiniPlaySceneViewRenderer(miniView, animController, renderConfig);
        }
        else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void clearRenderQueue() {
        renderQueue.clear();
    }

    public void addRenderable(Renderable renderable) {
        renderQueue.add(renderable);
    }

    public void addRenderables(Stream<Renderable> renderables) {
        renderables.forEach(this::addRenderable);
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort(RENDERING_ORDER);
        renderQueue.forEach(r -> {
            switch (r.layer()) {
                case SCENE    -> renderScene(r, tick, debugMode);
                case OVERLAY  -> renderOverlay(r, tick);
                default       -> renderEntity(r, tick);
            }
        });
    }

    public void clearSceneCanvas(GameScene gameScene) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            if (canvasRendering.canvas() != null) {
                final var ctx = canvasRendering.canvas().getGraphicsContext2D();
                ctx.setFill(canvasRendering.backgroundColor());
                ctx.fillRect(0, 0, canvasRendering.canvas().getWidth(), canvasRendering.canvas().getHeight());
            } else {
                Logger.error("Cannot create game scene canvas: no canvas has been assigned");
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

    public void renderOverlay(Renderable r, long tick) {
        if (miniViewRenderer != null) {
            miniViewRenderer.render(r, tick);
        }
    }

    private void configureRenderer(Renderer renderer, SceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
