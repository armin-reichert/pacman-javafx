/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private BaseRenderer entityRenderer;
    private BaseRenderer sceneRenderer;
    private BaseRenderer hudRenderer;
    private BaseRenderer messageViewRenderer;

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

            messageViewRenderer = config.createMessageViewRenderer(canvas);
            configureRenderer(messageViewRenderer, canvasRendering);
        }
        else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void renderFrame(GameScene gameScene, GameContext game, long tick, boolean debugMode) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            final GameSession session = game.session();

            if (canvasRendering.clearCanvasBeforeRendering()) {
                entityRenderer.clearCanvas();
            }

            if (sceneRenderer != null) {
                sceneRenderer.render(gameScene, tick);
            }

            gameScene.renderables().sorted(Renderable.RENDERING_ORDER).forEach(e -> entityRenderer.render(e, tick));

            if (session.hud().isVisible()) {
                session.hud().entities().forEach(hudEntity -> hudRenderer.render(hudEntity, tick));
            }

            if (debugMode) {
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(gameScene, tick));
            }
        });
    }

    private void configureRenderer(Renderer renderer, SceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
