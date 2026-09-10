/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_GameLevelRendererKey;
import de.amr.pacmanfx.tengenmspacman.sprites.MapImageSet;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Common_GameLevelRendererKey;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

public class TengenMsPacMan_PlayScene2D_Renderer extends BaseRenderer implements SpriteRenderer {

    public static final int CONTENT_INDENT = 16;

    private final Renderer levelRenderer;

    public TengenMsPacMan_PlayScene2D_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);

        final var cr8 = gameScene.reqCanvasRendering();

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.scalingProperty().bind(cr8.scalingProperty());
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());

        setDebugInfoRenderer(new TengenMsPacMan_PlaySceneDebugInfoRenderer(animController, canvas));
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_PlayScene2D playScene)) {
            return;
        }

        final GameContext game = playScene.game();
        final GameSession session = game.session();

        session.optLevel().ifPresent(level -> {
            final WorldMap worldMap = level.worldMap();
            final double scaledIndent = scaled(CONTENT_INDENT);

            ctx.save();
            ctx.translate(scaledIndent, 0);

            final LevelCompletedAnimation.FlashingState flashingState = playScene.optLevelCompletedAnimation()
                .flatMap(LevelCompletedAnimation::flashingState)
                .orElse(null);

            configureLevelRenderer(levelRenderer, level, flashingState);
            levelRenderer.render(level, tick);

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

    private static void configureLevelRenderer(Renderer levelRenderer, GameLevel level, LevelCompletedAnimation.FlashingState flashingState) {
        final InfoMap info = levelRenderer.info();
        info.clear();

        final WorldMap worldMap = level.worldMap();

        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_CATEGORY);
        info.put(TengenMsPacMan_GameLevelRendererKey.MAP_CATEGORY, mapCategory);
        info.put(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET, worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET));
        if (flashingState == null) {
            info.put(Common_GameLevelRendererKey.BRIGHT, false);
            info.put(Common_GameLevelRendererKey.FLASHING_INDEX, -1);
        } else {
            info.put(Common_GameLevelRendererKey.BRIGHT, flashingState.isHighlighted());
            info.put(Common_GameLevelRendererKey.FLASHING_INDEX, flashingState.flashingIndex());
        }
    }
}