package experiments;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTileSet;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.model.WorldMapProperty;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

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
                WorldMap map = WorldMap.fromFile(mapFile);
                updateMapData(map);
                if (saveWorldMap(map, mapFile)) {
                    Logger.info("Updated map file {}", mapFile);
                }
            } catch (IOException x) {
                Logger.error(x);
            }
        }
    }

    private static boolean saveWorldMap(WorldMap worldMap,File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(WorldMapFormatter.formatted(worldMap));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

    private static void updateMapData(WorldMap map) {
        map.tiles().forEach(tile -> {
            byte content = map.content(LayerID.TERRAIN, tile);
            byte newContent = switch (content) {
                case TerrainTileSet.OBSOLETE_DWALL_H -> TerrainTileSet.WALL_H;
                case TerrainTileSet.OBSOLETE_DWALL_V -> TerrainTileSet.WALL_V;
                case TerrainTileSet.OBSOLETE_DCORNER_NW -> TerrainTileSet.ARC_NW;
                case TerrainTileSet.OBSOLETE_DCORNER_SW -> TerrainTileSet.ARC_SW;
                case TerrainTileSet.OBSOLETE_DCORNER_SE -> TerrainTileSet.ARC_SE;
                case TerrainTileSet.OBSOLETE_DCORNER_NE -> TerrainTileSet.ARC_NE;
                default -> content;
            };
            map.setContent(LayerID.TERRAIN, tile, newContent);
        });
        Vector2i houseMinTile = map.getTerrainTileProperty("pos_house_min_tile", new Vector2i(10, 15));
        Vector2i houseMaxTile = houseMinTile.plus(7, 4);
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MIN_TILE, WorldMapFormatter.formatTile(houseMinTile));
        map.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_HOUSE_MAX_TILE, WorldMapFormatter.formatTile(houseMaxTile));
        for (int row = houseMinTile.y(); row <= houseMaxTile.y(); ++row) {
            for (int col = houseMinTile.x(); col <= houseMaxTile.x(); ++col) {
                switch (map.content(LayerID.TERRAIN, row, col)) {
                    case TerrainTileSet.DCORNER_NW -> map.setContent(LayerID.TERRAIN, row, col, TerrainTileSet.ARC_NW);
                    case TerrainTileSet.DCORNER_SW -> map.setContent(LayerID.TERRAIN, row, col, TerrainTileSet.ARC_SW);
                    case TerrainTileSet.DCORNER_SE -> map.setContent(LayerID.TERRAIN, row, col, TerrainTileSet.ARC_SE);
                    case TerrainTileSet.DCORNER_NE -> map.setContent(LayerID.TERRAIN, row, col, TerrainTileSet.ARC_NE);
                }
            }
        }
    }
}
