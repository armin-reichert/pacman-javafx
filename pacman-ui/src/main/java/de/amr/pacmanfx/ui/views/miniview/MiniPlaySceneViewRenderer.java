/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.miniview;

import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.rendering.RenderableObject;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.uilib.rendering.RenderableGameLevel;

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

        entityRenderer = renderConfig.createVariantRenderer(animController, miniView.canvas());
        entityRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        entityRenderer.scalingProperty().bind(scalingProperty());

        levelRenderer = renderConfig.createGameLevelRenderer(animController, miniView.canvas());
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        levelRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case RenderableGameLevel _ -> levelRenderer.render(r, tick);
            case RenderableObject wrapper -> render(wrapper.content(), tick);
            default -> entityRenderer.render(r, tick);
        }
    }
}
