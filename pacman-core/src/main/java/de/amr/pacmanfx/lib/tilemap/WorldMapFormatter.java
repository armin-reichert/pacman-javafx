/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.Vector2i;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public interface WorldMapFormatter {

    String TILE_FORMAT = "(%d,%d)";

    static String formatTile(Vector2i tile) {
        requireNonNull(tile);
        return String.format(TILE_FORMAT, tile.x(), tile.y());
    }

    static String formatTile(int tileX, int tileY) {
        return String.format(TILE_FORMAT, tileX, tileY);
    }

    static String formatted(WorldMap worldMap) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        pw.println(WorldMap.MARKER_BEGIN_TERRAIN_LAYER);
        printLayer(pw, worldMap, worldMap.terrainLayer);
        pw.println(WorldMap.MARKER_BEGIN_FOOD_LAYER);
        printLayer(pw, worldMap, worldMap.foodLayer);
        return sw.toString();
    }

    private static void printLayer(PrintWriter pw, WorldMap worldMap, WorldMapLayer layer) {
        Map<String, String> properties = layer.properties();
        properties.keySet().stream().sorted().map(name -> "%s=%s".formatted(name, properties.get(name))).forEach(pw::println);
        pw.println(WorldMap.MARKER_BEGIN_DATA_SECTION);
        for (int row = 0; row < worldMap.numRows(); ++row) {
            for (int col = 0; col < worldMap.numCols(); ++col) {
                byte value = layer.get(row, col);
                pw.printf("#%02X", value);
                if (col < worldMap.numCols() - 1) {
                    pw.print(",");
                }
            }
            pw.println();
        }
        pw.flush();
    }
}
