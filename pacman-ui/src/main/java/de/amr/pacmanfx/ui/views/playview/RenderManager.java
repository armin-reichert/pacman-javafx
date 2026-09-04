package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.RenderingComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.HUD_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.FontSmoothingType;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class RenderManager {

    private BaseRenderer entityRenderer;
    private BaseRenderer sceneRenderer;
    private HUD_Renderer hudRenderer;
    private BaseRenderer debugRenderer;

    public void updateRenderers(GameAppContext app, GameScene gameScene) {
        requireNonNull(gameScene);

        final CanvasRenderingComp canvasRendering = gameScene.reqComp(CanvasRenderingComp.class);
        final ActorSpriteAnimController animController = app.game().variant().systems().actorSpriteAnimController();
        final GameVariantRenderConfig renderConfig = app.currentGameVariantUIConfig().renderConfig();
        final Canvas canvas = canvasRendering.canvas();

        if (canvas != null) {
            setEntityRenderer(renderConfig.createActorRenderer(animController, canvas));
            setSceneRenderer(renderConfig.createGameSceneRenderer(gameScene, animController, canvas));
            setHudRenderer(renderConfig.createHUDRenderer(gameScene, animController, canvas)); // may return null!
            setDebugRenderer(new BaseGameSceneDebugInfoRenderer(animController, canvas)); //TODO handle scene-specific debug renderer

            configureRenderer(entityRenderer, canvasRendering);
            configureRenderer(sceneRenderer,  canvasRendering);
            configureRenderer(hudRenderer,    canvasRendering);
            configureRenderer(debugRenderer,  canvasRendering);

            setGameSceneFontSmoothing(app.ui().viewModel().common2DSettings().fontSmoothingOnProperty().get());
        } else {
            Logger.error("Cannot create game scene and HUD renderer: no canvas has been assigned");
        }
    }

    public void renderFrame(GameScene gameScene, GameSession session, long tick) {
        gameScene.optCanvasRendering().ifPresent(canvasRendering -> {
            if (canvasRendering.clearCanvasBeforeRendering()) {
                sceneRenderer.clearCanvas();
            }
            sceneRenderer.render(gameScene, tick);
            hudRenderer.drawHUD(session.hud(), session, gameScene, tick);
            hudRenderer.drawMessage(session);

            session.optLevel().ifPresent(level -> entitiesInRenderingOrder(level.entities()).forEach(
                actor -> entityRenderer.render(actor, tick)));

            if (gameScene.viewModel().debugModeOnProperty().get()) {
                debugRenderer().render(gameScene, tick);
            }
        });
    }

    public void setGameSceneFontSmoothing(boolean smoothing) {
        sceneRenderer.ctx().setFontSmoothingType(smoothing ? FontSmoothingType.LCD : FontSmoothingType.GRAY);
    }

    public BaseRenderer actorRenderer() {
        return entityRenderer;
    }

    public void setEntityRenderer(BaseRenderer entityRenderer) {
        this.entityRenderer = entityRenderer;
    }

    public BaseRenderer sceneRenderer() {
        return sceneRenderer;
    }

    public void setSceneRenderer(BaseRenderer sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
    }

    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    public void setHudRenderer(HUD_Renderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public BaseRenderer debugRenderer() {
        return debugRenderer;
    }

    public void setDebugRenderer(BaseRenderer debugRenderer) {
        this.debugRenderer = debugRenderer;
    }

    private List<GameEntity> entitiesInRenderingOrder(GameLevelEntitySet entities) {
        return entities.all()
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
