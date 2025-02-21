package experiments;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
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
        TileMap terrain = map.terrain();
        terrain.tiles().forEach(tile -> {
            byte content = terrain.get(tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.CORNER_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.CORNER_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.CORNER_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.CORNER_NE;
                default -> content;
            };
            terrain.set(tile, newContent);
        });
        Vector2i houseMinTile = map.getTileProperty("pos_house_min_tile", new Vector2i(10, 15));
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);
        terrain.setProperty(WorldMap.PROPERTY_POS_HOUSE_MIN_TILE, WorldMap.formatTile(houseMinTile));
        terrain.setProperty(WorldMap.PROPERTY_POS_HOUSE_MAX_TILE, WorldMap.formatTile(houseMaxTile));
        for (int row = houseMinTile.y(); row <= houseMaxTile.y(); ++row) {
            for (int col = houseMinTile.x(); col <= houseMaxTile.x(); ++col) {
                switch (terrain.get(row, col)) {
                    case TerrainTiles.DCORNER_ANGULAR_NW -> terrain.set(row, col, TerrainTiles.CORNER_NW);
                    case TerrainTiles.DCORNER_ANGULAR_SW -> terrain.set(row, col, TerrainTiles.CORNER_SW);
                    case TerrainTiles.DCORNER_ANGULAR_SE -> terrain.set(row, col, TerrainTiles.CORNER_SE);
                    case TerrainTiles.DCORNER_ANGULAR_NE -> terrain.set(row, col, TerrainTiles.CORNER_NE);
                }
            }
        }
    }
}
