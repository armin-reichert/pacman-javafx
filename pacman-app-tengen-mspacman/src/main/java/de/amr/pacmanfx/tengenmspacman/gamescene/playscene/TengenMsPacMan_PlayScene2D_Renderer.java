/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_LevelRenderInfoKey;
import de.amr.pacmanfx.tengenmspacman.sprites.MapImageSet;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class TengenMsPacMan_PlayScene2D_Renderer extends BaseRenderer {

    private final Renderer entityRenderer;
    private final Renderer levelRenderer;

    public TengenMsPacMan_PlayScene2D_Renderer(
        TengenMsPacMan_PlayScene2D playScene,
        GameVariantRenderConfig renderConfig,
        ActorSpriteAnimController animController,
        Canvas canvas) {

        super(canvas);

        final GameSceneCanvasRenderingComp canvasRendering = playScene.reqCanvasRendering();

        entityRenderer = renderConfig.createEntityRenderer(animController, canvas);
        entityRenderer.scalingProperty().bind(canvasRendering.scalingProperty());
        entityRenderer.backgroundColorProperty().bind(backgroundColorProperty());

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.scalingProperty().bind(canvasRendering.scalingProperty());
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        // NES screen width = 32 tiles but map width is only 28 tiles, so adjust:
        final double xOffset = scaled(2*TS);
        switch (r) {
            case TengenMsPacMan_PlayScene2D playScene -> {
                final GameLevel level = playScene.game().session().optLevel().orElse(null);
                if (level != null) {
                    final var flashing = playScene.flashingState();
                    configureLevelRenderer(level.worldMap(),
                        flashing != null && flashing.isHighlighted(),
                        flashing != null ? flashing.flashingIndex() : -1);

                    ctx.save();
                    ctx.translate(xOffset, 0);
                    levelRenderer.render(level, tick);
                    ctx.restore();
                }
            }
            case RenderableWrapper wrapper -> {
                // Game entities are wrapped to SCENE layer so they are rendered using this renderer
                // and not by the global entity renderer from the render manager
                ctx.save();
                ctx.translate(xOffset, 0);
                render(wrapper.content(), tick);
                ctx.restore();
            }
            default -> {
                ctx.save();
                clipLeftAndRight(xOffset);
                entityRenderer.render(r, tick);
                ctx.restore();
            }
        }
    }

    private void clipLeftAndRight(double margin) {
        // -1 to clip 1 pixel wide vertical stripe on tne right (hide ugly map sprite border)
        ctx.getCanvas().setClip(new Rectangle(margin, 0, canvas().getWidth() - 2 * margin - 1, canvas().getHeight()));
    }

    private void configureLevelRenderer(WorldMap worldMap, boolean highlighted, int flashingIndex) {
        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_CATEGORY);
        final MapImageSet mapImageSet = worldMap.getConfigValue(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET);
        levelRenderer.info().clear();
        levelRenderer.info().put(TengenMsPacMan_LevelRenderInfoKey.MAP_CATEGORY, mapCategory);
        levelRenderer.info().put(TengenMsPacMan_LevelRenderInfoKey.MAP_IMAGE_SET, mapImageSet);
        levelRenderer.info().put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, highlighted);
        levelRenderer.info().put(LevelRenderInfoKey.FLASHING_INDEX, flashingIndex);
        levelRenderer.info().put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, highlighted);
        levelRenderer.info().put(LevelRenderInfoKey.FLASHING_INDEX, flashingIndex);
    }
}