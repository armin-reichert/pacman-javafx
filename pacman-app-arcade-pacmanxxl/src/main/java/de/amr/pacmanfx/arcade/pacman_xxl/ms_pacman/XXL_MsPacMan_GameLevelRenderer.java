/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.ui.gamescene.d2.GenericMapRenderer;
import de.amr.basics.InfoMap;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public class XXL_MsPacMan_GameLevelRenderer extends ArcadeMsPacMan_GameLevelRenderer {

    private final GenericMapRenderer mapRenderer;

    public XXL_MsPacMan_GameLevelRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(animSystem, canvas, null);

        mapRenderer = new GenericMapRenderer(canvas);
        mapRenderer.scalingProperty().bind(scalingProperty());
        mapRenderer.backgroundColorProperty().bind(backgroundColorProperty());
    }

    @Override
    public void applyLevelSettings(GameRules rules, GameLevel level, InfoMap renderInfo) {
        final GenericWorldMapColorScheme worldMapColorScheme = level.worldMap().getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        final var mapColoring = new TerrainMapColoring(
            backgroundColor(),
            Color.valueOf(worldMapColorScheme.wallFill()),
            Color.valueOf(worldMapColorScheme.wallStroke()),
            Color.valueOf(worldMapColorScheme.door())
        );
        renderInfo.put(GenericMapRenderer.RenderInfoKey.TERRAIN_MAP_COLORING, mapColoring);
    }

    @Override
    protected void drawMap(GameLevel level, InfoMap info) {
        mapRenderer.drawMap(level, info);
    }
}
