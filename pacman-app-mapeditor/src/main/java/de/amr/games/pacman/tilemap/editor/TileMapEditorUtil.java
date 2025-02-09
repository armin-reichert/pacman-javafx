/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.tilemap.rendering.TileMapRenderer;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.net.URL;

import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static java.util.Objects.requireNonNull;

public interface TileMapEditorUtil {

    byte[][] GHOST_HOUSE_SHAPE = {
        {16, 8, 8, 14, 14, 8, 8, 17},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8, 18}
    };

    byte[][] CIRCLE_2x2 = {
        {TileEncoding.CORNER_NW, TileEncoding.CORNER_NE},
        {TileEncoding.CORNER_SW, TileEncoding.CORNER_SE}
    };

    static String urlString(String resourcePath) {
        URL url = requireNonNull(TileMapEditorUtil.class.getResource(resourcePath));
        return url.toExternalForm();
    }

    static byte mirroredTileContent(byte content) {
        return switch (content) {
            case TileEncoding.CORNER_NE -> TileEncoding.CORNER_NW;
            case TileEncoding.CORNER_NW -> TileEncoding.CORNER_NE;
            case TileEncoding.CORNER_SE -> TileEncoding.CORNER_SW;
            case TileEncoding.CORNER_SW -> TileEncoding.CORNER_SE;
            case TileEncoding.DCORNER_NE -> TileEncoding.DCORNER_NW;
            case TileEncoding.DCORNER_NW -> TileEncoding.DCORNER_NE;
            case TileEncoding.DCORNER_SE -> TileEncoding.DCORNER_SW;
            case TileEncoding.DCORNER_SW -> TileEncoding.DCORNER_SE;
            case TileEncoding.DCORNER_ANGULAR_NE -> TileEncoding.DCORNER_ANGULAR_NW;
            case TileEncoding.DCORNER_ANGULAR_NW -> TileEncoding.DCORNER_ANGULAR_NE;
            case TileEncoding.DCORNER_ANGULAR_SE -> TileEncoding.DCORNER_ANGULAR_SW;
            case TileEncoding.DCORNER_ANGULAR_SW -> TileEncoding.DCORNER_ANGULAR_SE;
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

    static String formatColor(Color color) {
        return String.format("rgb(%d,%d,%d)", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    static String formatColorHex(Color color) {
        return String.format("#%02x%02x%02x", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    static Color getColorFromMap(TileMap map, String key, Color defaultColor) {
        if (map.hasProperty(key)) {
            String colorSpec = map.getStringProperty(key);
            try {
                return Color.web(colorSpec);
            } catch (Exception x) {
                Logger.error("Could not create color from value '{}'", colorSpec);
                return defaultColor;
            }
        }
        return defaultColor;
    }

    static HBox filler(int pixels) {
        var filler = new HBox();
        filler.setMinWidth(pixels);
        filler.setMaxWidth(pixels);
        return filler;
    }

    static Palette createTerrainPalette(byte id, int toolSize, TileMapEditor editor, TileMapRenderer renderer) {
        var palette = new Palette(id, toolSize, 1, 23, renderer);
        palette.addTileTool(editor, TileEncoding.EMPTY, "Empty Space");
        palette.addTileTool(editor, TileEncoding.WALL_H, "Horiz. Wall");
        palette.addTileTool(editor, TileEncoding.WALL_V, "Vert. Wall");
        palette.addTileTool(editor, TileEncoding.DWALL_H, "Horiz. Double-Wall");
        palette.addTileTool(editor, TileEncoding.DWALL_V, "Vert. Double-Wall");
        palette.addTileTool(editor, TileEncoding.CORNER_NW, "NW Corner");
        palette.addTileTool(editor, TileEncoding.CORNER_NE, "NE Corner");
        palette.addTileTool(editor, TileEncoding.CORNER_SW, "SW Corner");
        palette.addTileTool(editor, TileEncoding.CORNER_SE, "SE Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_NW, "NW Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_NE, "NE Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_SW, "SW Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_SE, "SE Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_ANGULAR_NW, "NW Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_ANGULAR_NE, "NE Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_ANGULAR_SW, "SW Corner");
        palette.addTileTool(editor, TileEncoding.DCORNER_ANGULAR_SE, "SE Corner");
        palette.addTileTool(editor, TileEncoding.TUNNEL, "Tunnel");
        palette.addTileTool(editor, TileEncoding.DOOR, "Door");
        palette.addTileTool(editor, TileEncoding.ONE_WAY_UP, "One-Way Up");
        palette.addTileTool(editor, TileEncoding.ONE_WAY_RIGHT, "One-Way Right");
        palette.addTileTool(editor, TileEncoding.ONE_WAY_DOWN, "One-Way Down");
        palette.addTileTool(editor, TileEncoding.ONE_WAY_LEFT, "One-Way Left");

        palette.selectTool(0); // "No Tile"
        return palette;
    }

    static Palette createActorPalette(byte id, int toolSize, TileMapEditor editor, TerrainRendererInEditor renderer) {
        var palette = new Palette(id, toolSize, 1, 10, renderer);
        palette.addTileTool(editor, TileEncoding.EMPTY, "");
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
        palette.addTileTool(editor, TileEncoding.EMPTY, "No Food");
        palette.addTileTool(editor, TileEncoding.PELLET, "Pellet");
        palette.addTileTool(editor, TileEncoding.ENERGIZER, "Energizer");
        palette.selectTool(0); // "No Food"
        return palette;
    }
}
