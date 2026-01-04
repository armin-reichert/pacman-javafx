/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_PlayScene2D.CANVAS_WIDTH_UNSCALED;

public class TengenMsPacMan_PlayScene2D_Renderer extends GameScene2D_Renderer
    implements SpriteRenderer, TengenMsPacMan_SceneRenderingCommons {

    private static final List<Byte> GHOSTS_Z_ORDER = List.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW);

    private static final float CONTENT_INDENT = TS(2);

    private static class PlaySceneDebugInfoRenderer extends BaseDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(UIPreferences prefs, Canvas canvas) {
            super(prefs, canvas);
        }

        @Override
        public void draw(GameScene2D scene) {
            final Game game = scene.context().currentGame();
            final TengenMsPacMan_PlayScene2D playScene = (TengenMsPacMan_PlayScene2D) scene;
            final StateMachine.State<Game> gameState = game.control().state();

            drawTileGrid(CANVAS_WIDTH_UNSCALED, playScene.canvasHeightUnscaled(), Color.LIGHTGRAY);

            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState, gameState.timer().tickCount()), 0, scaled(3 * TS));
            game.optGameLevel().ifPresent(gameLevel -> {
                drawMovingActorInfo(gameLevel.pac());
                gameLevel.ghosts().forEach(this::drawMovingActorInfo);
            });
            ctx.fillText("Camera y=%.2f".formatted(playScene.dynamicCamera().getTranslateY()), scaled(11*TS), scaled(15*TS));
            ctx.restore();
        }
    }

    private final RenderInfo renderInfo = new RenderInfo();
    private final TengenMsPacMan_GameLevelRenderer levelRenderer;
    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final List<Actor> actorsInZOrder = new ArrayList<>();
    private final Rectangle clipRect;

    public TengenMsPacMan_PlayScene2D_Renderer(GameUI_Config uiConfig, UIPreferences prefs, GameScene2D scene, Canvas canvas) {
        super(canvas);

        levelRenderer = (TengenMsPacMan_GameLevelRenderer) adaptRenderer(uiConfig.createGameLevelRenderer(canvas), scene);
        actorRenderer = (TengenMsPacMan_ActorRenderer) adaptRenderer(uiConfig.createActorRenderer(canvas), scene);
        debugRenderer = adaptRenderer(new PlaySceneDebugInfoRenderer(prefs, canvas), scene);

        // All maps are 28 tiles wide but the NES screen is 32 tiles wide. To accommodate, the maps are centered
        // horizontally and 2 tiles on each side are clipped.
        clipRect = new Rectangle();
        clipRect.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(CONTENT_INDENT)));
        clipRect.yProperty().bind(canvas.translateYProperty());
        clipRect.widthProperty().bind(scalingProperty().multiply(CANVAS_WIDTH_UNSCALED - 2 * CONTENT_INDENT));
        clipRect.heightProperty().bind(canvas.heightProperty());
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.INSTANCE;
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        scene.context().currentGame().optGameLevel().ifPresent(level -> {
            configureRenderInfo(scene, level.worldMap());
            configureActorZOrder(level);
            ctx.getCanvas().setClip(clipRect);
            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            levelRenderer.drawLevel(level, renderInfo);
            levelRenderer.drawDoor(level.worldMap()); // ghosts appear under door, so draw door over again
            actorsInZOrder.forEach(actorRenderer::drawActor);
            ctx.restore();
            if (scene.debugInfoVisible()) {
                ctx.getCanvas().setClip(null); // also show normally clipped region (to see how Pac-Man travels through portals)
                debugRenderer.draw(scene);
            }
        });
    }

    private void configureRenderInfo(GameScene2D scene, WorldMap worldMap) {
        final TengenMsPacMan_PlayScene2D playScene = (TengenMsPacMan_PlayScene2D) scene;
        final long tick = playScene.ui().clock().tickCount();

        renderInfo.clear();
        // this is needed for drawing animated maze with different images:
        renderInfo.put(CommonRenderInfoKey.TICK, tick);
        renderInfo.put(TengenMsPacMan_UIConfig.ConfigKey.MAP_CATEGORY, worldMap.getConfigValue(TengenMsPacMan_UIConfig.ConfigKey.MAP_CATEGORY));
        if (playScene.levelCompletedAnimation() != null && playScene.isMazeHighlighted()) {
            renderInfo.put(CommonRenderInfoKey.MAP_BRIGHT, true);
            renderInfo.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, playScene.levelCompletedAnimation().flashingIndex());
        } else {
            renderInfo.put(CommonRenderInfoKey.MAP_BRIGHT, false);
        }
    }

    private void configureActorZOrder(GameLevel level) {
        actorsInZOrder.clear();
        level.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(level.pac());
        GHOSTS_Z_ORDER.stream().map(level::ghost).forEach(actorsInZOrder::add);
    }
}