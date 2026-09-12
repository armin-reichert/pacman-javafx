/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import de.amr.pacmanfx.uilib.rendering.Renderer;

public class MiniPlaySceneViewRenderer extends BaseRenderer {

    private final Renderer levelRenderer;
    private final Renderer entityRenderer;

    public MiniPlaySceneViewRenderer(
        MiniPlaySceneView miniView,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig) {

        super(miniView.canvas());

        backgroundColorProperty().bind(miniView.viewModel().common2DSettings().canvasBackgroundColorProperty());
        scalingProperty().bind(miniView.scalingProperty());

        entityRenderer = renderConfig.createEntityRenderer(animController, miniView.canvas());
        entityRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        entityRenderer.scalingProperty().bind(scalingProperty());

        levelRenderer = renderConfig.createGameLevelRenderer(animController, miniView.canvas());
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        levelRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevel level -> {
                levelRenderer.info().put(LevelRenderInfoKey.ENERGIZERS_SHOWN, level.heartbeat().state() == Pulse.State.ON);
                levelRenderer.info().put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, false);
                levelRenderer.info().put(LevelRenderInfoKey.SHOW_EMPTY_MAZE, level.food().remainingFoodCount() == 0);
                levelRenderer.info().put(LevelRenderInfoKey.MAZE_IS_FLASHING, false);
                levelRenderer.render(level, tick);
            }
            case RenderableWrapper wrapper -> render(wrapper.wrappedRenderable(), tick);
            default -> entityRenderer.render(r, tick);
        }
    }
}
