/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapRenderer;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Locale;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BEFORE_MAZE;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BELOW_MAZE;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileValue(byte tileValue) {
        if (tileValue == TerrainTile.ARC_NE.$)  return TerrainTile.ARC_NW.$;
        if (tileValue == TerrainTile.ARC_NW.$)  return TerrainTile.ARC_NE.$;
        if (tileValue == TerrainTile.ARC_SE.$)  return TerrainTile.ARC_SW.$;
        if (tileValue == TerrainTile.ARC_SW.$)  return TerrainTile.ARC_SE.$;
        if (tileValue == TerrainTile.DARC_NE.$) return TerrainTile.DARC_NW.$;
        if (tileValue == TerrainTile.DARC_NW.$) return TerrainTile.DARC_NE.$;
        if (tileValue == TerrainTile.DARC_SE.$) return TerrainTile.DARC_SW.$;
        if (tileValue == TerrainTile.DARC_SW.$) return TerrainTile.DARC_SE.$;
        return tileValue;
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

    static Palette createTerrainPalette(byte id, int toolSize, TileMapEditor editor, TerrainMapRenderer terrainMapRenderer) {
        var palette = new Palette(id, toolSize, 1, 13, terrainMapRenderer);
        palette.addTileTool(editor, TerrainTile.EMPTY.$, "Empty Space");
        palette.addTileTool(editor, TerrainTile.WALL_H.$, "Horiz. Wall");
        palette.addTileTool(editor, TerrainTile.WALL_V.$, "Vert. Wall");
        palette.addTileTool(editor, TerrainTile.ARC_NW.$, "NW Corner");
        palette.addTileTool(editor, TerrainTile.ARC_NE.$, "NE Corner");
        palette.addTileTool(editor, TerrainTile.ARC_SW.$, "SW Corner");
        palette.addTileTool(editor, TerrainTile.ARC_SE.$, "SE Corner");
        palette.addTileTool(editor, TerrainTile.TUNNEL.$, "Tunnel");
        palette.addTileTool(editor, TerrainTile.DOOR.$, "Door");
        palette.addTileTool(editor, TerrainTile.ONE_WAY_UP.$, "One-Way Up");
        palette.addTileTool(editor, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right");
        palette.addTileTool(editor, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down");
        palette.addTileTool(editor, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    static Palette createActorPalette(byte id, int toolSize, TileMapEditor editor, TerrainTileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 11, renderer);
        palette.addTileTool(editor, TerrainTile.EMPTY.$, "Nope");
        palette.addPropertyTool(WorldMapProperty.POS_PAC, "Pac-Man");
        palette.addPropertyTool(WorldMapProperty.POS_RED_GHOST, "Red Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_PINK_GHOST, "Pink Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_CYAN_GHOST, "Cyan Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_ORANGE_GHOST, "Orange Ghost");
        palette.addPropertyTool(WorldMapProperty.POS_BONUS, "Bonus");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_RED_GHOST, "Red Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter");
        palette.addPropertyTool(WorldMapProperty.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter");
        palette.selectTool(0); // "No actor"
        return palette;
    }

    static Palette createFoodPalette(byte id, int toolSize, TileMapEditor editor, FoodMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 3, renderer);
        palette.addTileTool(editor, FoodTile.EMPTY.code(), "No Food");
        palette.addTileTool(editor, FoodTile.PELLET.code(), "Pellet");
        palette.addTileTool(editor, FoodTile.ENERGIZER.code(), "Energizer");
        palette.selectTool(0); // "No Food"
        return palette;
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    static int fullTiles(double pixels, double gridSize) {
        return (int) (pixels / gridSize);
    }

    static Vector2i tileAtMousePosition(double mouseX, double mouseY, double gridSize) {
        return new Vector2i(fullTiles(mouseX, gridSize), fullTiles(mouseY, gridSize));
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
}
