/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
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

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.v2i;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.maps.editor.TileMapEditorViewModel.*;

/**
 * @author Armin Reichert
 */
public class EditController {

    public enum EditMode {DRAW, ERASE}

    // For now, here:
    static final byte[][] GHOST_HOUSE_SHAPE = {
        {16, 8, 8, 14, 14, 8, 8, 17},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {9, 0, 0, 0, 0, 0, 0, 9},
        {19, 8, 8, 8, 8, 8, 8, 18}
    };

    static final byte[][] CIRCLE_2x2 = {
        {Tiles.CORNER_NW, Tiles.CORNER_NE},
        {Tiles.CORNER_SW, Tiles.CORNER_SE}
    };

    static byte mirroredTileContent(byte content) {
        return switch (content) {
            case Tiles.CORNER_NE -> Tiles.CORNER_NW;
            case Tiles.CORNER_NW -> Tiles.CORNER_NE;
            case Tiles.CORNER_SE -> Tiles.CORNER_SW;
            case Tiles.CORNER_SW -> Tiles.CORNER_SE;
            case Tiles.DCORNER_NE -> Tiles.DCORNER_NW;
            case Tiles.DCORNER_NW -> Tiles.DCORNER_NE;
            case Tiles.DCORNER_SE -> Tiles.DCORNER_SW;
            case Tiles.DCORNER_SW -> Tiles.DCORNER_SE;
            case Tiles.DCORNER_ANGULAR_NE -> Tiles.DCORNER_ANGULAR_NW;
            case Tiles.DCORNER_ANGULAR_NW -> Tiles.DCORNER_ANGULAR_NE;
            case Tiles.DCORNER_ANGULAR_SE -> Tiles.DCORNER_ANGULAR_SW;
            case Tiles.DCORNER_ANGULAR_SW -> Tiles.DCORNER_ANGULAR_SE;
            default -> content;
        };
    }

    final BooleanProperty editingEnabledPy = new SimpleBooleanProperty(false);

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
            invalidateTerrainMapPaths();
            viewModel.updateSourceView();
        }
    };

    final ObjectProperty<EditMode> modePy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            switch (get()) {
                case EditMode.DRAW -> viewModel.indicateEditMode();
                case EditMode.ERASE -> viewModel.indicateEraseMode();
            }
        }
    };

    final BooleanProperty symmetricEditModePy = new SimpleBooleanProperty(true);

    private final TileMapEditorViewModel viewModel;
    private boolean unsavedChanges;
    private boolean terrainMapPathsUpToDate;

    private boolean dragging = false;

    private final ObstacleEditor obstacleEditor = new ObstacleEditor();

    EditController(TileMapEditorViewModel viewModel) {
        this.viewModel = viewModel;
        this.worldMapPy.bind(viewModel.worldMapProperty());
        viewModel.gridSizeProperty().addListener((py,ov,nv) -> invalidateTerrainMapPaths());
        obstacleEditor.enabledPy.bind(editingEnabledPy);
        setMode(EditMode.DRAW);
    }

    WorldMap editedWorldMap() {
        return worldMapPy.get();
    }

    //TODO data structure
    Vector2i editedContentMinTile() {
        return obstacleEditor.minTile;
    }

    Vector2i editedContentMaxTile() {
        return obstacleEditor.maxTile;
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
        Logger.info("Mouse clicked {}", event);
        if (event.getButton() == MouseButton.PRIMARY) {
            viewModel.canvas().requestFocus();
            viewModel.contextMenu().hide();
            if (event.getClickCount() == 2 && !editingEnabledPy.get()) {
                editingEnabledPy.set(true);
            }
        }
    }

    void onEditCanvasMouseDragged(MouseEvent event) {
        Logger.debug("Mouse dragged {}", event);
        if (!dragging) {
            Vector2i dragStartTile = tileAtMousePosition(event.getX(), event.getY());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.info("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAtMousePosition(event.getX(), event.getY()));
        }
    }

    void onEditCanvasMouseReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            Logger.info("Mouse released: {}", event);
            if (dragging) {
                dragging = false;
                Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
                Logger.info("Dragging ends at tile {}", tile);
                obstacleEditor.endEditing(tile);
            } else {
                editAtMousePosition(event);
            }
        }
    }

    void onEditCanvasMouseMoved(MouseEvent event) {
        Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
        focussedTilePy.set(tile);
        if (!editingEnabledPy.get()) {
            return;
        }
        WorldMap worldMap = worldMapPy.get();
        if (modePy.get() == EditMode.ERASE) {
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
                viewModel.readPrevMapFileInDirectory().ifPresentOrElse(
                    file -> showInfoMessage("Previous map file read: %s".formatted(file.getName()), 3),
                    () -> showErrorMessage("Previous file not available", 1));
            } else if (event.getCode() == KeyCode.RIGHT) {
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
        boolean editingEnabled = editingEnabledPy.get();
        switch (ch) {
            case "n" -> {
                if (editingEnabled) setNormalDrawMode();
            }
            case "s" -> {
                if (editingEnabled) {
                    setSymmetricDrawMode();
                }
            }
            case "x" -> {
                if (editingEnabled) {
                    if (modePy.get() == EditMode.ERASE) {
                        modePy.set(EditMode.DRAW);
                    } else {
                        setEraseMode();
                    }
                }
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
        if (editingEnabledPy.get()) {
            Vector2i tile = tileAtMousePosition(event.getX(), event.getY());
            WorldMap worldMap = worldMapPy.get();

            var miAddCircle2x2 = new MenuItem("2x2 Circle");
            miAddCircle2x2.setOnAction(actionEvent -> addShapeMirrored(worldMap.terrain(), CIRCLE_2x2, tile));
            miAddCircle2x2.disableProperty().bind(editingEnabledPy.not());

            var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
            miAddHouse.setOnAction(actionEvent -> addHouse(worldMap.terrain(), tile));
            miAddHouse.disableProperty().bind(editingEnabledPy.not());

            contextMenu.getItems().setAll(miAddCircle2x2, miAddHouse);
            contextMenu.show(viewModel.canvas(), event.getScreenX(), event.getScreenY());
        }
    }

    void setNormalDrawMode() {
        setMode(EditMode.DRAW);
        symmetricEditModePy.set(false);
    }

    void setSymmetricDrawMode() {
        setMode(EditMode.DRAW);
        symmetricEditModePy.set(true);
    }

    void setEraseMode() {
        setMode(EditMode.ERASE);
    }

    void setMode(EditMode mode) {
        modePy.set(checkNotNull(mode));
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

    void invalidateTerrainMapPaths() {
        terrainMapPathsUpToDate = false;
    }

    void ensureTerrainMapsPathsUpToDate() {
        if (!terrainMapPathsUpToDate) {
            worldMapPy.get().terrain().computeTerrainPaths();
            terrainMapPathsUpToDate = true;
        }
    }

    void markTileMapEdited(TileMap tileMap) {
        unsavedChanges = true;
        WorldMap worldMap = worldMapPy.get();
        if (worldMap != null) {
            viewModel.updateSourceView();
            if (tileMap == worldMap.terrain()) {
                invalidateTerrainMapPaths();
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
        if (!editingEnabledPy.get()) {
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

    void clearFood(WorldMap worldMap) {
        worldMap.food().clear();
        markTileMapEdited(worldMap.food());
    }

    void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
        for (int row = emptyRowsTop; row < terrain.numRows() - emptyRowsBottom; ++row) {
            terrain.set(row, 0, Tiles.DWALL_V);
            terrain.set(row, terrain.numCols() - 1, Tiles.DWALL_V);
        }
        for (int col = 1; col < terrain.numCols() - 1; ++col) {
            terrain.set(emptyRowsTop, col, Tiles.DWALL_H);
            terrain.set(terrain.numRows() - 1 - emptyRowsBottom, col, Tiles.DWALL_H);
        }
        terrain.set(emptyRowsTop, 0, Tiles.DCORNER_NW);
        terrain.set(emptyRowsTop, terrain.numCols() - 1, Tiles.DCORNER_NE);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, 0, Tiles.DCORNER_SW);
        terrain.set(terrain.numRows() - 1 - emptyRowsBottom, terrain.numCols() - 1, Tiles.DCORNER_SE);

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
        checkNotNull(tileMap);
        checkNotNull(tile);
        if (editingEnabledPy.get()) {
            tileMap.set(tile, value);
            if (symmetricEditModePy.get()) {
                tileMap.set(tile.y(), tileMap.numCols() - 1 - tile.x(), mirroredTileContent(tileMap.get(tile)));
            }
        }
        markTileMapEdited(tileMap);
    }

    void eraseTileValue(TileMap tileMap, Vector2i tile) {
        tileMap.set(tile, Tiles.EMPTY);
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

        Vector2i houseOrigin = v2i(tilesX / 2 - 4, tilesY / 2 - 3);

        addBorder(terrain, 3, 2);
        addHouse(terrain, houseOrigin);

        terrain.setProperty(PROPERTY_COLOR_WALL_STROKE, DEFAULT_COLOR_WALL_STROKE);
        terrain.setProperty(PROPERTY_COLOR_WALL_FILL, DEFAULT_COLOR_WALL_FILL);
        terrain.setProperty(PROPERTY_COLOR_DOOR, DEFAULT_COLOR_DOOR);

        terrain.setProperty(PROPERTY_POS_PAC, formatTile(houseOrigin.plus(3, 11)));
        terrain.setProperty(PROPERTY_POS_BONUS, formatTile(houseOrigin.plus(3, 5)));

        terrain.setProperty(PROPERTY_POS_SCATTER_RED_GHOST, formatTile(v2i(tilesX - 3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_PINK_GHOST, formatTile(v2i(3, 0)));
        terrain.setProperty(PROPERTY_POS_SCATTER_CYAN_GHOST, formatTile(v2i(tilesX - 1, tilesY - 2)));
        terrain.setProperty(PROPERTY_POS_SCATTER_ORANGE_GHOST, formatTile(v2i(0, tilesY - 2)));

        invalidateTerrainMapPaths();

        worldMap.food().setProperty(PROPERTY_COLOR_FOOD, DEFAULT_COLOR_FOOD);

        Logger.info("Map created. rows={}, cols={}", tilesY, tilesX);
        return worldMap;
    }


    // Testing

    private class ObstacleEditor {

        private BooleanProperty enabledPy = new SimpleBooleanProperty();

        private Vector2i anchor;
        private Vector2i frontier;
        private Vector2i minTile; // top left corner
        private Vector2i maxTile; // bottom right corner
        private boolean join = true;

        boolean isDisabled() {
            return !(enabledPy.get() &&
                viewModel.selectedPaletteID() == PALETTE_ID_TERRAIN &&
                viewModel.selectedPalette().getSelectedRow() == 0 &&
                viewModel.selectedPalette().getSelectedCol() == 0);
        }

        void startEditing(Vector2i tile) {
            if (isDisabled()) {
                return;
            }
            Logger.info("Start inserting oval at tile {}", tile);
            minTile = maxTile = anchor = frontier = tile;
        }

        void continueEditing(Vector2i tile) {
            if (isDisabled()) {
                return;
            }
            if (tile.equals(frontier)) {
                return;
            }
            Logger.info("Continue inserting oval at tile {}", tile);
            frontier = tile;
            int dx = frontier.x() - anchor.x(), dy = frontier.y() - anchor.y();
            if (dx >= 0) {
                if (dy >= 0) {
                    // frontier right-down
                    minTile = anchor;
                    maxTile = frontier;
                } else {
                    // frontier right-up
                    minTile = new Vector2i(anchor.x(), frontier.y());
                    maxTile = new Vector2i(frontier.x(), anchor.y());
                }
            } else {
                if (dy >= 0) {
                    // frontier left-down
                    minTile = new Vector2i(frontier.x(), anchor.y());
                    maxTile = new Vector2i(anchor.x(), frontier.y());
                } else {
                    // frontier left-up
                    minTile = frontier;
                    maxTile = anchor;
                }
            }
            Logger.info("Min tile {} max tile {}", minTile, maxTile);
        }

        void endEditing(Vector2i tile) {
            if (isDisabled()) {
                return;
            }
            Logger.info("End inserting oval at tile {}", tile);
            commit();
        }

        void commit() {
            byte[][] editedContent = editedContent();
            if (editedContent != null) {
                if (join) {
                    editedContent = joinedContent(editedContent, editedContent.length, editedContent[0].length);
                }
                copy(editedContent, viewModel.worldMapProperty().get().terrain());
            }
            anchor = frontier = minTile = maxTile = null;
        }

        byte[][] joinedContent(byte[][] editedContent, int numRows, int numCols) {
            byte[][] newContent = new byte[numRows][numCols];
            for (int row = 0; row < numRows; ++row) {
                System.arraycopy(editedContent[row], 0, newContent[row], 0, numCols);
            }
            TileMap originalTerrain = viewModel.worldMapProperty().get().terrain();
            int crossings;

            crossings = 0;
            int leftBorder = minTile.x();
            for (int row = minTile.y(); row < maxTile.y(); ++row) {
                int x = 0, y = row - minTile.y();
                if (editedContent[y][x] == Tiles.WALL_V && originalTerrain.get(row, leftBorder) == Tiles.WALL_H) {
                    newContent[y][x] = Globals.isEven(crossings) ? Tiles.CORNER_SE : Tiles.CORNER_NE;
                    ++crossings;
                }
            }

            crossings = 0;
            int rightBorder = maxTile.x();
            for (int row = minTile.y(); row < maxTile.y(); ++row) {
                int x = rightBorder - minTile.x(), y = row - minTile.y();
                if (editedContent[y][x] == Tiles.WALL_V && originalTerrain.get(row, leftBorder) == Tiles.WALL_H) {
                    newContent[y][x] = Globals.isEven(crossings) ? Tiles.CORNER_SW : Tiles.CORNER_NW;
                    ++crossings;
                }
            }

            crossings = 0;
            int upperBorder = minTile.y(); // upper border
            for (int col = minTile.x(); col < maxTile.x(); ++col) {
                int x = col - minTile.x(), y = upperBorder - minTile.y();
                if (editedContent[y][x] == Tiles.WALL_H && originalTerrain.get(upperBorder, col) == Tiles.WALL_V) {
                    newContent[y][x] = Globals.isEven(crossings) ? Tiles.CORNER_SE : Tiles.CORNER_SW;
                    ++crossings;
                }
            }
            crossings = 0;
            int lowerBorder = maxTile.y(); // lower border
            for (int col = minTile.x(); col < maxTile.x(); ++col) {
                int x = col - minTile.x(), y = lowerBorder - minTile.y();
                if (editedContent[y][x] == Tiles.WALL_H && originalTerrain.get(lowerBorder, col) == Tiles.WALL_V) {
                    newContent[y][x] = Globals.isEven(crossings) ? Tiles.CORNER_NE : Tiles.CORNER_NW;
                    ++crossings;
                }
            }

            return newContent;
        }

        byte[][] editedContent() {
            if (minTile == null || maxTile == null) {
                return null;
            }
            int numRows = maxTile.y() - minTile.y() + 1;
            int numCols = maxTile.x() - minTile.x() + 1;
            if (numRows <= 1 || numCols <= 1) {
                return null;
            }
            byte[][] area = new byte[numRows][numCols];
            for (int row = minTile.y(); row <= maxTile.y(); ++row) {
                for (int col = minTile.x(); col <= maxTile.x(); ++col) {
                    byte value = Tiles.EMPTY;
                    if (row == minTile.y() && col == minTile.x()) {
                        value = Tiles.CORNER_NW;
                    } else if (row == minTile.y() && col == maxTile.x()) {
                        value = Tiles.CORNER_NE;
                    } else if (row == maxTile.y() && col == minTile.x()) {
                        value = Tiles.CORNER_SW;
                    } else if (row == maxTile.y() && col == maxTile.x()) {
                        value = Tiles.CORNER_SE;
                    } else if (row == minTile.y() || row == maxTile.y()) {
                        value = Tiles.WALL_H;
                    } else if (col == minTile.x() || col == maxTile.x()) {
                        value = Tiles.WALL_V;
                    }
                    area[row - minTile.y()][col - minTile.x()] = value;
                }
            }
            return area;
        }

        void copy(byte[][] values, TileMap tileMap) {
            int numRows = values.length;
            int numCols = values[0].length;
            for (int row = 0; row < numRows; ++row) {
                for (int col = 0; col < numCols; ++col) {
                    Vector2i tile = new Vector2i(minTile.x() + col, minTile.y() + row);
                    setTileValue(tileMap, tile, values[row][col]);
                }
            }
        }
    }

} // EditController
