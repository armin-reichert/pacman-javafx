/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileEncoding;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.Globals.vec_2i;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.maps.editor.TileMapEditorViewModel.*;

/**
 * @author Armin Reichert
 */
public class EditController {

    // For now, here:
    static final byte[][] GHOST_HOUSE_SHAPE = {
        {16, 8, 8, 14, 14, 8, 8, 17},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8, 18}
    };

    static final byte[][] CIRCLE_2x2 = {
        {TileEncoding.CORNER_NW, TileEncoding.CORNER_NE},
        {TileEncoding.CORNER_SW, TileEncoding.CORNER_SE}
    };

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

    final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>();

    final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            WorldMap map = get();
            if (viewModel.foodPropertiesEditor() != null) {
                viewModel.foodPropertiesEditor().setTileMap(map.food());
            }
            if (viewModel.terrainPropertiesEditor() != null) {
                viewModel.terrainPropertiesEditor().setTileMap(map.terrain());
            }
            invalidateTerrainData();
            viewModel.updateSourceView();
        }
    };

    final ObjectProperty<EditMode> modePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            switch (get()) {
                case INSPECT -> viewModel.indicateInspectMode();
                case DRAW -> viewModel.indicateEditMode();
                case ERASE -> viewModel.indicateEraseMode();
            }
        }
    };

    final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(true);

    private final TileMapEditorViewModel viewModel;
    private final ObstacleEditor obstacleEditor;
    private boolean unsavedChanges;
    private boolean terrainDataUpToDate;
    private boolean dragging = false;
    private final List<Vector2i> tilesWithErrors = new ArrayList<>();

    EditController(TileMapEditorViewModel viewModel) {
        this.viewModel = viewModel;
        this.worldMapPy.bind(viewModel.worldMapProperty());
        viewModel.gridSizeProperty().addListener((py,ov,nv) -> invalidateTerrainData());
        obstacleEditor = new ObstacleEditor(this, viewModel);
        obstacleEditor.enabledPy.bind(modePy.map(mode -> mode != EditMode.INSPECT));
        setMode(EditMode.INSPECT);
    }

    Vector2i editedContentMinTile() {
        return obstacleEditor.minTile();
    }

    Vector2i editedContentMaxTile() {
        return obstacleEditor.maxTile();
    }

    byte[][] editedContent() {
        return obstacleEditor.editedContent();
    }

    void initEventHandlers() {
        Canvas editCanvas = viewModel.canvas();
        editCanvas.setOnMouseClicked(this::onEditCanvasMouseClicked);
        editCanvas.setOnMouseReleased(this::onEditCanvasMouseReleased);
        editCanvas.setOnMouseDragged(this::onEditCanvasMouseDragged);
        editCanvas.setOnMouseMoved(this::onEditCanvasMouseMoved);
        editCanvas.setOnKeyPressed(this::onEditCanvasKeyPressed);
    }

    void onEditCanvasMouseClicked(MouseEvent event) {
        Logger.debug("Mouse clicked {}", event);
        if (event.getButton() == MouseButton.PRIMARY) {
            viewModel.canvas().requestFocus();
            viewModel.contextMenu().hide();
            if (event.getClickCount() == 2 && isMode(EditMode.INSPECT)) {
                setMode(EditMode.DRAW);
            }
        }
    }

    void onEditCanvasMouseDragged(MouseEvent event) {
        Logger.debug("Mouse dragged {}", event);
        if (!dragging) {
            Vector2i dragStartTile = tileAtMousePosition(event.getX(), event.getY());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.debug("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAtMousePosition(event.getX(), event.getY()));
        }
    }

    void onEditCanvasMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            Logger.debug("Mouse released: {}", event);
            if (dragging) {
                dragging = false;
                Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
                Logger.debug("Dragging ends at tile {}", tile);
                obstacleEditor.endEditing(tile);
            } else {
                editAtMousePosition(event);
            }
        }
    }

    void onEditCanvasMouseMoved(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        focussedTilePy.set(tile);
        if (isMode(EditMode.INSPECT)) {
            return;
        }
        WorldMap worldMap = worldMapPy.get();
        if (isMode(EditMode.ERASE)) {
            switch (viewModel.selectedPaletteID()) {
                case PALETTE_ID_TERRAIN -> eraseTileValue(worldMap.terrain(), tile);
                case PALETTE_ID_FOOD -> eraseTileValue(worldMap.food(), tile);
            }
        } else {
            if (event.isShiftDown()) {
                switch (viewModel.selectedPaletteID()) {
                    case PALETTE_ID_TERRAIN -> {
                        if (viewModel.selectedPalette().isToolSelected()) {
                            viewModel.selectedPalette().selectedTool().apply(worldMap.terrain(), focussedTilePy.get());
                        }
                        markTileMapEdited(worldMap.terrain());
                    }
                    case PALETTE_ID_FOOD -> {
                        if (viewModel.selectedPalette().isToolSelected()) {
                            viewModel.selectedPalette().selectedTool().apply(worldMap.food(), focussedTilePy.get());
                        }
                        markTileMapEdited(worldMap.food());
                    }
                    default -> {
                    }
                }
            }
        }
    }

    void onKeyPressed(KeyEvent event) {
        if (event.isAltDown()) {
            if (event.getCode() == KeyCode.LEFT) {
                event.consume();
                viewModel.readPrevMapFileInDirectory().ifPresentOrElse(
                    file -> showInfoMessage("Previous map file read: %s".formatted(file.getName()), 3),
                    () -> showErrorMessage("Previous file not available", 1));
            } else if (event.getCode() == KeyCode.RIGHT) {
                event.consume();
                viewModel.readNextMapFileInDirectory().ifPresentOrElse(
                    file -> showInfoMessage("Next map file read: %s".formatted(file.getName()), 3),
                    () -> showErrorMessage("Next file not available", 1));
            }
        }
    }

    void onEditCanvasKeyPressed(KeyEvent event) {
        Direction cursor = switch (event.getCode()) {
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
        if (cursor != null && focussedTilePy.get() != null) {
            WorldMap worldMap = worldMapPy.get();
            Vector2i newTile = focussedTilePy.get().plus(cursor.vector());
            if (!worldMap.terrain().outOfBounds(newTile)) {
                focussedTilePy.set(newTile);
            }
        }
    }

    void onKeyTyped(KeyEvent event) {
        Logger.debug("Typed {}", event);
        String ch = event.getCharacter();
        switch (ch) {
            case "i" -> {
                setMode(EditMode.INSPECT);
            }
            case "n" -> {
                setMode(EditMode.DRAW);
                symmetricEditModePy.set(false);
            }
            case "s" -> {
                setMode(EditMode.DRAW);
                symmetricEditModePy.set(true);
            }
            case "x" -> {
                setMode(isMode(EditMode.ERASE) ? EditMode.INSPECT : EditMode.ERASE);
            }
            case "+" -> {
                if (viewModel.gridSizeProperty().get() < MAX_GRID_SIZE) {
                    viewModel.gridSizeProperty().set(viewModel.gridSizeProperty().get() + 1);
                }
            }
            case "-" -> {
                if (viewModel.gridSizeProperty().get() > MIN_GRID_SIZE) {
                    viewModel.gridSizeProperty().set(viewModel.gridSizeProperty().get() - 1);
                }
            }
        }
    }

    void onEditCanvasContextMenuRequested(ContextMenu contextMenu, ContextMenuEvent event) {
        if (!isMode(EditMode.INSPECT)) {
            Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
            WorldMap worldMap = worldMapPy.get();

            var miAddCircle2x2 = new MenuItem("2x2 Circle");
            miAddCircle2x2.setOnAction(actionEvent -> addShapeMirrored(worldMap.terrain(), CIRCLE_2x2, tile));

            var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
            miAddHouse.setOnAction(actionEvent -> addHouse(worldMap.terrain(), tile));

            contextMenu.getItems().setAll(miAddCircle2x2, miAddHouse);
            contextMenu.show(viewModel.canvas(), event.getScreenX(), event.getScreenY());
        }
    }

    EditMode mode() { return modePy.get(); }

    boolean isMode(EditMode mode) { return mode() == mode; }

    void setMode(EditMode mode) {
        modePy.set(assertNotNull(mode));
    }

    boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    void clearUnsavedChanges() {
        unsavedChanges = false;
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / viewModel.gridSizeProperty().get());
    }

    void invalidateTerrainData() {
        terrainDataUpToDate = false;
    }

    void ensureTerrainMapsPathsUpToDate() {
        if (!terrainDataUpToDate) {
            WorldMap worldMap = worldMapPy.get();
            tilesWithErrors.clear();
            tilesWithErrors.addAll(worldMap.updateObstacleList());
            terrainDataUpToDate = true;
        }
    }

    void markTileMapEdited(TileMap tileMap) {
        unsavedChanges = true;
        WorldMap worldMap = worldMapPy.get();
        if (worldMap != null) {
            viewModel.updateSourceView();
            if (tileMap == worldMap.terrain()) {
                invalidateTerrainData();
            }
        }
    }

    Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    void editMapTileAtMousePosition(TileMap tileMap, Vector2i tile, boolean erase) {
        if (erase) { // Control-Click clears tile content
            eraseTileValue(tileMap, tile);
        } else if (viewModel.selectedPalette().isToolSelected()) {
            viewModel.selectedPalette().selectedTool().apply(tileMap, tile);
        }
    }

    void editAtMousePosition(MouseEvent event) {
        if (isMode(EditMode.INSPECT)) {
            return;
        }
        WorldMap worldMap = worldMapPy.get();
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        boolean erase = event.isControlDown();
        switch (viewModel.selectedPaletteID()) {
            case PALETTE_ID_TERRAIN -> editMapTileAtMousePosition(worldMap.terrain(), tile, erase);
            case PALETTE_ID_ACTORS -> {
                if (viewModel.selectedPalette().isToolSelected()) {
                    viewModel.selectedPalette().selectedTool().apply(worldMap.terrain(), tile);
                    markTileMapEdited(worldMap.terrain());
                    viewModel.terrainPropertiesEditor().updatePropertyEditorValues();
                }
            }
            case PALETTE_ID_FOOD -> editMapTileAtMousePosition(worldMap.food(), tile, erase);
            default -> Logger.error("Unknown palette selection");
        }
    }

    void clearTerrain(WorldMap worldMap) {
        worldMap.terrain().clear();
    }

    public List<Vector2i> tilesWithErrors() {
        return tilesWithErrors;
    }

    void clearFood(WorldMap worldMap) {
        worldMap.food().clear();
        markTileMapEdited(worldMap.food());
    }

    void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
        for (int row = emptyRowsTop; row < terrain.numRows() - emptyRowsBottom; ++row) {
            terrain.set(row, 0, TileEncoding.DWALL_V);
            terrain.set(row, terrain.numCols() - 1, TileEncoding.DWALL_V);
        }
        for (int col = 1; col < terrain.numCols() - 1; ++col) {
            terrain.set(emptyRowsTop, col, TileEncoding.DWALL_H);
            terrain.set(terrain.numRows() - 1 - emptyRowsBottom, col, TileEncoding.DWALL_H);
        }
        terrain.set(emptyRowsTop, 0, TileEncoding.DCORNER_NW);
        terrain.set(emptyRowsTop, terrain.numCols() - 1, TileEncoding.DCORNER_NE);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, 0, TileEncoding.DCORNER_SW);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, terrain.numCols() - 1, TileEncoding.DCORNER_SE);

        markTileMapEdited(terrain);
    }

    void addHouse(TileMap terrain, Vector2i tile) {
        addShape(terrain, GHOST_HOUSE_SHAPE, tile);
        terrain.setProperty(PROPERTY_POS_HOUSE_MIN_TILE, formatTile(tile));
        terrain.setProperty(PROPERTY_POS_RED_GHOST, formatTile(tile.plus(3, -1)));
        terrain.setProperty(PROPERTY_POS_CYAN_GHOST, formatTile(tile.plus(1, 2)));
        terrain.setProperty(PROPERTY_POS_PINK_GHOST, formatTile(tile.plus(3, 2)));
        terrain.setProperty(PROPERTY_POS_ORANGE_GHOST, formatTile(tile.plus(5, 2)));

        viewModel.terrainPropertiesEditor().rebuildPropertyEditors();
    }

    void addShapeMirrored(TileMap map, byte[][] content, Vector2i originTile) {
        int numRows = content.length, numCols = content[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                setTileValue(map, originTile.plus(col, row), content[row][col]);
            }
        }
        markTileMapEdited(map);
    }

    void addShape(TileMap map, byte[][] content, Vector2i originTile) {
        int numRows = content.length, numCols = content[0].length;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                map.set(originTile.plus(col, row), content[row][col]);
            }
        }
        markTileMapEdited(map);
    }

    /**
     * This method should be used whenever a tile value is set! It takes editor enabled state and symmetric editing mode
     * into account.
     */
    public void setTileValue(TileMap tileMap, Vector2i tile, byte value) {
        assertNotNull(tileMap);
        assertNotNull(tile);
        tileMap.set(tile, value);
        if (symmetricEditModePy.get()) {
            tileMap.set(tile.y(), tileMap.numCols() - 1 - tile.x(), mirroredTileContent(tileMap.get(tile)));
        }
        markTileMapEdited(tileMap);
    }

    void eraseTileValue(TileMap tileMap, Vector2i tile) {
        tileMap.set(tile, TileEncoding.EMPTY);
        markTileMapEdited(tileMap);
    }

    public void showInfoMessage(String message, long seconds) {
        viewModel.showMessage(message, seconds, MessageType.INFO);
    }

    public void showWarningMessage(String message, long seconds) {
        viewModel.showMessage(message, seconds, MessageType.WARNING);
    }

    public void showErrorMessage(String message, long seconds) {
        viewModel.showMessage(message, seconds, MessageType.ERROR);
    }

    WorldMap createPreconfiguredMap(int tilesX, int tilesY) {
        var worldMap = new WorldMap(tilesY, tilesX);
        TileMap terrain = worldMap.terrain();

        Vector2i houseOrigin = vec_2i(tilesX / 2 - 4, tilesY / 2 - 3);

        addBorder(terrain, 3, 2);
        addHouse(terrain, houseOrigin);

        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, DEFAULT_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL, DEFAULT_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR, DEFAULT_COLOR_DOOR);

        terrain.setProperty(PROPERTY_POS_PAC, formatTile(houseOrigin.plus(3, 11)));
        terrain.setProperty(PROPERTY_POS_BONUS, formatTile(houseOrigin.plus(3, 5)));

        terrain.setProperty(PROPERTY_POS_SCATTER_RED_GHOST, formatTile(vec_2i(tilesX - 3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_PINK_GHOST, formatTile(vec_2i(3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_CYAN_GHOST, formatTile(vec_2i(tilesX - 1, tilesY - 2)));
        terrain.setProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, formatTile(vec_2i(0, tilesY - 2)));

        invalidateTerrainData();

        worldMap.food().setProperty(PROPERTY_COLOR_FOOD, DEFAULT_COLOR_FOOD);

        Logger.info("Map created. rows={}, cols={}", tilesY, tilesX);
        return worldMap;
    }

} // EditController
