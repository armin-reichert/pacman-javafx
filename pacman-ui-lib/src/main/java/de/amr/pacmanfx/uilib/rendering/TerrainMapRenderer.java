/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;

public interface TerrainMapRenderer extends Renderer {

    TerrainMapColorScheme DEFAULT_COLOR_SCHEME = new TerrainMapColorScheme(Color.BLACK, Color.RED,  Color.GOLD, Color.PINK);

    ObjectProperty<TerrainMapColorScheme> colorSchemeProperty();

    void draw(WorldMap worldMap);
}
