package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.SceneWithoutLevel;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
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
    private GameSceneRenderer sceneRenderer;
    private HUD_Renderer hudRenderer;

    public void updateRenderers(GameAppContext app, GameScene gameScene) {
        requireNonNull(gameScene);

        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig config = app.currentGameVariantUIConfig().renderConfig();

        final CanvasRenderingComp canvasRendering = gameScene.reqComp(CanvasRenderingComp.class);
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            baseRenderer = new BaseRenderer(canvas);

            entityRenderer = config.createActorRenderer(animController, canvas);
            configureRenderer(entityRenderer, canvasRendering);

            sceneRenderer = config.createGameSceneRenderer(gameScene, animController, canvas); // may be null!
            if (sceneRenderer != null) {
                configureRenderer(sceneRenderer, canvasRendering);
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> configureRenderer(debugRenderer, canvasRendering));
            }

            hudRenderer = config.createHUDRenderer(gameScene, animController, canvas);
            configureRenderer(hudRenderer, canvasRendering);
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void renderFrame(GameScene gameScene, GameSession session, long tick, boolean debugMode) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            if (canvasRendering.clearCanvasBeforeRendering()) {
                baseRenderer.clearCanvas();
            }

            if (sceneRenderer != null) {
                sceneRenderer.render(gameScene, tick);
            }

            //TODO add message into entity collection and assign suitable rendering order
            hudRenderer.drawMessage(session);

            final List<GameEntity> entities = new ArrayList<>();
            session.optLevel().ifPresent(level -> entities.addAll(level.entities().all().toList()));
            if (gameScene instanceof SceneWithoutLevel sceneWithoutLevel) {
               entities.addAll(sceneWithoutLevel.entities().selectAll().toList());
            }
            sortInRenderingOrder(entities).forEach(e -> entityRenderer.render(e, tick));

            hudRenderer.drawHUD(session.hud(), session, gameScene, tick);

            if (debugMode) {
                sceneRenderer.optDebugInfoRenderer().ifPresent(debugRenderer -> debugRenderer.render(gameScene, tick));
            }
        });
    }

    public BaseRenderer actorRenderer() {
        return entityRenderer;
    }

    public void setEntityRenderer(BaseRenderer entityRenderer) {
        this.entityRenderer = entityRenderer;
    }

    public GameSceneRenderer sceneRenderer() {
        return sceneRenderer;
    }

    public void setSceneRenderer(GameSceneRenderer sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
    }

    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    public void setHudRenderer(HUD_Renderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    private List<GameEntity> sortInRenderingOrder(Collection<GameEntity> entities) {
        return entities.stream()
            .filter(e -> e.hasComp(RenderingComp.class))
            .sorted((e1, e2) -> RenderingComp.RENDERING_ORDER.compare(
                e1.reqComp(RenderingComp.class),
                e2.reqComp(RenderingComp.class)))
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void configureRenderer(Renderer renderer, CanvasRenderingComp canvasRendering) {
        renderer.backgroundColorProperty().bind(canvasRendering.backgroundColorProperty());
        renderer.scalingProperty().bind(canvasRendering.scalingProperty());
    }
}
