/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class WorldMapParser {

    public enum ParsingState { START, TERRAIN_LAYER, FOOD_LAYER}

    public static Vector2i parseTile(String s) {
        requireNonNull(s);
        final Matcher m = WorldMap.TILE_PATTERN.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot create tile from '%s'".formatted(s));
        }
        try {
            final int x = Integer.parseInt(m.group(1));
            final int y = Integer.parseInt(m.group(2));
            return new Vector2i(x, y);
        } catch (NumberFormatException x) {
            Logger.error(x, "Could not parse tile from text '{}'", s);
            throw new IllegalArgumentException("Cannot create tile from '%s'".formatted(s));
        }
    }

    public static Optional<WorldMap> parse(
        Stream<String> linesStream,
        Predicate<Byte> validTerrainValueTest,
        Predicate<Byte> validFoodValueTest) {

        final WorldMapParser parser = new WorldMapParser();
        try {
            final WorldMap worldMap = parser.parse(new ArrayList<>(linesStream.toList()), validTerrainValueTest, validFoodValueTest);
            return Optional.of(worldMap);
        } catch (WorldMapParseException x) {
            Logger.error(x, "Could not parse world map");
            return Optional.empty();
        }
    }

    private static boolean isTerrainSectionStart(String line) {
        return line.startsWith(WorldMap.MARKER_BEGIN_TERRAIN_LAYER);
    }

    private static boolean isFoodSectionStart(String line) {
        return line.startsWith(WorldMap.MARKER_BEGIN_FOOD_LAYER);
    }

    private static boolean isDataSectionStart(String line) {
        return line.startsWith(WorldMap.MARKER_BEGIN_DATA_SECTION);
    }

    private final List<String> terrainLayerSection = new ArrayList<>();
    private final List<String> foodLayerSection = new ArrayList<>();
    private ParsingState state;

    public WorldMapParser() {
        state = ParsingState.START;
    }

    private WorldMap parse(List<String> lines, Predicate<Byte> validTerrainValueTest, Predicate<Byte> validFoodValueTest)
        throws WorldMapParseException
    {
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

        for (String line : lines) {
            switch (state) {
                case START -> {
                    if (isTerrainSectionStart(line)) {
                        state = ParsingState.TERRAIN_LAYER;
                    } else {
                        Logger.warn("Unexpected section start line: {}", line);
                    }
                }
                case TERRAIN_LAYER -> {
                    if (isFoodSectionStart(line)) {
                        state = ParsingState.FOOD_LAYER;
                    } else if (isTerrainSectionStart(line)) {
                        Logger.warn("Unexpected terrain section start line: {}", line);
                    } else {
                        terrainLayerSection.add(line);
                    }
                }
                case FOOD_LAYER -> {
                    if (isTerrainSectionStart(line)) {
                        Logger.warn("Unexpected terrain section start line: {}", line);
                    } else if (isFoodSectionStart(line)) {
                        Logger.warn("Unexpected food section start line: {}", line);
                    } else {
                        foodLayerSection.add(line);
                    }
                }
            }
        }
        worldMap.terrainLayer = new TerrainLayer(
            parseLayer(TerrainLayer::new, terrainLayerSection, validTerrainValueTest));

        worldMap.foodLayer = new FoodLayer(
            parseLayer(FoodLayer::new, foodLayerSection, validFoodValueTest));

        if (worldMap.terrainLayer.numRows() != worldMap.foodLayer.numRows()) {
            throw new WorldMapParseException("Terrain layer has %d rows but food layer has %d rows"
                .formatted(worldMap.terrainLayer.numRows(), worldMap.foodLayer.numRows()), null);
        }
        if (worldMap.terrainLayer.numCols() != worldMap.foodLayer.numCols()) {
            throw new WorldMapParseException("Terrain layer has %d cols but food layer has %d cols"
                .formatted(worldMap.terrainLayer.numCols(), worldMap.foodLayer.numCols()), null);
        }

        worldMap.numRows = worldMap.terrainLayer.numRows();
        worldMap.numCols = worldMap.terrainLayer.numCols();

        return worldMap;
    }

    @SuppressWarnings("unchecked")
    private <T extends WorldMapLayer> T parseLayer(BiFunction<Integer, Integer, WorldMapLayer> layerFactory, List<String> lines, Predicate<Byte> valueValidator)
        throws WorldMapParseException {

        // First pass: read property section and determine data section size
        int numDataRows = 0, numDataCols = -1;
        int dataSectionStartIndex = -1;
        final StringBuilder propertySection = new StringBuilder();
        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            final String line = lines.get(lineIndex);
            if (isDataSectionStart(line)) {
                dataSectionStartIndex = lineIndex + 1;
            } else if (dataSectionStartIndex == -1) {
                propertySection.append(line).append("\n");
            } else {
                numDataRows++;
                final String[] entries = line.split(",");
                if (numDataCols == -1) {
                    numDataCols = entries.length;
                } else if (entries.length != numDataCols) {
                    final String msg = "Inconsistent tile map data: found %d column(s), expected %d".formatted(entries.length, numDataCols);
                    throw new WorldMapParseException(msg, null, lineIndex, line);
                }
            }
        }
        if (numDataRows == 0) {
            final String msg = ("Inconsistent tile map data: No data section found");
            throw new WorldMapParseException(msg, null, 0, "");
        }

        // Second pass: read data and build new tile map
        final WorldMapLayer mapLayer = layerFactory.apply(numDataRows, numDataCols);

        mapLayer.propertyMap().putAll(parseProperties(propertySection.toString()));

        for (int lineIndex = dataSectionStartIndex; lineIndex < lines.size(); ++lineIndex) {
            final String line = lines.get(lineIndex);
            final int row = lineIndex - dataSectionStartIndex;
            final String[] entries = line.split(",");
            for (int col = 0; col < entries.length; ++col) {
                final String entry = entries[col].trim();
                try {
                    final byte value = Byte.decode(entry);
                    if (valueValidator.test(value)) {
                        mapLayer.setContent(row, col, value);
                    } else {
                        mapLayer.setContent(row, col, (byte) 0);
                        Logger.error("Invalid tile map value {} at row {}, col {}", value, row, col);
                    }
                } catch (NumberFormatException x) {
                    Logger.error("Invalid tile map entry {} at row {}, col {}", entry, row, col);
                }
            }
        }

        if (mapLayer instanceof TerrainLayer terrain) {
            terrain.createObstacles();
        }
        return (T) mapLayer;
    }

    private Map<String, String> parseProperties(String text) {
        final var properties = new HashMap<String, String>();
        final String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith(WorldMap.MARKER_COMMENT)) continue;
            final String[] sides = line.split("=");
            if (sides.length != 2) {
                Logger.error("Invalid line inside property section: {}", line);
            } else {
                final String lhs = sides[0].trim(), rhs = sides[1].trim();
                properties.put(lhs, rhs);
            }
        }
        return properties;
    }
}
