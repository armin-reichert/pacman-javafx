/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.MapRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public class MiniViewRenderer extends BaseRenderer {

    private final Renderer levelRenderer;
    private final Renderer entityRenderer;

    public MiniViewRenderer(
        Canvas canvas,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameViewModel vm) {

        super(canvas);

        entityRenderer = renderConfig.createEntityRenderer(animController, canvas);
        entityRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());
        entityRenderer.scalingProperty().bind(scalingProperty());

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());
        levelRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case RenderableWrapper wrapper -> render(wrapper.wrappedRenderable(), tick);
            case GameLevel level -> {
                levelRenderer.info().put(MapRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON);
                levelRenderer.info().put(MapRenderInfoKey.BRIGHT, false);
                levelRenderer.info().put(MapRenderInfoKey.EMPTY, level.food().remainingFoodCount() == 0);
                levelRenderer.info().put(MapRenderInfoKey.FLASHING, false);
                levelRenderer.render(level, tick);
            }
            default -> entityRenderer.render(r, tick);
        }
    }
}
