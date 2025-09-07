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

import static de.amr.pacmanfx.lib.tilemap.WorldMapParser.parseTile;
import static de.amr.pacmanfx.model.WorldMapProperty.POS_HOUSE_MAX_TILE;
import static de.amr.pacmanfx.model.WorldMapProperty.POS_HOUSE_MIN_TILE;
import static java.util.Objects.requireNonNull;

public class Action_ClearFoodAroundHouse extends AbstractEditorAction<Void> {

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
                        worldMap.setContent(LayerID.FOOD, row, col, TerrainTile.EMPTY.code());
                        editor.setFoodMapChanged();
                        editor.setEdited(true);
                    }
                }
            }
        }
        return null;
    }
}
