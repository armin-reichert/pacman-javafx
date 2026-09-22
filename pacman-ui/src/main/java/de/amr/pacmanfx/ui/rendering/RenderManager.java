/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;

import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.GameVariantPlayConfig;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniViewOverlayRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private Renderer variantRenderer;
    private Renderer levelRenderer;
    private Renderer sceneRenderer;
    private Renderer sceneDebugRenderer;
    private Renderer miniViewOverlayRenderer;

    private final RenderQueue renderQueue = new RenderQueue();

    private GameSceneCanvasRenderingComp sceneCanvasRendering;

    public RenderManager() {}

    public void createRenderers(
        GameVariantPlayConfig playConfig,
        GameVariantRenderConfig renderConfig,
        AbstractGameScene gameScene,
        MiniPlaySceneView miniView)
    {
        requireNonNull(playConfig);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(miniView);

        final ActorSpriteAnimController animController = playConfig.systems().actorSpriteAnimController();

        //TODO This is just a temporary solution
        miniViewOverlayRenderer = new MiniViewOverlayRenderer(miniView, animController, renderConfig);

        // If this scene has 2D rendering support, create and configure renderers

        sceneCanvasRendering = gameScene.optCanvasRendering().orElse(null);
        if (sceneCanvasRendering == null) {
            return;
        }

        final Canvas sceneCanvas = sceneCanvasRendering.canvas();
        if (sceneCanvas == null) {
            return;
        }

        variantRenderer    = renderConfig.createVariantRenderer(animController, sceneCanvas);
        sceneRenderer      = renderConfig.createGameSceneRenderer(gameScene, animController, sceneCanvas); // may return null!
        sceneDebugRenderer = renderConfig.createGameSceneDebugRenderer(gameScene, animController, sceneCanvas);
        levelRenderer      = renderConfig.createGameLevelRenderer(animController, sceneCanvas);

        final Vector2f offset = gameScene.renderOffset();
        if (sceneRenderer != null) {
            configureRenderer(sceneRenderer, sceneCanvasRendering, offset);
        }
        configureRenderer(variantRenderer, sceneCanvasRendering, offset);
        configureRenderer(sceneDebugRenderer, sceneCanvasRendering, offset);
        configureRenderer(levelRenderer, sceneCanvasRendering, offset);
    }

    public RenderQueue renderQueue() {
        return renderQueue;
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

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort();

        renderQueue.renderables().forEach(r -> {

            switch (r.layer()) {
                case WORLD    -> renderWorld(r, tick);
                case SCENE    -> renderScene(r, tick);
                case MINIVIEW_OVERLAY -> renderOverlay(r, tick);
                case HUD      -> renderHUD(r, tick);
                default       -> renderAnything(r, tick);
            }
        });

        if (debugMode) {
            renderQueue.renderables()
                .filter(r -> r.layer() == RenderingLayer.SCENE)
                .forEach(r -> sceneDebugRenderer.render(r, tick));
        }
    }


    private static void renderWithOffset(Renderable r, Renderer renderer, long tick) {
        final Vector2f offset = renderer.info().get("offset", Vector2f.class).scaled(renderer.scaling());
        renderer.ctx().save();
        renderer.ctx().translate(offset.x(), offset.y());
        renderer.render(r, tick);
        renderer.ctx().restore();
    }

    private void renderAnything(Renderable r, long tick) {
        renderWithOffset(r, variantRenderer, tick);
    }

    private void renderScene(Renderable r, long tick) {
        if (sceneRenderer != null) {
            renderWithOffset(r, sceneRenderer, tick);
        }
    }

    private void renderWorld(Renderable r, long tick) {
        renderWithOffset(r, levelRenderer, tick);
    }

    private void renderHUD(Renderable r, long tick) {
        variantRenderer.render(r, tick);
    }

    private void renderOverlay(Renderable r, long tick) {
        if (miniViewOverlayRenderer != null) {
            miniViewOverlayRenderer.render(r, tick);
        }
    }

    private static void configureRenderer(
        Renderer renderer,
        GameSceneCanvasRenderingComp canvasRendering,
        Vector2f offset)
    {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
        renderer.info().put("offset", offset);
    }
}
