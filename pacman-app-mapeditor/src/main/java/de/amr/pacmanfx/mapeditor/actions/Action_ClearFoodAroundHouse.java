/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.TerrainTile;
import de.amr.pacmanfx.model.world.WorldMap;

import static de.amr.pacmanfx.model.world.WorldMap.parseTile;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.POS_HOUSE_MAX_TILE;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.POS_HOUSE_MIN_TILE;
import static java.util.Objects.requireNonNull;

public class Action_ClearFoodAroundHouse extends EditorAction<Void> {

    private final WorldMap worldMap;

    public Action_ClearFoodAroundHouse(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        String minTileValue = worldMap.terrainLayer().propertyMap().get(POS_HOUSE_MIN_TILE);
        String maxTileValue = worldMap.terrainLayer().propertyMap().get(POS_HOUSE_MAX_TILE);
        if (minTileValue != null && maxTileValue != null) {
            Vector2i minTile = parseTile(minTileValue).orElse(null);
            Vector2i maxTile = parseTile(maxTileValue).orElse(null);
            if (minTile != null && maxTile != null) {
                for (int col = minTile.x() - 1; col <= maxTile.x() + 1; ++col) {
                    for (int row = minTile.y() - 1; row <= maxTile.y() + 1; ++row) {
                        if (worldMap.foodLayer().outOfBounds(row, col)) continue;
                        worldMap.foodLayer().setContent(row, col, TerrainTile.EMPTY.$);
                        editor.setFoodMapChanged();
                        editor.setEdited(true);
                    }
                }
            }
        }
        return null;
    }
}
