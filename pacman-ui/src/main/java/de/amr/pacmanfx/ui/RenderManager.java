/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.views.miniview.MiniPlaySceneView;
import de.amr.pacmanfx.ui.views.miniview.MiniViewRenderer;
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
    private Renderer hudRenderer;
    private Renderer miniViewRenderer;

    private final List<Renderable> renderQueue = new ArrayList<>();

    public void updateRenderers(GameAppContext app, GameScene gameScene) {
        requireNonNull(app);
        requireNonNull(gameScene);

        if (!gameScene.hasComp(SceneCanvasRenderingComp.class)) {
            return;
        }
        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
            final GameVariantRenderConfig config = app.currentGameVariantUIConfig().renderConfig();

            canvas.getGraphicsContext2D().setImageSmoothing(false);

            entityRenderer = config.createEntityRenderer(animController, canvas);
            configureRenderer(entityRenderer, canvasRendering);

            sceneRenderer = config.createGameSceneRenderer(gameScene, animController, canvas); // may be null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, canvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, canvasRendering));
            }

            hudRenderer = config.createHUDRenderer(gameScene, animController, canvas);
            configureRenderer(hudRenderer, canvasRendering);

            //TODO temp solution
            final MiniPlaySceneView miniView = app.ui().views().gamePlayView().layers().miniViewLayer();
            miniView.createRenderer();
        }
        else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
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

    public void setMiniViewRenderer(MiniViewRenderer miniViewRenderer) {
        this.miniViewRenderer = miniViewRenderer;
    }

    public void clearRenderQueue() {
        renderQueue.clear();
    }

    public void add(Renderable renderable) {
        renderQueue.add(renderable);
    }

    public void addAll(Stream<Renderable> renderables) {
        renderables.forEach(this::add);
    }

    public void renderFrame(long tick, boolean debugMode) {
        renderQueue.sort(RENDERING_ORDER);
        renderQueue.forEach(r -> {
            switch (r.layer()) {
                case HUD -> hudRenderer.render(r, tick);
                case SCENE -> renderGameScene(r, tick, debugMode);
                case OVERLAY -> renderMiniView(r, tick);
                default -> renderGameEntity(r, tick);
            }
        });
    }

    private void renderGameEntity(Renderable r, long tick) {
        if (entityRenderer != null) {
            if (r instanceof RenderableWrapper wrapper) {
                renderGameEntity(wrapper.wrappedRenderable(), tick);
            } else {
                entityRenderer.render(r, tick);
            }
        }
    }

    private void renderMiniView(Renderable r, long tick) {
        if (miniViewRenderer != null) {
            miniViewRenderer.render(r, tick);
        }
    }

    private void renderGameScene(Renderable r, long tick, boolean debugMode) {
        if (sceneRenderer != null) {
            sceneRenderer.render(r, tick);
            if (debugMode) {
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(r, tick));
            }
        }
    }

    private void configureRenderer(Renderer renderer, SceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
