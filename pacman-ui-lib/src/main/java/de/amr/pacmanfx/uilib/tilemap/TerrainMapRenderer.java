/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

import java.util.Set;

public interface TerrainMapRenderer extends CanvasRenderer {

    TerrainMapColorScheme DEFAULT_COLOR_SCHEME = new TerrainMapColorScheme(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    ObjectProperty<TerrainMapColorScheme> colorSchemeProperty();

    void draw(WorldMap worldMap, Set<Obstacle> obstacles);
}
