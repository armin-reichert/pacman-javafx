/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.TileMapRenderer;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Locale;

import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileValue(byte content) {
        return switch (content) {
            case TerrainTiles.CORNER_NE -> TerrainTiles.CORNER_NW;
            case TerrainTiles.CORNER_NW -> TerrainTiles.CORNER_NE;
            case TerrainTiles.CORNER_SE -> TerrainTiles.CORNER_SW;
            case TerrainTiles.CORNER_SW -> TerrainTiles.CORNER_SE;
            case TerrainTiles.DCORNER_ANGULAR_NE -> TerrainTiles.DCORNER_ANGULAR_NW;
            case TerrainTiles.DCORNER_ANGULAR_NW -> TerrainTiles.DCORNER_ANGULAR_NE;
            case TerrainTiles.DCORNER_ANGULAR_SE -> TerrainTiles.DCORNER_ANGULAR_SW;
            case TerrainTiles.DCORNER_ANGULAR_SW -> TerrainTiles.DCORNER_ANGULAR_SE;
            default -> content;
        };
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
        if (worldMap.hasProperty(layerID, key)) {
            String colorSpec = worldMap.getStringProperty(layerID, key);
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
        palette.addTileTool(editor, TerrainTiles.EMPTY, "Empty Space");
        palette.addTileTool(editor, TerrainTiles.WALL_H, "Horiz. Wall");
        palette.addTileTool(editor, TerrainTiles.WALL_V, "Vert. Wall");
        palette.addTileTool(editor, TerrainTiles.CORNER_NW, "NW Corner");
        palette.addTileTool(editor, TerrainTiles.CORNER_NE, "NE Corner");
        palette.addTileTool(editor, TerrainTiles.CORNER_SW, "SW Corner");
        palette.addTileTool(editor, TerrainTiles.CORNER_SE, "SE Corner");
        palette.addTileTool(editor, TerrainTiles.TUNNEL, "Tunnel");
        palette.addTileTool(editor, TerrainTiles.DOOR, "Door");
        palette.addTileTool(editor, TerrainTiles.ONE_WAY_UP, "One-Way Up");
        palette.addTileTool(editor, TerrainTiles.ONE_WAY_RIGHT, "One-Way Right");
        palette.addTileTool(editor, TerrainTiles.ONE_WAY_DOWN, "One-Way Down");
        palette.addTileTool(editor, TerrainTiles.ONE_WAY_LEFT, "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    static Palette createActorPalette(byte id, int toolSize, TileMapEditor editor, TerrainRendererInEditor renderer) {
        var palette = new Palette(id, toolSize, 1, 10, renderer);
        palette.addTileTool(editor, TerrainTiles.EMPTY, "");
        palette.addPropertyTool(PROPERTY_POS_PAC, "Pac-Man");
        palette.addPropertyTool(PROPERTY_POS_RED_GHOST, "Red Ghost");
        palette.addPropertyTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost");
        palette.addPropertyTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost");
        palette.addPropertyTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter");
        palette.addPropertyTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter");
        palette.selectTool(0); // "No actor"
        return palette;
    }

    static Palette createFoodPalette(byte id, int toolSize, TileMapEditor editor, TileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 3, renderer);
        palette.addTileTool(editor, FoodTiles.EMPTY, "No Food");
        palette.addTileTool(editor, FoodTiles.PELLET, "Pellet");
        palette.addTileTool(editor, FoodTiles.ENERGIZER, "Energizer");
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
