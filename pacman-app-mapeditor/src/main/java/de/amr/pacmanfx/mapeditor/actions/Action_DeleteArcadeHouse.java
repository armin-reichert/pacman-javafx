/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.world.TerrainTile;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.model.world.WorldMapPropertyName;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import org.tinylog.Logger;

public class Action_DeleteArcadeHouse extends EditorAction<Void> {

    public Action_DeleteArcadeHouse(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();

        Vector2i minTile = worldMap.terrainLayer().getTileProperty(WorldMapPropertyName.POS_HOUSE_MIN_TILE);
        if (minTile == null) {
            Logger.error("Cannot delete Arcade house, minTile is null");
            return null;
        }

        Vector2i maxTile = worldMap.terrainLayer().getTileProperty(WorldMapPropertyName.POS_HOUSE_MAX_TILE);
        if (maxTile == null) {
            Logger.error("Cannot delete Arcade house, maxTile is null");
            return null;
        }

        for (int x = minTile.x(); x <= maxTile.x(); ++x) {
            for (int y = minTile.y(); y <= maxTile.y(); ++y) {
                worldMap.terrainLayer().setContent(y, x, TerrainTile.EMPTY.$);
            }
        }

        var terrainProperties = worldMap.terrainLayer().propertyMap();
        terrainProperties.remove(WorldMapPropertyName.POS_HOUSE_MIN_TILE);
        terrainProperties.remove(WorldMapPropertyName.POS_HOUSE_MAX_TILE);
        terrainProperties.remove(WorldMapPropertyName.POS_GHOST_1_RED);
        terrainProperties.remove(WorldMapPropertyName.POS_GHOST_2_PINK);
        terrainProperties.remove(WorldMapPropertyName.POS_GHOST_3_CYAN);
        terrainProperties.remove(WorldMapPropertyName.POS_GHOST_4_ORANGE);

        editor.setWorldMapChanged();
        editor.setEdited(true);

        return null;
    }
}