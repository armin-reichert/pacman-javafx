/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.TerrainTile;
import de.amr.pacmanfx.model.world.WorldMap;
import org.tinylog.Logger;

import static de.amr.pacmanfx.model.world.WorldMapParser.parseTile;
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
        final String minTileValue = worldMap.terrainLayer().propertyMap().get(POS_HOUSE_MIN_TILE);
        final String maxTileValue = worldMap.terrainLayer().propertyMap().get(POS_HOUSE_MAX_TILE);
        if (minTileValue != null && maxTileValue != null) {
            try {
                final Vector2i minTile = parseTile(minTileValue);
                final Vector2i maxTile = parseTile(maxTileValue);
                for (int col = minTile.x() - 1; col <= maxTile.x() + 1; ++col) {
                    for (int row = minTile.y() - 1; row <= maxTile.y() + 1; ++row) {
                        if (worldMap.foodLayer().outOfBounds(row, col)) continue;
                        worldMap.foodLayer().setContent(row, col, TerrainTile.EMPTY.$);
                        editor.setFoodMapChanged();
                        editor.setEdited(true);
                    }
                }
            } catch (IllegalArgumentException x) {
                Logger.error(x, "Action {} not executed", getClass().getSimpleName());
            }
        }
        return null;
    }
}
