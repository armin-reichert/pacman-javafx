/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class XXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevel_Renderer {

    private final GenericLevelRenderer genericLevelRenderer;

    public XXL_PacMan_GameLevelRenderer(Canvas canvas) {
        super(canvas, null);

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