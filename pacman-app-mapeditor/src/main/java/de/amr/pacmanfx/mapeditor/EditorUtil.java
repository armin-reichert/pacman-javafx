/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.*;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.DefaultWorldMapProperties;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface EditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(EditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static String generateSourceCode(WorldMap worldMap) {
        StringBuilder sb = new StringBuilder();
        String[] sourceTextLines = formatted(worldMap).split("\n");
        for (int i = 0; i < sourceTextLines.length; ++i) {
            sb.append("%5d: ".formatted(i + 1)).append(sourceTextLines[i]).append("\n");
        }
        return sb.toString();
    }

    static byte mirroredTileCode(byte code) {
        if (code == TerrainTile.ARC_NE.$)  return TerrainTile.ARC_NW.$;
        if (code == TerrainTile.ARC_NW.$)  return TerrainTile.ARC_NE.$;
        if (code == TerrainTile.ARC_SE.$)  return TerrainTile.ARC_SW.$;
        if (code == TerrainTile.ARC_SW.$)  return TerrainTile.ARC_SE.$;
        if (code == TerrainTile.ANG_ARC_NE.$) return TerrainTile.ANG_ARC_NW.$;
        if (code == TerrainTile.ANG_ARC_NW.$) return TerrainTile.ANG_ARC_NE.$;
        if (code == TerrainTile.ANG_ARC_SE.$) return TerrainTile.ANG_ARC_SW.$;
        if (code == TerrainTile.ANG_ARC_SW.$) return TerrainTile.ANG_ARC_SE.$;
        return code;
    }

    static Optional<Vector2i> parseMapSize(String cols_x_rows) {
        String[] tuple = cols_x_rows.split("x");
        if (tuple.length != 2) {
            return Optional.empty();
        }
        try {
            int numCols = Integer.parseInt(tuple[0].trim());
            int numRows = Integer.parseInt(tuple[1].trim());
            return Optional.of(new Vector2i(numCols, numRows));
        } catch (Exception x) {
            Logger.error("Could not parse '{}' as size value (cols x rows)", cols_x_rows);
            return Optional.empty();
        }
    }

    static Color parseColor(String text) {
        try {
            return Color.valueOf(text);
        } catch (Exception x) {
            Logger.error(x);
            return Color.WHITE;
        }
    }

    // Note: String.format is locale-dependent! This may produce illegal color format if locale is not ENGLISH!
    static String formatColor(Color color) {
        return String.format(Locale.ENGLISH, "rgba(%d,%d,%d,%.2f)",
                (int) (color.getRed()   * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue()  * 255),
                color.getOpacity());
    }

    static String formatColorHex(Color color) {
        return String.format("#%02x%02x%02x", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    static Color getColorFromMap(WorldMap worldMap, LayerID layerID, String key, Color defaultColor) {
        requireNonNull(worldMap);
        requireNonNull(layerID);
        requireNonNull(key);
        String colorExpression = worldMap.properties(layerID).get(key);
        if (colorExpression == null) {
            return defaultColor;
        }
        try {
            return Color.valueOf(colorExpression);
        } catch (Exception x) {
            Logger.error(x);
            Logger.error("Error parsing color expression '{}', using default color {}", colorExpression, defaultColor);
            return defaultColor;
        }
    }

    static Node filler(int pixels) {
        var filler = new HBox();
        filler.setMinWidth(pixels);
        filler.setMaxWidth(pixels);
        return filler;
    }

    static Node spacer() {
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    static int fullTiles(double pixels, double gridSize) {
        return (int) (pixels / gridSize);
    }

    static boolean hasAccessibleTerrainAtTile(WorldMap worldMap, Vector2i tile) {
        byte value = worldMap.content(LayerID.TERRAIN, tile);
        return value == TerrainTile.EMPTY.$
                || value == TerrainTile.ONE_WAY_DOWN.$
                || value == TerrainTile.ONE_WAY_UP.$
                || value == TerrainTile.ONE_WAY_LEFT.$
                || value == TerrainTile.ONE_WAY_RIGHT.$;
    }

    static boolean canPlaceFoodAtTile(WorldMap worldMap, Vector2i tile) {
        return !worldMap.outOfWorld(tile)
                && tile.y() >= GameLevel.EMPTY_ROWS_OVER_MAZE
                && tile.y() < worldMap.numRows() - GameLevel.EMPTY_ROWS_BELOW_MAZE
                && !isPartOfHouse(worldMap, tile)
                && hasAccessibleTerrainAtTile(worldMap, tile);
    }

    static boolean isPartOfHouse(WorldMap worldMap, Vector2i tile) {
        Vector2i minTile = worldMap.getTerrainTileProperty(DefaultWorldMapProperties.POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(DefaultWorldMapProperties.POS_HOUSE_MAX_TILE);
        if (minTile != null && maxTile != null) {
            return minTile.x() <= tile.x() && tile.x() <= maxTile.x()
                    && minTile.y() <= tile.y() && tile.y() <= maxTile.y();
        }
        return false;
    }

    static Optional<Image> loadImage(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            return Optional.of(new Image(stream));
        } catch (IOException x) {
            Logger.error(x);
            return Optional.empty();
        }
    }

    static boolean saveWorldMap(WorldMap worldMap, File file) {
        try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
            pw.print(formatted(worldMap));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }

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
        printLayer(pw, worldMap, worldMap.layer(LayerID.TERRAIN));
        pw.println(WorldMap.MARKER_BEGIN_FOOD_LAYER);
        printLayer(pw, worldMap, worldMap.layer(LayerID.FOOD));
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