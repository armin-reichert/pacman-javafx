/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_GameLevelRendererKey;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation.FlashingState;
import de.amr.pacmanfx.uilib.rendering.*;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class TengenMsPacMan_PlayScene2D_Renderer extends BaseRenderer implements SpriteRenderer {

    private final Renderer entityRenderer;
    private final Renderer levelRenderer;

    public TengenMsPacMan_PlayScene2D_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);

        final var cr8 = gameScene.reqCanvasRendering();

        entityRenderer = renderConfig.createEntityRenderer(animController, canvas);
        entityRenderer.scalingProperty().bind(cr8.scalingProperty());
        entityRenderer.backgroundColorProperty().bind(backgroundColorProperty());

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
        final double scaledSceneIndent = scaled(2*TS);
        switch (r) {
            case TengenMsPacMan_PlayScene2D playScene -> {
                final GameLevel level = playScene.game().session().optLevel().orElse(null);
                if (level != null) {
                    ctx.save();
                    ctx.translate(scaledSceneIndent, 0);
                    configureLevelRenderer(level, playScene.optLevelCompletedAnimation().orElse(null));
                    levelRenderer.render(level, tick);
                    ctx.restore();
                }
            }
            case RenderableWrapper wrapper -> {
                ctx.save();
                ctx.translate(scaledSceneIndent, 0);
                render(wrapper.content(), tick);
                ctx.restore();
            }
            default -> entityRenderer.render(r, tick);
        }
    }

    private void configureLevelRenderer(GameLevel level, LevelCompletedAnimation completedAnimation) {
        final WorldMap worldMap = level.worldMap();

        final InfoMap renderInfo = levelRenderer.info();
        renderInfo.clear();

        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_CATEGORY);
        renderInfo.put(TengenMsPacMan_GameLevelRendererKey.MAP_CATEGORY, mapCategory);
        renderInfo.put(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET, worldMap.getConfigValue(TengenMsPacMan_GameLevelRendererKey.MAP_IMAGE_SET));

        final FlashingState flashingState = completedAnimation != null
            ? completedAnimation.optFlashingState().orElse(null)
            : null;

        if (flashingState == null) {
            renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, false);
            renderInfo.put(LevelRenderInfoKey.FLASHING_INDEX, -1);
        } else {
            renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, flashingState.isHighlighted());
            renderInfo.put(LevelRenderInfoKey.FLASHING_INDEX, flashingState.flashingIndex());
        }

    }
}