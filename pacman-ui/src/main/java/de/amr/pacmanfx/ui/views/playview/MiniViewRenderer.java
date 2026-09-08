/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.basics.InfoMap;
import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.CommonRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableWrapper;
import javafx.scene.canvas.Canvas;

import java.util.Map;

public class MiniViewRenderer extends BaseRenderer {

    private final BaseRenderer levelRenderer;
    private final BaseRenderer entityRenderer;

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
                final InfoMap infoMap = new InfoMap();
                infoMap.putAll(Map.of(
                    CommonRenderInfoKey.ENERGIZER_VISIBLE, level.heartbeat().state() == Pulse.State.ON,
                    CommonRenderInfoKey.MAP_BRIGHT, false,
                    CommonRenderInfoKey.MAP_EMPTY, level.food().remainingFoodCount() == 0,
                    CommonRenderInfoKey.MAP_FLASHING, false,
                    CommonRenderInfoKey.TICK, tick
                ));
                levelRenderer.setInfoMap(infoMap);
                levelRenderer.render(level, tick);
            }
            default -> entityRenderer.render(r, tick);
        }
    }
}
