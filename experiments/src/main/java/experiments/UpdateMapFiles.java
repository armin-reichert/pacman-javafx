package experiments;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTiles;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public class UpdateMapFiles {

    public static void main(String[] args) {
        if (args.length == 0) {
            Logger.info("Usage: UpdateMapFiles <directory>");
            return;
        }
        updateMapFiles(new File(args[0]));
    }

    public static void updateMapFiles(File directory) {
        File[] mapFiles = directory.listFiles((File dir, String name) -> name.endsWith(".world"));
        if (mapFiles == null) {
            Logger.info("No map files found in directory {}", directory);
            return;
        }
        for (File mapFile : mapFiles) {
            try {
                WorldMap map = new WorldMap(mapFile);
                updateMapData(map);
                map.save(mapFile);
                Logger.info("Updated map file {}", mapFile);
            } catch (IOException x) {
                Logger.error(x);
            }
        }
    }

    private static void updateMapData(WorldMap map) {
        map.tiles().forEach(tile -> {
            byte content = map.content(LayerID.TERRAIN, tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.ARC_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.ARC_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.ARC_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.ARC_NE;
                default -> content;
            };
            map.setContent(LayerID.TERRAIN, tile, newContent);
        });
        Vector2i houseMinTile = map.getTerrainTileProperty("pos_house_min_tile", new Vector2i(10, 15));
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MIN_TILE, WorldMap.formatTile(houseMinTile));
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MAX_TILE, WorldMap.formatTile(houseMaxTile));
        for (int row = houseMinTile.y(); row <= houseMaxTile.y(); ++row) {
            for (int col = houseMinTile.x(); col <= houseMaxTile.x(); ++col) {
                switch (map.content(LayerID.TERRAIN, row, col)) {
                    case TerrainTiles.DCORNER_NW -> map.setContent(LayerID.TERRAIN, row, col, TerrainTiles.ARC_NW);
                    case TerrainTiles.DCORNER_SW -> map.setContent(LayerID.TERRAIN, row, col, TerrainTiles.ARC_SW);
                    case TerrainTiles.DCORNER_SE -> map.setContent(LayerID.TERRAIN, row, col, TerrainTiles.ARC_SE);
                    case TerrainTiles.DCORNER_NE -> map.setContent(LayerID.TERRAIN, row, col, TerrainTiles.ARC_NE);
                }
            }
        }
    }
}
