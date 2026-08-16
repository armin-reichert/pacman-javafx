/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.MapConfigKey;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_PlayScene2D;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_WIDTH;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PlayScene2D_Renderer
    extends BaseRenderer
    implements GameScene2D_Renderer, SpriteRenderer, TengenMsPacMan_SceneRendererMixin
{
    private static final int CONTENT_INDENT = 2 * WorldMap.TS;

    private static final List<GhostPersonality> GHOSTS_Z_ORDER = List.of(
        GhostPersonality.ORANGE_GHOST_POKEY,
        GhostPersonality.CYAN_GHOST_BASHFUL,
        GhostPersonality.PINK_GHOST_SPEEDY,
        GhostPersonality.RED_GHOST_SHADOW);

    private class PlaySceneDebugInfoRenderer extends BaseDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(Canvas canvas) {
            super(canvas);
        }

        @Override
        public void draw(AbstractGameScene scene, long tick) {
            final GameSession session = scene.game().session();
            final GameState gameState = scene.game().state();
            final TengenMsPacMan_PlayScene2D playScene = (TengenMsPacMan_PlayScene2D) scene;

            drawTileGrid(NES_SCREEN_WIDTH, playScene.canvasHeightUnscaled(), Color.LIGHTGRAY);

            ctx.save();
            ctx.translate(scaled(CONTENT_INDENT), 0);
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s %d".formatted(gameState, gameState.timer().tickCount()), 0, scaled(3 * WorldMap.TS));
            session.optLevel().ifPresent(level -> {
                drawMovingActorInfo(animSystem, level.entities().pac());
                level.entities().ghosts().forEach(ghost -> drawMovingActorInfo(animSystem, ghost));
            });
            ctx.fillText("Camera y=%.2f".formatted(playScene.dynamicCamera().getTranslateY()), scaled(11* WorldMap.TS), scaled(15* WorldMap.TS));
            ctx.restore();
        }
    }

    private final SpriteAnimSystem animSystem;

    private final RenderInfo renderInfo = new RenderInfo();
    private final TengenMsPacMan_GameLevelRenderer levelRenderer;
    private final TengenMsPacMan_ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;
    private final List<GameEntity> actorsInZOrder = new ArrayList<>();

    public TengenMsPacMan_PlayScene2D_Renderer(
        GameVariantRenderConfig renderConfig, AbstractGameScene scene, SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
        levelRenderer = scene.rendering2D().configureRenderer((TengenMsPacMan_GameLevelRenderer) renderConfig.createGameLevelRenderer(animSystem, canvas));
        actorRenderer = scene.rendering2D().configureRenderer((TengenMsPacMan_ActorRenderer)     renderConfig.createActorRenderer(animSystem, canvas));
        debugRenderer = scene.rendering2D().configureRenderer(new PlaySceneDebugInfoRenderer(canvas));
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
    public void draw(AbstractGameScene scene, long tick) {
        clearCanvas();
        if (!(scene instanceof TengenMsPacMan_PlayScene2D playScene2D)) {
            return;
        }

        final GameSession session = scene.game().session();
        session.optLevel().ifPresent(level -> {
            final WorldMap worldMap = level.worldMap();
            final House house = level.entities().house();
            final double scaledIndent = scaled(CONTENT_INDENT);

            configureRenderInfo(playScene2D, worldMap, tick);
            configureActorZOrder(level);

            ctx.save();
            ctx.translate(scaledIndent, 0);
            levelRenderer.drawLevel(scene.game(), level, renderInfo);
            levelRenderer.drawDoor(house, worldMap); // ghosts appear under door, so draw door over again
            actorsInZOrder.forEach(actorRenderer::drawActor);
            ctx.restore();

            if (scene.app().ui().viewModel().debugModeOnProperty.get()) {
                debugRenderer.draw(playScene2D, tick);
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
        actorsInZOrder.add(level.entities().pac());
        GHOSTS_Z_ORDER.stream().map(level.entities()::ghost).forEach(actorsInZOrder::add);
        level.entities().optBonus().ifPresent(actorsInZOrder::add);
    }
}