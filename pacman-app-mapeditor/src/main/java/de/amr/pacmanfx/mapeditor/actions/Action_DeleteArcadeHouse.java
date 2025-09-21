/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import org.tinylog.Logger;

import static de.amr.pacmanfx.model.DefaultWorldMapProperties.*;

public class Action_DeleteArcadeHouse extends EditorAction<Void> {

    public Action_DeleteArcadeHouse(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();

        Vector2i minTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        if (minTile == null) {
            Logger.error("Cannot delete Arcade house, minTile is null");
            return null;
        }

        Vector2i maxTile = worldMap.getTerrainTileProperty(POS_HOUSE_MAX_TILE);
        if (maxTile == null) {
            Logger.error("Cannot delete Arcade house, maxTile is null");
            return null;
        }

        for (int x = minTile.x(); x <= maxTile.x(); ++x) {
            for (int y = minTile.y(); y <= maxTile.y(); ++y) {
                worldMap.layer(LayerID.TERRAIN).set(y, x, TerrainTile.EMPTY.$);
            }
        }

        var terrainProperties = worldMap.properties(LayerID.TERRAIN);
        terrainProperties.remove(POS_HOUSE_MIN_TILE);
        terrainProperties.remove(POS_HOUSE_MAX_TILE);
        terrainProperties.remove(POS_GHOST_1_RED);
        terrainProperties.remove(POS_GHOST_2_PINK);
        terrainProperties.remove(POS_GHOST_3_CYAN);
        terrainProperties.remove(POS_GHOST_4_ORANGE);

        editor.setWorldMapChanged();
        editor.setEdited(true);

        return null;
    }
}