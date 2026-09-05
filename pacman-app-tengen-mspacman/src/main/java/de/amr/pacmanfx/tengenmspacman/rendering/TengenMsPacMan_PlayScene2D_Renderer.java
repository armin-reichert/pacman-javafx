/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.MapConfigKey;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PlayScene2D_Renderer extends GameSceneRenderer
    implements SpriteRenderer, TengenMsPacMan_SceneRendererMixin
{
    public static final int CONTENT_INDENT = 16;

    private class PlaySceneDebugInfoRenderer extends BaseGameSceneDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(ActorSpriteAnimController animController, Canvas canvas) {
            super(animController, canvas);
        }

        @Override
        public void render(Object r, long tick) {
            if (!(r instanceof TengenMsPacMan_PlayScene2D playScene)) {
                return;
            }

            final GameContext game = playScene.game();
            final GameSession session = game.session();
            final AbstractGameState gameState = game.state();

            drawTileGrid(NES_SCREEN_WIDTH, playScene.canvasHeightUnscaled(), Color.LIGHTGRAY);

            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState.name(), gameState.timer().tickCount()), 0, scaled(3 * WorldMap.TS));
            session.optLevel().ifPresent(level -> {
                drawMovingActorInfo(animController, level.entities().pac());
                level.entities().ghosts().forEach(ghost -> drawMovingActorInfo(animController, ghost));
            });
            ctx.fillText("Camera y=%.2f".formatted(playScene.dynamicCamera().getTranslateY()), scaled(11* WorldMap.TS), scaled(15* WorldMap.TS));
            ctx.restore();
        }
    }

    private final ActorSpriteAnimController animController;

    private final InfoMap infoMap = new InfoMap();
    private final TengenMsPacMan_GameLevelRenderer levelRenderer;

    public TengenMsPacMan_PlayScene2D_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);

        final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);
        this.animController = requireNonNull(animController);

        levelRenderer = r2D.configureRenderer((TengenMsPacMan_GameLevelRenderer) renderConfig.createGameLevelRenderer(animController, canvas));
        setDebugInfoRenderer(new PlaySceneDebugInfoRenderer(animController, canvas));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public Renderer renderer() {
        return this;
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof TengenMsPacMan_PlayScene2D playScene)) {
            return;
        }

        final GameContext game = playScene.game();
        final GameSession session = game.session();

        session.optLevel().ifPresent(level -> {
            final WorldMap worldMap = level.worldMap();
            final House house = level.entities().house();
            final double scaledIndent = scaled(CONTENT_INDENT);

            ctx.save();
            ctx.translate(scaledIndent, 0);

            configureRenderInfo(playScene, worldMap, tick);
            levelRenderer.setInfoMap(infoMap);
            levelRenderer.render(level, tick);
            levelRenderer.drawDoor(house, worldMap); // ghosts appear under door, so draw door over again

            ctx.restore();

            // All maps are 28 tiles wide but the NES screen is 32 tiles wide.
            // To accommodate, the maps are centered horizontally and 2 tiles on each side are clipped.
            final double stripeHeight = ctx.getCanvas().getHeight();
            ctx.save();

            ctx.setFill(backgroundColor());
            ctx.fillRect(0, 0, scaledIndent, stripeHeight);
            ctx.fillRect(ctx.getCanvas().getWidth() - scaledIndent, 0, scaledIndent, stripeHeight);

            ctx.restore();
        });
    }

    private void configureRenderInfo(TengenMsPacMan_PlayScene2D playScene2D, WorldMap worldMap, long tick) {
        infoMap.clear();
        // this is needed for drawing animated maze with different images:
        infoMap.put(CommonRenderInfoKey.TICK, tick);
        infoMap.put(MapConfigKey.MAP_CATEGORY, worldMap.getConfigValue(MapConfigKey.MAP_CATEGORY));
        infoMap.put(CommonRenderInfoKey.MAP_BRIGHT, false);
        infoMap.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, -1);
        playScene2D.optLevelCompletedAnimation().flatMap(LevelCompletedAnimation::flashingState).ifPresent(flashingState -> {
            infoMap.put(CommonRenderInfoKey.MAP_BRIGHT, flashingState.isHighlighted());
            infoMap.put(CommonRenderInfoKey.MAZE_FLASHING_INDEX, flashingState.flashingIndex());
        });
    }
}