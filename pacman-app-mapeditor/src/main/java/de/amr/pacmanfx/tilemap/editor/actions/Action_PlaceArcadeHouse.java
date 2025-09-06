package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static java.util.Objects.requireNonNull;

public class Action_PlaceArcadeHouse extends AbstractEditorAction<Void> {

    public static final byte[][] DEFAULT_HOUSE_ROWS = {
        { ARC_NW.$,  WALL_H.$,  WALL_H.$,  DOOR.$,    DOOR.$,    WALL_H.$,  WALL_H.$,  ARC_NE.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { WALL_V.$,  EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   EMPTY.$,   WALL_V.$ },
        { ARC_SW.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  WALL_H.$,  ARC_SE.$ },
    };

    private final Vector2i houseMinTile;

    public Action_PlaceArcadeHouse(TileMapEditor editor, Vector2i houseMinTile) {
        super(editor);
        this.houseMinTile = requireNonNull(houseMinTile);
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);

        if (worldMap.outOfWorld(houseMinTile) || worldMap.outOfWorld(houseMaxTile)) {
            Logger.error("Illegal house position min: {} max: {}", houseMinTile, houseMaxTile);
            return null;
        }

        Vector2i oldHouseMinTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i oldHouseMaxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MIN_TILE, formatTile(houseMinTile));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MAX_TILE, formatTile(houseMaxTile));

        // clear tiles where house walls/doors were located (created at runtime!)
        if (oldHouseMinTile != null && oldHouseMaxTile != null) {
            clearTerrainAreaOneSided(editor, worldMap, oldHouseMinTile, oldHouseMaxTile);
            clearFoodAreaOneSided(editor, worldMap, oldHouseMinTile, oldHouseMaxTile);
        }
        // clear new house area
        clearTerrainAreaOneSided(editor, worldMap, houseMinTile, houseMaxTile);
        clearFoodAreaOneSided(editor, worldMap, houseMinTile, houseMaxTile);

        // place house tile content
        Vector2i houseSize = houseMaxTile.minus(houseMinTile).plus(1,1);
        for (int y = 0; y < houseSize.y(); ++y) {
            for (int x = 0; x < houseSize.x(); ++x) {
                worldMap.setContent(LayerID.TERRAIN, houseMinTile.y() + y, houseMinTile.x() + x, DEFAULT_HOUSE_ROWS[y][x]);
            }
        }

        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_RED_GHOST,      formatTile(houseMinTile.plus(3, -1)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_CYAN_GHOST,     formatTile(houseMinTile.plus(1, 2)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PINK_GHOST,     formatTile(houseMinTile.plus(3, 2)));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_ORANGE_GHOST,   formatTile(houseMinTile.plus(5, 2)));

        // clear pellets around house
        Vector2i minAround = houseMinTile.minus(1,1);
        Vector2i maxAround = houseMaxTile.plus(1,1);
        for (int x = minAround.x(); x <= maxAround.x(); ++x) {
            // Note: parameters are row and col (y and x)
            if (worldMap.outOfWorld(minAround.y(), x)) continue;
            if (worldMap.outOfWorld(maxAround.y(), x)) continue;
            if (x >= 0) {
                worldMap.setContent(LayerID.FOOD, minAround.y(), x, FoodTile.EMPTY.code());
                worldMap.setContent(LayerID.FOOD, maxAround.y(), x, FoodTile.EMPTY.code());
            }
        }
        for (int y = minAround.y(); y <= maxAround.y(); ++y) {
            if (worldMap.outOfWorld(y, minAround.x())) continue;
            if (worldMap.outOfWorld(y, maxAround.x())) continue;
            // Note: parameters are row and col (y and x)
            worldMap.setContent(LayerID.FOOD, y, minAround.x(), FoodTile.EMPTY.code());
            worldMap.setContent(LayerID.FOOD, y, maxAround.x(), FoodTile.EMPTY.code());
        }

        editor.setWorldMapChanged();
        editor.setEdited(true);
        return null;
    }

    private void clearTerrainAreaOneSided(TileMapEditor editor, WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.setContent(LayerID.TERRAIN, row, col, TerrainTile.EMPTY.$);
            }
        }
        editor.setTerrainMapChanged();
        editor.setEdited(true);
    }

    private void clearFoodAreaOneSided(TileMapEditor editor, WorldMap worldMap, Vector2i minTile, Vector2i maxTile) {
        for (int row = minTile.y(); row <= maxTile.y(); ++row) {
            for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                // No symmetric editing!
                worldMap.setContent(LayerID.FOOD, row, col, FoodTile.EMPTY.code());
            }
        }
        editor.setFoodMapChanged();
        editor.setEdited(true);
    }
}