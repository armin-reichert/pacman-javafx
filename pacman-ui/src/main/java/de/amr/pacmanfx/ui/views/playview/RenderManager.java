/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.SceneWithoutLevel;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private BaseRenderer baseRenderer;
    private BaseRenderer entityRenderer;
    private BaseRenderer sceneRenderer;
    private HUD_Renderer hudRenderer;
    private BaseRenderer messageViewRenderer;

    public void updateRenderers(GameAppContext app, GameScene gameScene) {
        requireNonNull(gameScene);

        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig config = app.currentGameVariantUIConfig().renderConfig();

        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            baseRenderer = new BaseRenderer(canvas) {
                @Override
                public void render(Object r, long tick) {}
            };

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
                baseRenderer.clearCanvas();
            }

            if (sceneRenderer != null) {
                sceneRenderer.render(gameScene, tick);
            }

            final List<GameEntity> entities = new ArrayList<>();
            session.optLevel().ifPresent(level -> {
                entities.addAll(level.entities().all().toList());
                if (level.entities().theMessageView() != null) {
                    messageViewRenderer.render(level.entities().theMessageView(), tick);
                }
            });

            if (gameScene instanceof SceneWithoutLevel sceneWithoutLevel) {
               entities.addAll(sceneWithoutLevel.entities().selectAll().toList());
            }
            sortInRenderingOrder(entities).forEach(e -> entityRenderer.render(e, tick));

            if (session.hud().isVisible()) {
                //TODO get rid of this:
                hudRenderer.drawHUD(session.hud(), session, gameScene, tick);
                session.hud().entities().forEach(hudEntity -> hudRenderer.drawHUDEntity(hudEntity, game));
            }

            if (debugMode) {
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(gameScene, tick));
            }
        });
    }

    private List<GameEntity> sortInRenderingOrder(Collection<GameEntity> entities) {
        return entities.stream()
            .filter(e -> e.hasComp(RenderingComp.class))
            .sorted((e1, e2) -> RenderingComp.RENDERING_ORDER.compare(
                e1.reqComp(RenderingComp.class),
                e2.reqComp(RenderingComp.class)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void configureRenderer(Renderer renderer, SceneCanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
