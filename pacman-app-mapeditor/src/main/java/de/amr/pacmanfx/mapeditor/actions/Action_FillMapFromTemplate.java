/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.worldmap.*;
import de.amr.pacmanfx.mapeditor.MessageType;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.TileMatcher;
import de.amr.pacmanfx.model.DefaultWorldMapPropertyName;
import de.amr.pacmanfx.model.GameLevel;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.time.LocalTime;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.mapeditor.EditorUtil.getColorFromMapLayer;

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

        Color fillColor = getColorFromMapLayer(terrain, DefaultWorldMapPropertyName.COLOR_WALL_FILL, null);
        if (fillColor == null) {
            ui.messageDisplay().showMessage("No fill color defined", 3, MessageType.ERROR);
            return null;
        }
        Color strokeColor = getColorFromMapLayer(terrain, DefaultWorldMapPropertyName.COLOR_WALL_STROKE, null);
        if (strokeColor == null) {
            ui.messageDisplay().showMessage("No stroke color defined", 3, MessageType.ERROR);
            return null;
        }
        Color doorColor = getColorFromMapLayer(terrain, DefaultWorldMapPropertyName.COLOR_DOOR, Color.PINK);
        if (doorColor == null) {
            ui.messageDisplay().showMessage("No door color defined", 3, MessageType.ERROR);
            return null;
        }
        Color foodColor = getColorFromMapLayer(worldMap.foodLayer(), DefaultWorldMapPropertyName.COLOR_FOOD, null);
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

        int numMazeRows = worldMap.numRows() - (GameLevel.EMPTY_ROWS_OVER_MAZE + GameLevel.EMPTY_ROWS_BELOW_MAZE);
        int numMazeCols = worldMap.numCols();
        for (int row = 0; row < numMazeRows; ++row) {
            for (int col = 0; col < numMazeCols; ++col) {
                Vector2i worldMapTile = Vector2i.of(col, row + GameLevel.EMPTY_ROWS_OVER_MAZE);
                try {
                    int[] pixelsOfTile = new int[TS*TS]; // pixels row-wise
                    rdr.getPixels(col * TS, row * TS, TS, TS, pixelFormat, pixelsOfTile, 0, TS);
                    byte foodValue = matcher.matchFoodTile(pixelsOfTile);
                    if (foodValue == FoodTile.PELLET.$ || foodValue == FoodTile.ENERGIZER.$) {
                        worldMap.setContent(LayerID.FOOD, worldMapTile, foodValue);
                    } else {
                        byte terrainValue = matcher.matchTerrainTile(pixelsOfTile);
                        worldMap.setContent(LayerID.TERRAIN, worldMapTile, terrainValue);
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
                .filter(tile -> worldMap.terrainLayer().get(tile) == TerrainTile.ANG_ARC_NW.$)
                .findFirst().orElse(null);

        Vector2i houseMaxTile = worldMap.terrainLayer().tiles()
                .filter(tile -> worldMap.terrainLayer().get(tile) == TerrainTile.ANG_ARC_SE.$)
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