/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.playview;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

public class MiniViewRenderer {

    // Note: The level and actor renderers cannot be created in the constructor, because the game controller has not yet
    //       selected a game variant when the constructor is called, so no variant configuration is available yet!
    private final BaseRenderer levelRenderer;
    private final BaseRenderer entityRenderer;

    public MiniViewRenderer(
        Canvas canvas,
        ActorSpriteAnimController animController,
        GameVariantRenderConfig renderConfig,
        GameViewModel vm) {

        entityRenderer = renderConfig.createEntityRenderer(animController, canvas);
        entityRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());

        levelRenderer = renderConfig.createGameLevelRenderer(animController, canvas);
        levelRenderer.backgroundColorProperty().bind(vm.common2DSettings().canvasBackgroundColorProperty());
    }

    public BaseRenderer entityRenderer() {
        return entityRenderer;
    }

    public BaseRenderer levelRenderer() {
        return levelRenderer;
    }
}
