/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.rendering;


import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.uilib.view2d.TerrainMapColoring;
import javafx.scene.paint.Color;

public interface RenderingUtil {

    static TerrainMapColoring findMapColoring(GameViewModel viewModel, WorldMap worldMap) {
        final Color backgroundColor = viewModel.common2DSettings().canvasBackgroundColorProperty().get();
        final GenericWorldMapColorScheme colorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        if (colorScheme == null) {
            return null;
        }
        return new TerrainMapColoring(
            backgroundColor,
            Color.valueOf(colorScheme.wallFill()),
            Color.valueOf(colorScheme.wallStroke()),
            Color.valueOf(colorScheme.door())
        );
    }

    static Color findPelletColor(WorldMap worldMap) {
        final GenericWorldMapColorScheme foodColorScheme = worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME);
        return foodColorScheme != null ? Color.valueOf(foodColorScheme.pellet()) : null;
    }
}
