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
import de.amr.pacmanfx.uilib.tilemap.TileMapRenderer;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Locale;

import static de.amr.pacmanfx.lib.tilemap.FoodTile.ENERGIZER;
import static de.amr.pacmanfx.lib.tilemap.FoodTile.PELLET;
import static de.amr.pacmanfx.lib.tilemap.TerrainTile.*;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileValue(byte tileValue) {
        if (tileValue == TerrainTile.byteValue(ARC_NE)) return TerrainTile.byteValue(ARC_NW);
        if (tileValue == TerrainTile.byteValue(ARC_NW)) return TerrainTile.byteValue(ARC_NE);
        if (tileValue == TerrainTile.byteValue(ARC_SE)) return TerrainTile.byteValue(ARC_SW);
        if (tileValue == TerrainTile.byteValue(ARC_SW)) return TerrainTile.byteValue(ARC_SE);
        if (tileValue == TerrainTile.byteValue(DCORNER_NE)) return TerrainTile.byteValue(DCORNER_NW);
        if (tileValue == TerrainTile.byteValue(DCORNER_NW)) return TerrainTile.byteValue(DCORNER_NE);
        if (tileValue == TerrainTile.byteValue(DCORNER_SE)) return TerrainTile.byteValue(DCORNER_SW);
        if (tileValue == TerrainTile.byteValue(DCORNER_SW)) return TerrainTile.byteValue(DCORNER_SE);
        return tileValue;
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

    static Palette createTerrainPalette(byte id, int toolSize, TileMapEditor editor, TileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 13, renderer);
        palette.addTileTool(editor, TerrainTile.byteValue(EMPTY), "Empty Space");
        palette.addTileTool(editor, TerrainTile.byteValue(WALL_H), "Horiz. Wall");
        palette.addTileTool(editor, TerrainTile.byteValue(WALL_V), "Vert. Wall");
        palette.addTileTool(editor, TerrainTile.byteValue(ARC_NW), "NW Corner");
        palette.addTileTool(editor, TerrainTile.byteValue(ARC_NE), "NE Corner");
        palette.addTileTool(editor, TerrainTile.byteValue(ARC_SW), "SW Corner");
        palette.addTileTool(editor, TerrainTile.byteValue(ARC_SE), "SE Corner");
        palette.addTileTool(editor, TerrainTile.byteValue(TUNNEL), "Tunnel");
        palette.addTileTool(editor, TerrainTile.byteValue(DOOR), "Door");
        palette.addTileTool(editor, TerrainTile.byteValue(ONE_WAY_UP), "One-Way Up");
        palette.addTileTool(editor, TerrainTile.byteValue(ONE_WAY_RIGHT), "One-Way Right");
        palette.addTileTool(editor, TerrainTile.byteValue(ONE_WAY_DOWN), "One-Way Down");
        palette.addTileTool(editor, TerrainTile.byteValue(ONE_WAY_LEFT), "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    static Palette createActorPalette(byte id, int toolSize, TileMapEditor editor, TerrainTileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 11, renderer);
        palette.addTileTool(editor, TerrainTile.emptyTileValue(), "Nope");
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

    static Palette createFoodPalette(byte id, int toolSize, TileMapEditor editor, TileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 3, renderer);
        palette.addTileTool(editor, FoodTile.emptyTileValue(), "No Food");
        palette.addTileTool(editor, FoodTile.byteValue(PELLET), "Pellet");
        palette.addTileTool(editor, FoodTile.byteValue(ENERGIZER), "Energizer");
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
}
