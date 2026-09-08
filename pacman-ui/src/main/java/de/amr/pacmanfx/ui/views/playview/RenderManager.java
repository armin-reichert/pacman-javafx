/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
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
        .comparingInt((Renderable r) -> r.layer().z())
        .thenComparingInt(Renderable::zOrder);
    private BaseRenderer entityRenderer;
    private BaseRenderer sceneRenderer;
    private BaseRenderer hudRenderer;

    private final List<Renderable> renderQueue = new ArrayList<>();

    public void updateRenderers(GameAppContext app, GameScene gameScene) {
        requireNonNull(app);
        requireNonNull(gameScene);

        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
            final GameVariantRenderConfig config = app.currentGameVariantUIConfig().renderConfig();

            entityRenderer = config.createEntityRenderer(animController, canvas);
            configureRenderer(entityRenderer, canvasRendering);

            sceneRenderer = config.createGameSceneRenderer(gameScene, animController, canvas); // may be null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, canvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, canvasRendering));
            }

            hudRenderer = config.createHUDRenderer(gameScene, animController, canvas);
            configureRenderer(hudRenderer, canvasRendering);
        }
        else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    //TODO temporary

    public MiniViewRenderer createMiniViewRenderer(
        GameViewModel viewModel,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig)
    {
        final var renderer = new MiniViewRenderer(
            entityRenderer.canvas(),
            animController,
            renderConfig,
            viewModel);

        renderer.levelRenderer().scalingProperty().bind(entityRenderer.scalingProperty());
        renderer.actorRenderer().scalingProperty().bind(entityRenderer.scalingProperty());

        return renderer;
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

    public void renderFrame(GameSession session, long tick, boolean debugMode) {
        renderQueue.sort(RENDERING_ORDER);
        renderQueue.forEach(renderable -> {
            switch (renderable.layer()) {
                case HUD -> {
                    if (session.hud().isVisible()) { //TODO
                        hudRenderer.render(renderable, tick);
                    }
                }
                case SCENE -> {
                    if (renderable instanceof GameScene gameScene) {
                        renderGameScene(gameScene, tick, debugMode);
                    }
                }
                default -> entityRenderer.render(renderable, tick);
            }
        });
    }

    //TODO fully integrate game scene rendering into renderFrame
    private void renderGameScene(GameScene gameScene, long tick, boolean debugMode) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            if (canvasRendering.clearCanvasBeforeRendering()) {
                entityRenderer.clearCanvas();
            }
            if (sceneRenderer != null) {
                sceneRenderer.render(gameScene, tick);
                if (debugMode) {
                    sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(gameScene, tick));
                }
            }
        });
    }

    private void configureRenderer(Renderer renderer, SceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
