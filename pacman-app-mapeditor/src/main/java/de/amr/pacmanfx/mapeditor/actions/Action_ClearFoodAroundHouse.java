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

import static de.amr.pacmanfx.lib.worldmap.WorldMapParser.parseTile;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.POS_HOUSE_MAX_TILE;
import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.POS_HOUSE_MIN_TILE;
import static java.util.Objects.requireNonNull;

public class Action_ClearFoodAroundHouse extends EditorAction<Void> {

    private final WorldMap worldMap;

    public Action_ClearFoodAroundHouse(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        String minTileValue = worldMap.properties(LayerID.TERRAIN).get(POS_HOUSE_MIN_TILE);
        String maxTileValue = worldMap.properties(LayerID.TERRAIN).get(POS_HOUSE_MAX_TILE);
        if (minTileValue != null && maxTileValue != null) {
            Vector2i minTile = parseTile(minTileValue).orElse(null);
            Vector2i maxTile = parseTile(maxTileValue).orElse(null);
            if (minTile != null && maxTile != null) {
                for (int col = minTile.x() - 1; col <= maxTile.x() + 1; ++col) {
                    for (int row = minTile.y() - 1; row <= maxTile.y() + 1; ++row) {
                        if (worldMap.outOfWorld(row, col)) continue;
                        worldMap.setContent(LayerID.FOOD, row, col, TerrainTile.EMPTY.$);
                        editor.setFoodMapChanged();
                        editor.setEdited(true);
                    }
                }
            }
        }
        return null;
    }
}
