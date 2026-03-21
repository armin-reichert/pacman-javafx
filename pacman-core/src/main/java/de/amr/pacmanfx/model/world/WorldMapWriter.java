/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

public class WorldMapWriter {

    public static String createSourceCode(WorldMap worldMap, boolean lineNumbers) {
        requireNonNull(worldMap);

        final StringWriter sw = new StringWriter();
        final WorldMapWriter w = new WorldMapWriter(worldMap, sw);
        w.printWorldMap();

        // Maybe add line numbers
        final String source = sw.toString();
        if (lineNumbers) {
            final var sb = new StringBuilder();
            final String[] lines = source.split("\\R");
            for (int lineNum = 1; lineNum <= lines.length; ++lineNum) {
                sb.append("%5d: %s\n".formatted(lineNum, lines[lineNum-1]));
            }
            return sb.toString();
        }
        else {
            return source;
        }
    }

    private final WorldMap worldMap;
    private final PrintWriter pw;

    public WorldMapWriter(WorldMap worldMap, Writer w) {
        this.worldMap = requireNonNull(worldMap);
        this.pw = new PrintWriter(requireNonNull(w));
    }

    public void printWorldMap() {
        final TerrainLayer terrain = worldMap.terrainLayer();
        pw.println(WorldMap.MARKER_BEGIN_TERRAIN_LAYER);
        printLayerProperties(terrain);
        printLayerData(terrain);

        final FoodLayer foodLayer = worldMap.foodLayer();
        pw.println(WorldMap.MARKER_BEGIN_FOOD_LAYER);
        printComment(" Pellets (total): %d".formatted(foodLayer.totalFoodCount()));
        printComment(" Energizers: %d".formatted(foodLayer.energizerTiles().size()));
        printLayerProperties(foodLayer);
        printLayerData(foodLayer);
    }

    private void printLayerData(WorldMapLayer layer) {
        pw.println(WorldMap.MARKER_BEGIN_DATA_SECTION);
        for (int row = 0; row < layer.numRows(); ++row) {
            final StringJoiner joiner = new StringJoiner(",");
            for (int col = 0; col < layer.numCols(); ++col) {
                final byte content = layer.content(row, col);
                joiner.add("#%02X".formatted(content));
            }
            pw.println(joiner);
        }
    }

    private void printLayerProperties(WorldMapLayer layer) {
        layer.propertiesSortedByName()
            .map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
            .forEach(pw::println);
    }

    private void printComment(String comment) {
        pw.println(WorldMap.COMMENT_PREFIX + comment);
    }
}
