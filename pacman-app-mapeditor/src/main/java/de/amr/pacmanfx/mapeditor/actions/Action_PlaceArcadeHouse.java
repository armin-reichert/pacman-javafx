/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.lib.worldmap.WorldMap;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.worldmap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static de.amr.pacmanfx.mapeditor.TileMapEditor.*;
import static java.util.Objects.requireNonNull;

public class Action_PlaceArcadeHouse extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i minTile;
    private final Vector2i maxTile;

    public Action_PlaceArcadeHouse(TileMapEditor editor, Vector2i minTile) {
        this(editor, editor.currentWorldMap(), minTile);
    }

    public Action_PlaceArcadeHouse(TileMapEditor editor, WorldMap worldMap, Vector2i minTile) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.minTile = requireNonNull(minTile);
        this.maxTile = minTile.plus(ARCADE_HOUSE_WIDTH - 1, ARCADE_HOUSE_HEIGHT - 1);
    }

    @Override
    public Void execute() {
        if (worldMap.outOfWorld(minTile) || worldMap.outOfWorld(maxTile)) {
            Logger.error("Illegal house position min: {} max: {}", minTile, maxTile);
            return null;
        }

        Vector2i oldMinTile = worldMap.getTerrainTileProperty(POS_HOUSE_MIN_TILE);
        Vector2i oldMaxTile = worldMap.getTerrainTileProperty(POS_HOUSE_MAX_TILE);

        worldMap.properties(LayerID.TERRAIN).put(POS_HOUSE_MIN_TILE, formatTile(minTile));
        worldMap.properties(LayerID.TERRAIN).put(POS_HOUSE_MAX_TILE, formatTile(maxTile));

        // clear tiles where house walls/doors were located (created at runtime!)
        if (oldMinTile != null && oldMaxTile != null) {
            clearHouseArea(worldMap, oldMinTile, oldMaxTile);
        }
        // clear new house area
        clearHouseArea(worldMap, minTile, maxTile);

        // place house tile content
        Vector2i houseSize = maxTile.minus(minTile).plus(1,1);
        for (int y = 0; y < houseSize.y(); ++y) {
            for (int x = 0; x < houseSize.x(); ++x) {
                worldMap.setContent(LayerID.TERRAIN, minTile.y() + y, minTile.x() + x, ARCADE_HOUSE_CODE[y][x]);
            }
        }

        // place ghosts
        worldMap.properties(LayerID.TERRAIN).put(POS_RED_GHOST,    formatTile(minTile.plus(3, -1)));
        worldMap.properties(LayerID.TERRAIN).put(POS_CYAN_GHOST,   formatTile(minTile.plus(1, 2)));
        worldMap.properties(LayerID.TERRAIN).put(POS_PINK_GHOST,   formatTile(minTile.plus(3, 2)));
        worldMap.properties(LayerID.TERRAIN).put(POS_ORANGE_GHOST, formatTile(minTile.plus(5, 2)));

        // clear pellets around house
        Vector2i minAround = minTile.minus(1,1);
        Vector2i maxAround = maxTile.plus(1,1);
        for (int x = minAround.x(); x <= maxAround.x(); ++x) {
            // Note: parameters are row and col (y and x)
            if (worldMap.outOfWorld(minAround.y(), x)) continue;
            if (worldMap.outOfWorld(maxAround.y(), x)) continue;
            worldMap.setContent(LayerID.FOOD, minAround.y(), x, FoodTile.EMPTY.$);
            worldMap.setContent(LayerID.FOOD, maxAround.y(), x, FoodTile.EMPTY.$);
        }
        for (int y = minAround.y(); y <= maxAround.y(); ++y) {
            if (worldMap.outOfWorld(y, minAround.x())) continue;
            if (worldMap.outOfWorld(y, maxAround.x())) continue;
            // Note: parameters are row and col (y and x)
            worldMap.setContent(LayerID.FOOD, y, minAround.x(), FoodTile.EMPTY.$);
            worldMap.setContent(LayerID.FOOD, y, maxAround.x(), FoodTile.EMPTY.$);
        }

        editor.setWorldMapChanged();
        editor.setEdited(true);
        return null;
    }

    private void clearHouseArea(WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.setContent(LayerID.TERRAIN, row, col, TerrainTile.EMPTY.$);
                worldMap.setContent(LayerID.FOOD,    row, col, FoodTile.EMPTY.$);
            }
        }
    }
}