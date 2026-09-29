/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;

public class MiniViewOverlayRenderer extends BaseRenderer {

    private final Renderer variantRenderer;

    public MiniViewOverlayRenderer(MiniPlaySceneView miniView, ActorSpriteAnimController animController, GameVariantRenderConfig renderConfig) {

        super(miniView.canvas());

        backgroundColorProperty().bind(miniView.viewModel().common2DSettings().canvasBackgroundColorProperty());
        scalingProperty().bind(miniView.scalingProperty());

        variantRenderer = renderConfig.createVariantRenderer(animController, miniView.canvas());
        variantRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        variantRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        variantRenderer.render(r, tick);
    }
}
