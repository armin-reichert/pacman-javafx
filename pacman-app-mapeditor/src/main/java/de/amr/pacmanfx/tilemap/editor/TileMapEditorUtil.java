/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTileSet;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTileSet;
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

import static de.amr.pacmanfx.lib.tilemap.FoodTileSet.TileID.ENERGIZER;
import static de.amr.pacmanfx.lib.tilemap.FoodTileSet.TileID.PELLET;
import static de.amr.pacmanfx.lib.tilemap.TerrainTileSet.TileID.*;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileValue(byte tileValue) {
        if (tileValue == TerrainTileSet.valueOf(ARC_NE)) return TerrainTileSet.valueOf(ARC_NW);
        if (tileValue == TerrainTileSet.valueOf(ARC_NW)) return TerrainTileSet.valueOf(ARC_NE);
        if (tileValue == TerrainTileSet.valueOf(ARC_SE)) return TerrainTileSet.valueOf(ARC_SW);
        if (tileValue == TerrainTileSet.valueOf(ARC_SW)) return TerrainTileSet.valueOf(ARC_SE);
        if (tileValue == TerrainTileSet.valueOf(DCORNER_NE)) return TerrainTileSet.valueOf(DCORNER_NW);
        if (tileValue == TerrainTileSet.valueOf(DCORNER_NW)) return TerrainTileSet.valueOf(DCORNER_NE);
        if (tileValue == TerrainTileSet.valueOf(DCORNER_SE)) return TerrainTileSet.valueOf(DCORNER_SW);
        if (tileValue == TerrainTileSet.valueOf(DCORNER_SW)) return TerrainTileSet.valueOf(DCORNER_SE);
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
        palette.addTileTool(editor, TerrainTileSet.valueOf(EMPTY), "Empty Space");
        palette.addTileTool(editor, TerrainTileSet.valueOf(WALL_H), "Horiz. Wall");
        palette.addTileTool(editor, TerrainTileSet.valueOf(WALL_V), "Vert. Wall");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ARC_NW), "NW Corner");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ARC_NE), "NE Corner");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ARC_SW), "SW Corner");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ARC_SE), "SE Corner");
        palette.addTileTool(editor, TerrainTileSet.valueOf(TUNNEL), "Tunnel");
        palette.addTileTool(editor, TerrainTileSet.valueOf(DOOR), "Door");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ONE_WAY_UP), "One-Way Up");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ONE_WAY_RIGHT), "One-Way Right");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ONE_WAY_DOWN), "One-Way Down");
        palette.addTileTool(editor, TerrainTileSet.valueOf(ONE_WAY_LEFT), "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    static Palette createActorPalette(byte id, int toolSize, TileMapEditor editor, TerrainTileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 11, renderer);
        palette.addTileTool(editor, TerrainTileSet.emptyTileValue(), "Nope");
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
        palette.addTileTool(editor, FoodTileSet.emptyTileValue(), "No Food");
        palette.addTileTool(editor, FoodTileSet.valueOf(PELLET), "Pellet");
        palette.addTileTool(editor, FoodTileSet.valueOf(ENERGIZER), "Energizer");
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
