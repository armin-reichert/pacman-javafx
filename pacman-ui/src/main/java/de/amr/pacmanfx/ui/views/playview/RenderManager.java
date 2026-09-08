/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.InfoMap;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    public static final Comparator<Renderable> RENDERING_ORDER = Comparator
        .comparingInt((Renderable r) -> r.layer().z())
        .thenComparingInt(Renderable::zOrder);

    private BaseRenderer entityRenderer;
    private BaseRenderer sceneRenderer;
    private BaseRenderer hudRenderer;
    private MiniViewRenderer miniViewRenderer;

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

            entityRenderer = config.createEntityRenderer(animController, canvas);
            configureRenderer(entityRenderer, canvasRendering);

            sceneRenderer = config.createGameSceneRenderer(gameScene, animController, canvas); // may be null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, canvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, canvasRendering));
            }

            hudRenderer = config.createHUDRenderer(gameScene, animController, canvas);
            configureRenderer(hudRenderer, canvasRendering);

            miniViewRenderer = createMiniViewRenderer(app);
        }
        else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
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
        miniViewRenderer.levelRenderer().clearCanvas();
        renderQueue.sort(RENDERING_ORDER);
        renderQueue.forEach(r -> {
            switch (r.layer()) {
                case HUD -> hudRenderer.render(r, tick);
                case SCENE -> renderGameScene(r, tick, debugMode);
                case OVERLAY -> renderMiniView(r, tick);
                default -> entityRenderer.render(r, tick);
            }
        });
    }

    private void renderMiniView(Renderable r, long tick) {
        if (r instanceof RenderableWrapper wrapper) {
            switch (wrapper.wrappedRenderable()) {
                case GameLevel level -> {
                    final InfoMap infoMap = new InfoMap();
                    infoMap.putAll(Map.of(
                        CommonRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON,
                        CommonRenderInfoKey.MAP_BRIGHT, false,
                        CommonRenderInfoKey.MAP_EMPTY, level.food().remainingFoodCount() == 0,
                        CommonRenderInfoKey.MAP_FLASHING, false,
                        CommonRenderInfoKey.TICK, tick
                    ));
                    miniViewRenderer.levelRenderer().setInfoMap(infoMap);
                    miniViewRenderer.levelRenderer().render(level, tick);
                }
                default -> miniViewRenderer.entityRenderer().render(wrapper.wrappedRenderable(), tick);
            }
        }
    }

    private void renderGameScene(Renderable r, long tick, boolean debugMode) {
        if (sceneRenderer != null) {
            if (!(sceneRenderer instanceof AutoClearDisabled)) {
                sceneRenderer.clearCanvas();
            }
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

    //TODO temporary solution

    private MiniViewRenderer createMiniViewRenderer(GameAppContext app) {
        final MiniPlaySceneView miniView = app.ui().views().gamePlayView().layers().miniViewLayer();
        final GameViewModel viewModel = app.ui().viewModel();
        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig renderConfig = app.currentGameVariantUIConfig().renderConfig();

        final var miniViewRenderer = new MiniViewRenderer(miniView.canvas(), animController, renderConfig, viewModel);

        miniViewRenderer.entityRenderer().backgroundColorProperty().bind(entityRenderer.backgroundColorProperty());
        miniViewRenderer.entityRenderer().scalingProperty().bind(miniView.scalingProperty());

        miniViewRenderer.levelRenderer().backgroundColorProperty().bind(entityRenderer.backgroundColorProperty());
        miniViewRenderer.levelRenderer().scalingProperty().bind(miniView.scalingProperty());

        return miniViewRenderer;
    }
}
