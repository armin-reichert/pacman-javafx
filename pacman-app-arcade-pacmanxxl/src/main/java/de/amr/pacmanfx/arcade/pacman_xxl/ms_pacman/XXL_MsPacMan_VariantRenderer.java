/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_RenderConfig;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_VariantRenderer;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import javafx.scene.canvas.Canvas;

public class XXL_MsPacMan_VariantRenderer extends ArcadeMsPacMan_VariantRenderer {

    private final GenericLevelRenderer levelRenderer;

    public XXL_MsPacMan_VariantRenderer(ActorSpriteAnimController animController, ArcadeMsPacMan_RenderConfig renderConfig, Canvas canvas) {
        super(animController, renderConfig, canvas);

        levelRenderer = new GenericLevelRenderer(canvas);
        levelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        levelRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameLevelView(GameLevel _, InfoMap _, RenderingLayer _, int _, Vector2f _),
                 GameEntityView(House _, RenderingLayer _, int _, Vector2f _, InfoMap _),
                 GameEntityView(Energizer _, RenderingLayer _, int _, Vector2f _, InfoMap _)
                -> levelRenderer.render(r, tick);

            default -> super.render(r, tick);
        }
    }
}
