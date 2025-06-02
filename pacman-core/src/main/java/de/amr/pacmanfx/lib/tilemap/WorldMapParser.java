/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface WorldMapParser {

    static WorldMap parse(Stream<String> linesStream) {
        final var lines = new ArrayList<>(linesStream.toList()); // modifiable list!
        final WorldMap worldMap = new WorldMap();

        // delete empty lines at end
        int i = lines.size() - 1;
        int count = 0;
        while (i >= 0 && lines.get(i).isBlank()) {
            lines.remove(i);
            ++count;
            --i;
        }
        if (count > 0) {
            Logger.info("{} empty line(s) at end of map file removed", count);
        }
        var terrainLayerRows = new ArrayList<String>();
        var foodLayerRows = new ArrayList<String>();
        boolean insideTerrainLayer = false, insideFoodLayer = false;
        for (String line : lines) {
            if (WorldMap.BEGIN_TERRAIN_LAYER.equals(line)) {
                insideTerrainLayer = true;
            } else if (WorldMap.BEGIN_FOOD_LAYER.equals(line)) {
                insideTerrainLayer = false;
                insideFoodLayer = true;
            } else if (insideTerrainLayer) {
                terrainLayerRows.add(line);
            } else if (insideFoodLayer) {
                foodLayerRows.add(line);
            } else {
                Logger.error("Line skipped: '{}'", line);
            }
        }
        worldMap.terrainLayer = parseTileMap(terrainLayerRows,
                value -> 0 <= value && value <= TerrainTiles.MAX_VALUE, TerrainTiles.EMPTY);

        worldMap.foodLayer = parseTileMap(foodLayerRows,
                value -> 0 <= value && value <= FoodTiles.ENERGIZER, FoodTiles.EMPTY);

        // Replace obsolete terrain tile values
        worldMap.tiles().forEach(tile -> {
            byte content = worldMap.content(LayerID.TERRAIN, tile);
            byte newContent = switch (content) {
                case TerrainTiles.OBSOLETE_DWALL_H -> TerrainTiles.WALL_H;
                case TerrainTiles.OBSOLETE_DWALL_V -> TerrainTiles.WALL_V;
                case TerrainTiles.OBSOLETE_DCORNER_NW -> TerrainTiles.ARC_NW;
                case TerrainTiles.OBSOLETE_DCORNER_SW -> TerrainTiles.ARC_SW;
                case TerrainTiles.OBSOLETE_DCORNER_SE -> TerrainTiles.ARC_SE;
                case TerrainTiles.OBSOLETE_DCORNER_NE -> TerrainTiles.ARC_NE;
                default -> content;
            };
            worldMap.setContent(LayerID.TERRAIN, tile, newContent);
        });
        return worldMap;
    }

    private static WorldMapLayer parseTileMap(List<String> lines, Predicate<Byte> valueAllowed, byte emptyValue) {
        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataStartIndex = -1;
        StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            if (WorldMap.BEGIN_DATA_SECTION.equals(line)) {
                dataStartIndex = lineIndex + 1;
            }
            else if (dataStartIndex == -1) {
                propertySection.append(line).append("\n");
            } else {
                numDataRows++;
                String[] columns = line.split(",");
                if (numDataCols == -1) {
                    numDataCols = columns.length;
                } else if (columns.length != numDataCols) {
                    Logger.error("Inconsistent tile map data: found {} column(s) in line {}, expected {}",
                            columns.length, lineIndex, numDataCols);
                }
            }
        }
        if (numDataRows == 0) {
            Logger.error("Inconsistent tile map data: No data");
        }

        // Second pass: read data and build new tile map
        var tileMap = new WorldMapLayer(numDataRows, numDataCols);
        tileMap.properties().putAll(parseProperties(propertySection.toString()));

        for (int lineIndex = dataStartIndex; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            int row = lineIndex -dataStartIndex;
            String[] columns = line.split(",");
            for (int col = 0; col < columns.length; ++col) {
                String entry = columns[col].trim();
                try {
                    byte value = Byte.decode(entry);
                    if (valueAllowed.test(value)) {
                        tileMap.set(row, col, value);
                    } else {
                        tileMap.set(row, col, emptyValue);
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }
        return tileMap;
    }

    private static Map<String, String> parseProperties(String text) {
        var properties = new HashMap<String, String>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("#")) continue;
            String[] sides = line.split("=");
            if (sides.length != 2) {
                Logger.error("Invalid line inside property section: {}", line);
            } else {
                String lhs = sides[0].trim(), rhs = sides[1].trim();
                properties.put(lhs, rhs);
            }
        }
        return properties;
    }
}
