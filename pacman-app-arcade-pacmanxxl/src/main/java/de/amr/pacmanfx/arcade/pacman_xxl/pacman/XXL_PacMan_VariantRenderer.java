/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.pacman;


import de.amr.basics.MapWithAccessors;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_RenderConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_VariantRenderer;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import javafx.scene.canvas.Canvas;

public class XXL_PacMan_VariantRenderer extends ArcadePacMan_VariantRenderer {

    private final GenericLevelRenderer genericLevelRenderer;

    public XXL_PacMan_VariantRenderer(ActorSpriteAnimController animController, ArcadePacMan_RenderConfig renderConfig, Canvas canvas) {
        super(animController, renderConfig, canvas);

        genericLevelRenderer = new GenericLevelRenderer(canvas);
        genericLevelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        genericLevelRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevelView(GameLevel level, MapWithAccessors renderInfo, RenderingLayer _, int _, Vector2f _)
                -> genericLevelRenderer.render(r, tick);

            case GameEntityView(House house, RenderingLayer _, int _, Vector2f _, MapWithAccessors renderInfo)
                -> genericLevelRenderer.render(r, tick);

            case GameEntityView(Energizer energizer, RenderingLayer _, int _, Vector2f _, MapWithAccessors renderInfo)
                -> genericLevelRenderer.render(r, tick);

            default -> super.render(r, tick);
        }
    }
}
