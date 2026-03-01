/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GameLevel_Renderer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GenericMapRenderer;
import de.amr.pacmanfx.uilib.rendering.RenderInfo;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Renderer for "Pac-Man XXL" game variant. Uses the vector graphics map renderer that can render any custom map.
 */
public class PacManXXL_PacMan_GameLevelRenderer extends ArcadePacMan_GameLevel_Renderer {

    private final GenericMapRenderer mapRenderer;

    public PacManXXL_PacMan_GameLevelRenderer(Canvas canvas) {
        super(canvas, null);
        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
        mapRenderer.backgroundColorProperty().bind(backgroundColorProperty());
    }

    @Override
    public void applyLevelSettings(GameLevel level, RenderInfo info) {
        final WorldMapColorScheme worldMapColorScheme = level.worldMap().getConfigValue(UIConfig.WorldMapConfigKey.COLOR_SCHEME);
        final var terrainMapColorScheme = new TerrainMapColorScheme(
            backgroundColor(),
            Color.valueOf(worldMapColorScheme.wallFill()),
            Color.valueOf(worldMapColorScheme.wallStroke()),
            Color.valueOf(worldMapColorScheme.door())
        );
        info.put(GenericMapRenderer.RenderInfoKey.TERRAIN_MAP_COLOR_SCHEME, terrainMapColorScheme);
    }

    @Override
    protected void drawMap(GameLevel level, RenderInfo info) {
        mapRenderer.drawMap(level, info);
    }
}