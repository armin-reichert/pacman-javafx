/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.TileMatcher;
import de.amr.pacmanfx.model.world.*;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.time.LocalTime;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.UfxMapEditor.getColorFromMapLayer;

public class Action_FillMapFromTemplate extends EditorUIAction<Void> {

    public Action_FillMapFromTemplate(TileMapEditorUI ui) {
        super(ui);
    }

    @Override
    public Void execute() {
        if (editor.templateImage() == null) {
            return null;
        }

        final WorldMap worldMap = editor.currentWorldMap();
        final WorldMapLayer terrain = worldMap.terrainLayer();

        Color fillColor = getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_WALL_FILL, null);
        if (fillColor == null) {
            ui.messageDisplay().showMessage("No fill color defined", 3, MessageType.ERROR);
            return null;
        }
        Color strokeColor = getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_WALL_STROKE, null);
        if (strokeColor == null) {
            ui.messageDisplay().showMessage("No stroke color defined", 3, MessageType.ERROR);
            return null;
        }
        Color doorColor = getColorFromMapLayer(terrain, WorldMapPropertyName.COLOR_DOOR, Color.PINK);
        if (doorColor == null) {
            ui.messageDisplay().showMessage("No door color defined", 3, MessageType.ERROR);
            return null;
        }
        Color foodColor = getColorFromMapLayer(worldMap.foodLayer(), WorldMapPropertyName.COLOR_FOOD, null);
        if (foodColor == null) {
            ui.messageDisplay().showMessage("No food color defined", 3, MessageType.ERROR);
            return null;
        }

        TileMatcher matcher = new TileMatcher(Color.TRANSPARENT, fillColor, strokeColor, doorColor, foodColor);
        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();
        PixelReader rdr = editor.templateImage().getPixelReader();
        if (rdr == null) {
            ui.messageDisplay().showMessage("Could not get pixel reader for this image", 5, MessageType.ERROR);
            return null;
        }

        LocalTime startTime = LocalTime.now();

        int emptyRowsTop = worldMap.terrainLayer().emptyRowsOverMaze();
        int emptyRowsBottom = worldMap.terrainLayer().emptyRowsBelowMaze();
        int numMazeRows = worldMap.numRows() - (emptyRowsTop + emptyRowsBottom);
        int numMazeCols = worldMap.numCols();
        for (int row = 0; row < numMazeRows; ++row) {
            for (int col = 0; col < numMazeCols; ++col) {
                Vector2i worldMapTile = Vector2i.of(col, row + emptyRowsTop);
                try {
                    int[] pixelsOfTile = new int[TS*TS]; // pixels row-wise
                    rdr.getPixels(col * TS, row * TS, TS, TS, pixelFormat, pixelsOfTile, 0, TS);
                    byte foodValue = matcher.matchFoodTile(pixelsOfTile);
                    if (foodValue == FoodTile.PELLET.$ || foodValue == FoodTile.ENERGIZER.$) {
                        worldMap.foodLayer().setContent(worldMapTile, foodValue);
                    } else {
                        byte terrainValue = matcher.matchTerrainTile(pixelsOfTile);
                        worldMap.terrainLayer().setContent(worldMapTile, terrainValue);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Logger.error("Could not get pixels for tile {}, maybe image has been cropped incorrectly?", worldMapTile);
                } catch (Exception e) {
                    Logger.error("Could not get pixels for tile {}", worldMapTile);
                    Logger.error(e);
                }
            }
        }

        // Find house: requires that at least min and max tiles have been detected
        Vector2i houseMinTile = worldMap.terrainLayer().tiles()
                .filter(tile -> worldMap.terrainLayer().content(tile) == TerrainTile.ANG_ARC_NW.$)
                .findFirst().orElse(null);

        Vector2i houseMaxTile = worldMap.terrainLayer().tiles()
                .filter(tile -> worldMap.terrainLayer().content(tile) == TerrainTile.ANG_ARC_SE.$)
                .findFirst().orElse(null);

        if (houseMinTile != null && houseMaxTile != null
                && houseMinTile.x() < houseMaxTile.x() && houseMinTile.y() < houseMaxTile.y()) {
            new Action_MoveArcadeHouse(editor, houseMinTile).execute();
        }

        editor.setWorldMapChanged();
        editor.setEdited(true);

        java.time.Duration duration = java.time.Duration.between(startTime, LocalTime.now());
        ui.messageDisplay().showMessage("Map creation took %d milliseconds".formatted(duration.toMillis()), 5, MessageType.INFO);
        ui.selectEditCanvasTab();

        //TODO localize
        ui.messageDisplay().showMessage("Add tunnels and missing actor positions if needed!", 5, MessageType.INFO);

        return null;
    }
}