package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;
import de.amr.pacmanfx.tilemap.editor.TileMatcher;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.nio.IntBuffer;
import java.time.LocalTime;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BEFORE_MAZE;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BELOW_MAZE;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.getColorFromMap;

public class Action_FillMapFromTemplate extends AbstractEditorUIAction<Void> {

    private final WorldMap worldMap;
    private final Image templateImage;

    public Action_FillMapFromTemplate(TileMapEditorUI ui, WorldMap worldMap, Image templateImage) {
        super(ui);
        this.worldMap = worldMap;
        this.templateImage = templateImage;
    }

    @Override
    public Void execute() {
        if (templateImage == null) {
            return null;
        }

        Color fillColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, null);
        if (fillColor == null) {
            ui.messageDisplay().showMessage("No fill color defined", 3, MessageType.ERROR);
            return null;
        }
        Color strokeColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, null);
        if (strokeColor == null) {
            ui.messageDisplay().showMessage("No stroke color defined", 3, MessageType.ERROR);
            return null;
        }
        Color doorColor = getColorFromMap(worldMap, LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, Color.PINK);
        if (doorColor == null) {
            ui.messageDisplay().showMessage("No door color defined", 3, MessageType.ERROR);
            return null;
        }
        Color foodColor = getColorFromMap(worldMap, LayerID.FOOD, WorldMapProperty.COLOR_FOOD, null);
        if (foodColor == null) {
            ui.messageDisplay().showMessage("No food color defined", 3, MessageType.ERROR);
            return null;
        }
        TileMatcher matcher = new TileMatcher(Color.TRANSPARENT, fillColor, strokeColor, doorColor, foodColor);
        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();
        PixelReader rdr = templateImage.getPixelReader();
        if (rdr == null) {
            ui.messageDisplay().showMessage("Could not get pixel reader for this image", 5, MessageType.ERROR);
            return null;
        }

        LocalTime startTime = LocalTime.now();

        int numMazeRows = worldMap.numRows() - (EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE);
        int numMazeCols = worldMap.numCols();
        for (int row = 0; row < numMazeRows; ++row) {
            for (int col = 0; col < numMazeCols; ++col) {
                Vector2i worldMapTile = Vector2i.of(col, row + EMPTY_ROWS_BEFORE_MAZE);
                try {
                    int[] pixelsOfTile = new int[TS*TS]; // pixels row-wise
                    rdr.getPixels(col * TS, row * TS, TS, TS, pixelFormat, pixelsOfTile, 0, TS);
                    byte foodValue = matcher.matchFoodTile(pixelsOfTile);
                    if (foodValue == FoodTile.PELLET.code() || foodValue == FoodTile.ENERGIZER.code()) {
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
        Vector2i houseMinTile = worldMap.tiles()
                .filter(tile -> worldMap.content(LayerID.TERRAIN, tile) == TerrainTile.DARC_NW.$)
                .findFirst().orElse(null);

        Vector2i houseMaxTile = worldMap.tiles()
                .filter(tile -> worldMap.content(LayerID.TERRAIN, tile) == TerrainTile.DARC_SE.$)
                .findFirst().orElse(null);

        if (houseMinTile != null && houseMaxTile != null
                && houseMinTile.x() < houseMaxTile.x() && houseMinTile.y() < houseMaxTile.y()) {
            new Action_PlaceArcadeHouse(editor, worldMap, houseMinTile).execute();
        }

        java.time.Duration duration = java.time.Duration.between(startTime, LocalTime.now());
        ui.messageDisplay().showMessage("Map creation took %d milliseconds".formatted(duration.toMillis()), 5, MessageType.INFO);

        editor.setWorldMapChanged();
        editor.setEdited(true);

        return null;
    }
}
