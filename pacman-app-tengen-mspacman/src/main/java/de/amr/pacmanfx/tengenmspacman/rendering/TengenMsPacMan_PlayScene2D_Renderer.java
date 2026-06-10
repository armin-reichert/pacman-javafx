/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.MapConfigKey;
import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.ui.Globals_GameUI;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

public class TengenMsPacMan_PlayScene2D_Renderer
    extends BaseRenderer
    implements GameScene2D_Renderer, SpriteRendererMixin, TengenMsPacMan_SceneRendererMixin
{
    private static final int CONTENT_INDENT = 2 * WorldMap.TS;
    private static final List<Byte> GHOSTS_Z_ORDER = List.of(GameModel.ORANGE_GHOST_POKEY, GameModel.CYAN_GHOST_BASHFUL, GameModel.PINK_GHOST_SPEEDY, GameModel.RED_GHOST_SHADOW);

    private static class PlaySceneDebugInfoRenderer extends BaseDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(Canvas canvas) {
            super(canvas);
        }

        @Override
        public void draw(GameScene2D scene) {
            final GameModel game = scene.gameModel();
            final GameState gameState = scene.gameState();
            final TengenMsPacMan_PlayScene2D playScene = (TengenMsPacMan_PlayScene2D) scene;

            drawTileGrid(NES_SCREEN_WIDTH, playScene.canvasHeightUnscaled(), Color.LIGHTGRAY);

            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState, gameState.timer().tickCount()), 0, scaled(3 * WorldMap.TS));
            game.optGameLevel().ifPresent(level -> {
                drawMovingActorInfo(level.entities().pac());
                level.entities().ghosts().forEach(this::drawMovingActorInfo);
            });
            ctx.fillText("Camera y=%.2f".formatted(playScene.dynamicCamera().getTranslateY()), scaled(11* WorldMap.TS), scaled(15* WorldMap.TS));
            ctx.restore();
        }
    }

    private final RenderInfo renderInfo = new RenderInfo();
    private final TengenMsPacMan_GameLevelRenderer levelRenderer;
    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;
    private final List<Actor> actorsInZOrder = new ArrayList<>();

    public TengenMsPacMan_PlayScene2D_Renderer(UIConfig uiConfig, GameScene2D scene, Canvas canvas) {
        super(canvas);
        levelRenderer = scene.configureRenderer((TengenMsPacMan_GameLevelRenderer) uiConfig.createGameLevelRenderer(canvas));
        actorRenderer = scene.configureRenderer((TengenMsPacMan_ActorRenderer)     uiConfig.createActorRenderer(canvas));
        debugRenderer = scene.configureRenderer(new PlaySceneDebugInfoRenderer(canvas));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        if (!(scene instanceof TengenMsPacMan_PlayScene2D playScene2D)) {
            return;
        }
        final GameModel game = playScene2D.gameModel();
        final long tick = playScene2D.game().clock().tickCount();

        game.optGameLevel().ifPresent(level -> {
            final WorldMap worldMap = level.worldMap();
            final double scaledIndent = scaled(CONTENT_INDENT);

            configureRenderInfo(playScene2D, worldMap, tick);
            configureActorZOrder(level);

            ctx.save();
            ctx.translate(scaledIndent, 0);
            levelRenderer.drawLevel(level, renderInfo);
            levelRenderer.drawDoor(worldMap); // ghosts appear under door, so draw door over again
            actorsInZOrder.forEach(actorRenderer::drawActor);
            ctx.restore();

            if (Globals_GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
                debugRenderer.draw(playScene2D);
            }
            else {
                // All maps are 28 tiles wide but the NES screen is 32 tiles wide.
                // To accommodate, the maps are centered horizontally and 2 tiles on each side are clipped.
                final double stripeHeight = ctx.getCanvas().getHeight();
                ctx.save();
                ctx.setFill(backgroundColor());
                ctx.fillRect(0, 0, scaledIndent, stripeHeight);
                ctx.fillRect(ctx.getCanvas().getWidth() - scaledIndent, 0, scaledIndent, stripeHeight);
                ctx.restore();
            }
        });
    }

    private void configureRenderInfo(TengenMsPacMan_PlayScene2D playScene2D, WorldMap worldMap, long tick) {
        renderInfo.clear();
        // this is needed for drawing animated maze with different images:
        renderInfo.put(CommonRenderInfoKey.TICK, tick);
        renderInfo.put(MapConfigKey.MAP_CATEGORY, worldMap.getConfigValue(MapConfigKey.MAP_CATEGORY));
        renderInfo.put(CommonRenderInfoKey.MAP_BRIGHT, false);
        renderInfo.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, -1);
        playScene2D.optLevelCompletedAnimation().flatMap(LevelCompletedAnimation::flashingState).ifPresent(flashingState -> {
            renderInfo.put(CommonRenderInfoKey.MAP_BRIGHT, flashingState.isHighlighted());
            renderInfo.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, flashingState.flashingIndex());
        });
    }

    private void configureActorZOrder(GameLevel level) {
        actorsInZOrder.clear();
        level.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(level.entities().pac());
        GHOSTS_Z_ORDER.stream().map(level::ghost).forEach(actorsInZOrder::add);
    }
}