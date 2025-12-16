/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_PlayScene2D.CANVAS_WIDTH_UNSCALED;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PlayScene2D_Renderer extends GameScene2D_Renderer
    implements SpriteRenderer, TengenMsPacMan_CommonSceneRenderingFunctions {

    private static final float CONTENT_INDENT = TS(2);

    private static class PlaySceneDebugInfoRenderer extends BaseDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(GameScene2D playScene, Canvas canvas) {
            super(playScene.ui(), canvas);
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

    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final RenderInfo gameLevelRenderInfo = new RenderInfo();
    private final TengenMsPacMan_GameLevelRenderer gameLevelRenderer;
    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final List<Actor> actorsInZOrder = new ArrayList<>();

    private final Rectangle clipRect;

    public TengenMsPacMan_PlayScene2D_Renderer(GameScene2D scene, Canvas canvas, TengenMsPacMan_SpriteSheet spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);

        final TengenMsPacMan_UIConfig uiConfig = scene.ui().currentConfig();

        gameLevelRenderer = adaptRenderer(
            uiConfig.createGameLevelRenderer(canvas), scene);

        actorRenderer = adaptRenderer(
            uiConfig.createActorRenderer(canvas), scene);

        debugRenderer = adaptRenderer(
            new PlaySceneDebugInfoRenderer(scene, canvas), scene);

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
        return spriteSheet;
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        scene.context().currentGame().optGameLevel().ifPresent(gameLevel -> {
            ctx.getCanvas().setClip(clipRect);
            drawGameLevel(scene, gameLevel);
            if (scene.debugInfoVisible()) {
                ctx.getCanvas().setClip(null); // also show normally clipped region (to see how Pac-Man travels through portals)
                debugRenderer.draw(scene);
            }
        });
    }

    private void drawGameLevel(GameScene2D scene, GameLevel gameLevel) {
        final TengenMsPacMan_PlayScene2D playScene = (TengenMsPacMan_PlayScene2D) scene;
        final long tick = playScene.ui().clock().tickCount();

        gameLevelRenderInfo.clear();
        // this is needed for drawing animated maze with different images:
        gameLevelRenderInfo.put(CommonRenderInfoKey.TICK, tick);
        gameLevelRenderInfo.put(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY,
            gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.CONFIG_KEY_MAP_CATEGORY));
        if (playScene.levelCompletedAnimation() != null && playScene.isMazeHighlighted()) {
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_BRIGHT, true);
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, playScene.levelCompletedAnimation().flashingIndex());
        } else {
            gameLevelRenderInfo.put(CommonRenderInfoKey.MAZE_BRIGHT, false);
        }
        ctx.save();
        ctx.translate(scaled(CONTENT_INDENT), 0);
        gameLevelRenderer.drawGameLevel(gameLevel, gameLevelRenderInfo);

        actorsInZOrder.clear();
        gameLevel.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        ghostsInZOrder(gameLevel).forEach(actorsInZOrder::add);
        actorsInZOrder.forEach(actorRenderer::drawActor);

        gameLevelRenderer.drawDoor(gameLevel.worldMap()); // ghosts appear under door when accessing house!
        ctx.restore();
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel gameLevel) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost);
    }
}