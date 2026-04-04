/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

public interface TerrainMapRenderer extends Renderer {

    TerrainMapColoring DEFAULT_MAP_COLORING = new TerrainMapColoring(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    ObjectProperty<TerrainMapColoring> mapColoringProperty();

    void draw(WorldMap worldMap);
}
