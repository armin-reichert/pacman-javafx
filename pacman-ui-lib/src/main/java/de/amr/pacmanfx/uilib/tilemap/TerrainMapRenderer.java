package de.amr.pacmanfx.uilib.tilemap;

import de.amr.pacmanfx.lib.tilemap.Obstacle;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.beans.property.ObjectProperty;

import java.util.Set;

public interface TerrainMapRenderer extends CanvasRenderer {

    ObjectProperty<TerrainMapColorScheme> colorSchemeProperty();

    void draw(WorldMap worldMap, Set<Obstacle> obstacles);
}
