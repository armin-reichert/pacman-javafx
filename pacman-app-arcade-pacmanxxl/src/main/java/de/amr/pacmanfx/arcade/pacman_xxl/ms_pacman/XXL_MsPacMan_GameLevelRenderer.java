/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class XXL_MsPacMan_GameLevelRenderer extends ArcadeMsPacMan_GameLevelRenderer {

    private final GenericLevelRenderer genericLevelRenderer;

    public XXL_MsPacMan_GameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(animSystem, canvas, null);

        genericLevelRenderer = new GenericLevelRenderer(canvas);
        genericLevelRenderer.scalingProperty().bind(scalingProperty());
        genericLevelRenderer.backgroundColorProperty().bind(backgroundColorProperty());
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameLevel level)) {
            return;
        }

        //TODO don't do this in every render frame
        final GenericWorldMapColorScheme worldMapColorScheme = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final var mapColoring = new TerrainMapColoring(
            backgroundColor(),
            Color.valueOf(worldMapColorScheme.wallFill()),
            Color.valueOf(worldMapColorScheme.wallStroke()),
            Color.valueOf(worldMapColorScheme.door())
        );
        infoMap.put(GenericLevelRenderer.RenderInfoKey.TERRAIN_MAP_COLORING, mapColoring);
        genericLevelRenderer.setInfoMap(infoMap);

        genericLevelRenderer.render(r, tick);
    }
}
