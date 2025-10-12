/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.ArcadeHouse;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

public class Action_PlaceArcadeHouse extends EditorAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i minTile;

    public Action_PlaceArcadeHouse(TileMapEditor editor, Vector2i minTile) {
        this(editor, editor.currentWorldMap(), minTile);
    }

    public Action_PlaceArcadeHouse(TileMapEditor editor, WorldMap worldMap, Vector2i minTile) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.minTile = requireNonNull(minTile);
    }

    @Override
    public Void execute() {
        final Vector2i houseSize = ArcadeHouse.SIZE_IN_TILES;
        final TerrainLayer terrain = worldMap.terrainLayer();
        final Map<String, String> terrainProperties = terrain.propertyMap();
        final Vector2i maxTile = minTile.plus(houseSize).minus(1, 1);
        if (terrain.outOfBounds(minTile) || terrain.outOfBounds(maxTile)) {
            Logger.error("Illegal house position min: {} max: {}", minTile, maxTile);
            return null;
        }

        terrainProperties.put(POS_HOUSE_MIN_TILE, String.valueOf(minTile));
        terrainProperties.put(POS_HOUSE_MAX_TILE, String.valueOf(maxTile));

        // clear new house area
        clearArea(minTile, maxTile);

        // place house tile content
        for (int y = 0; y < houseSize.y(); ++y) {
            for (int x = 0; x < houseSize.x(); ++x) {
                terrain.setContent(minTile.y() + y, minTile.x() + x, ArcadeHouse.CONTENT[y][x]);
            }
        }
        editor.setTerrainMapChanged();

        // place ghosts
        terrainProperties.put(POS_GHOST_1_RED,    String.valueOf(minTile.plus(3, -1)));
        terrainProperties.put(POS_GHOST_3_CYAN,   String.valueOf(minTile.plus(1, 2)));
        terrainProperties.put(POS_GHOST_2_PINK,   String.valueOf(minTile.plus(3, 2)));
        terrainProperties.put(POS_GHOST_4_ORANGE, String.valueOf(minTile.plus(5, 2)));
        editor.setTerrainMapPropertyChanged();

        return null;
    }

    private void clearArea(Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.terrainLayer().setContent(row, col, TerrainTile.EMPTY.$);
                worldMap.foodLayer().setContent(row, col, FoodTile.EMPTY.$);
            }
        }
    }
}