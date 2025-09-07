/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.model.WorldMapProperty.*;

public class Action_DeleteArcadeHouse extends AbstractEditorAction<Void> {

    public Action_DeleteArcadeHouse(TileMapEditor editor) {
        super(editor);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();
        var terrainProperties = worldMap.properties(LayerID.TERRAIN);
        Vector2i minTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(POS_HOUSE_MAX_TILE);
        for (int x = minTile.x(); x <= maxTile.x(); ++x) {
            for (int y = minTile.y(); y <= maxTile.y(); ++y) {
                worldMap.layer(LayerID.TERRAIN).set(y, x, TerrainTile.EMPTY.$);
            }
        }
        terrainProperties.remove(POS_HOUSE_MIN_TILE);
        terrainProperties.remove(POS_HOUSE_MAX_TILE);
        terrainProperties.remove(POS_RED_GHOST);
        terrainProperties.remove(POS_PINK_GHOST);
        terrainProperties.remove(POS_CYAN_GHOST);
        terrainProperties.remove(POS_ORANGE_GHOST);

        editor.setWorldMapChanged();
        editor.setEdited(true);

        return null;
    }
}
