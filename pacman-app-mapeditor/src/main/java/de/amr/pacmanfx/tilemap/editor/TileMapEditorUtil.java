/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BEFORE_MAZE;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BELOW_MAZE;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileCode(byte code) {
        if (code == TerrainTile.ARC_NE.$)  return TerrainTile.ARC_NW.$;
        if (code == TerrainTile.ARC_NW.$)  return TerrainTile.ARC_NE.$;
        if (code == TerrainTile.ARC_SE.$)  return TerrainTile.ARC_SW.$;
        if (code == TerrainTile.ARC_SW.$)  return TerrainTile.ARC_SE.$;
        if (code == TerrainTile.DARC_NE.$) return TerrainTile.DARC_NW.$;
        if (code == TerrainTile.DARC_NW.$) return TerrainTile.DARC_NE.$;
        if (code == TerrainTile.DARC_SE.$) return TerrainTile.DARC_SW.$;
        if (code == TerrainTile.DARC_SW.$) return TerrainTile.DARC_SE.$;
        return code;
    }

    static Vector2i parseSize(String cols_x_rows) {
        String[] tuple = cols_x_rows.split("x");
        if (tuple.length != 2) {
            return null;
        }
        try {
            int numCols = Integer.parseInt(tuple[0].trim());
            int numRows = Integer.parseInt(tuple[1].trim());
            return new Vector2i(numCols, numRows);
        } catch (Exception x) {
            return null;
        }
    }

    static Color parseColor(String text) {
        try {
            return Color.web(text);
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
        if (worldMap.properties(layerID).containsKey(key)) {
            String colorSpec = worldMap.properties(layerID).get(key);
            try {
                return Color.web(colorSpec);
            } catch (Exception x) {
                Logger.error("Could not create color from value '{}'", colorSpec);
                return defaultColor;
            }
        }
        return defaultColor;
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

    static boolean canEditFoodAtTile(WorldMap worldMap, Vector2i tile) {
        return !worldMap.outOfWorld(tile)
                && tile.y() >= EMPTY_ROWS_BEFORE_MAZE
                && tile.y() < worldMap.numRows() - EMPTY_ROWS_BELOW_MAZE
                && !isPartOfHouse(worldMap, tile)
                && hasAccessibleTerrainAtTile(worldMap, tile);
    }

    static boolean isPartOfHouse(WorldMap worldMap, Vector2i tile) {
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE);
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
            pw.print(WorldMapFormatter.formatted(worldMap));
            return true;
        } catch (IOException x) {
            Logger.error(x);
            return false;
        }
    }
}
