/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.TerrainMapColorScheme;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.function.Predicate;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
import static de.amr.games.pacman.lib.tilemap.WorldMap.PROPERTY_COLOR_FOOD;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.MS_PACMAN_COLOR_FOOD;
import static de.amr.games.pacman.tilemap.editor.TileMapEditor.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.*;

public class EditCanvas {

    public static final Cursor CURSOR_RUBBER = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    private final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>();
    private final IntegerProperty gridSizePy = new SimpleIntegerProperty(8);
    private final ObjectProperty<Image> templateImageGreyPy = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private final Canvas canvas;
    private final TileMapEditor editor;
    private final ObstacleEditor obstacleEditor;
    private final ContextMenu contextMenu = new ContextMenu();

    private boolean dragging = false;

    public EditCanvas(TileMapEditor editor) {
        this.editor = assertNotNull(editor);

        obstacleEditor = new ObstacleEditor() {
            @Override
            public void setValue(Vector2i tile, byte value) {
                editor.setTileValue(editor.editedWorldMap(), LayerID.TERRAIN, tile, value);
            }
        };
        obstacleEditor.joiningProperty().bind(editor.obstaclesJoiningProperty());
        obstacleEditor.worldMapProperty().bind(worldMapPy);
        obstacleEditor.symmetricEditProperty().bind(editor.symmetricEditProperty());

        gridSizePy.bind(editor.gridSizeProperty());
        worldMapPy.bind(editor.editedWorldMapProperty());
        templateImageGreyPy.bind(editor.templateImageProperty().map(Ufx::imageToGreyscale));

        canvas = new Canvas();
        canvas.heightProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().numRows() * gridSize(), worldMapPy, gridSizePy));

        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().numCols() * gridSize(), worldMapPy, gridSizePy));

        canvas.setOnContextMenuRequested(this::onContextMenuRequested);
        canvas.setOnMouseClicked(this::onMouseClicked);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseMoved(this::onMouseMoved);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnKeyPressed(this::onKeyPressed);
    }

    public Canvas canvas() { return canvas; }

    private int gridSize() { return gridSizePy.get(); }

    private WorldMap worldMap() { return worldMapPy.get(); }

    public ObjectProperty<Vector2i> focussedTileProperty() { return focussedTilePy; }

    public Vector2i focussedTile() { return focussedTilePy.get(); }

    public boolean moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().outOfBounds(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTilePy.set(nextTile);
                return true;
            }
        }
        return false;
    }

    public void enterInspectMode() {
        canvas.setCursor(Cursor.HAND); // TODO use other cursor
        obstacleEditor.setEnabled(false);
    }

    public void enterEditMode() {
        canvas.setCursor(Cursor.DEFAULT);
        obstacleEditor.setEnabled(true);
    }

    public void enterEraseMode() {
        canvas.setCursor(CURSOR_RUBBER);
        obstacleEditor.setEnabled(false);
    }

    public void draw(TerrainMapColorScheme colors) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);

        double scaling = gridSize() / (double) TS;

        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (templateImageGreyPy.get() != null) {
            g.drawImage(templateImageGreyPy.get(),
                0, EMPTY_ROWS_BEFORE_MAZE * scaling * TS,
                canvas.getWidth(), canvas.getHeight() - (EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE) * scaling * TS);
        }

        if (editor.gridVisibleProperty().get()) {
            drawGrid(g);
        }

        // Indicate outer areas where obstacles are treated as outer walls
        g.save();
        g.setStroke(Color.grayRgb(200, 0.75));
        g.setLineWidth(0.75);
        g.setLineDashes(5, 5);
        g.strokeLine(0, EMPTY_ROWS_BEFORE_MAZE * scaling * TS, canvas.getWidth(), EMPTY_ROWS_BEFORE_MAZE * scaling * TS);
        g.strokeLine(0, canvas.getHeight() - EMPTY_ROWS_BELOW_MAZE * scaling * TS,
            canvas.getWidth(), canvas.getHeight() - EMPTY_ROWS_BELOW_MAZE * scaling * TS);
        g.restore();

        // Terrain
        if (editor.terrainVisibleProperty().get()) {
            editor.terrainTileRenderer().setScaling(scaling);
            editor.terrainTileRenderer().setColors(colors);
            editor.terrainTileRenderer().setSegmentNumbersDisplayed(editor.segmentNumbersDisplayedProperty().get());
            editor.terrainTileRenderer().setObstacleInnerAreaDisplayed(editor.obstacleInnerAreaDisplayedProperty().get());
            editor.terrainTileRenderer().drawTerrain(g, worldMap(), worldMap().obstacles());
            obstacleEditor.draw(g, editor.terrainTileRenderer());
        }

        // Tiles that seem to be wrong
        for (Vector2i tile : editor.tilesWithErrors()) {
            g.setFont(Font.font("sans", gridSize() - 2));
            g.setFill(Color.grayRgb(200, 0.8));
            g.fillText("?", tile.x() * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            if (editor.isSymmetricEdit()) {
                int x = worldMap().numCols() - tile.x() - 1;
                g.fillText("?", x * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            }
        }

        // Vertical separator to indicate symmetric edit mode
        if (editor.isEditMode(EditMode.EDIT) && editor.isSymmetricEdit()) {
            g.save();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(0.75);
            g.setLineDashes(5, 5);
            g.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
            g.restore();
        }

        // Food
        if (editor.foodVisibleProperty().get()) {
            Color foodColor = getColorFromMap(worldMap(), LayerID.FOOD, PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            editor.foodRenderer().setScaling(scaling);
            editor.foodRenderer().setEnergizerColor(foodColor);
            editor.foodRenderer().setPelletColor(foodColor);
            worldMap().tiles().forEach(tile -> editor.foodRenderer().drawTile(g, tile, worldMap().get(LayerID.FOOD, tile)));
        }

        if (editor.actorsVisibleProperty().get()) {
            editor.drawActorSprites(g, worldMap(), gridSize());
        }

        if (focussedTile() != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(focussedTile().x() * gridSize(), focussedTile().y() * gridSize(), gridSize(), gridSize());
        }
    }

    private void drawGrid(GraphicsContext g) {
        g.setStroke(Color.grayRgb(180));
        g.setLineWidth(0.5);
        for (int row = 1; row < worldMap().numRows(); ++row) {
            g.strokeLine(0, row * gridSize(), canvas.getWidth(), row * gridSize());
        }
        for (int col = 1; col < worldMap().numCols(); ++col) {
            g.strokeLine(col * gridSize(), 0, col * gridSize(), canvas.getHeight());
        }
    }

    private void onMouseClicked(MouseEvent e) {
        Logger.debug("Mouse clicked {}", e);
        if (e.getButton() == MouseButton.PRIMARY) {
            canvas.requestFocus();
            contextMenu.hide();
            if (e.getClickCount() == 2 && editor.isEditMode(EditMode.INSPECT)) {
                editor.showEditHelpText();
            }
        }
    }

    private void onMouseDragged(MouseEvent e) {
        Logger.debug("Mouse dragged {}", e);
        if (!dragging) {
            Vector2i dragStartTile = tileAtMousePosition(e.getX(), e.getY(), gridSize());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.debug("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAtMousePosition(e.getX(), e.getY(), gridSize()));
        }
    }

    private void onMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            if (dragging) {
                dragging = false;
                obstacleEditor.endEditing();
                editor.getChangeManager().setTerrainMapChanged();
                editor.getChangeManager().setEdited(true);
            } else {
                editor.editAtMousePosition(e);
            }
        }
    }

    private void onMouseMoved(MouseEvent e) {
        Vector2i tile = tileAtMousePosition(e.getX(), e.getY(), gridSize());
        focussedTilePy.set(tile);
        switch (editor.editMode()) {
            case EDIT -> {
                if (e.isShiftDown()) {
                    switch (editor.selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> {
                            if (editor.selectedPalette().isToolSelected()) {
                                editor.selectedPalette().selectedTool().apply(worldMap(), LayerID.TERRAIN, focussedTile());
                            }
                            editor.getChangeManager().setEdited(true);
                            editor.getChangeManager().setWorldMapChanged(); // can delete food!
                        }
                        case TileMapEditor.PALETTE_ID_FOOD -> {
                            if (editor.selectedPalette().isToolSelected()) {
                                editor.selectedPalette().selectedTool().apply(worldMap(), LayerID.FOOD, focussedTile());
                            }
                            editor.getChangeManager().setEdited(true);
                            editor.getChangeManager().setFoodMapChanged();
                        }
                        default -> {}
                    }
                }
            }
            case ERASE -> {
                if (e.isShiftDown()) {
                    switch (editor.selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> editor.clearTerrainTileValue(tile);
                        case TileMapEditor.PALETTE_ID_FOOD -> editor.clearFoodTileValue(tile);
                    }
                }
            }
            case INSPECT -> {}
        }
    }


    private void onContextMenuRequested(ContextMenuEvent menuEvent) {
        if (editor.isEditMode(EditMode.INSPECT)) {
            return;
        }
        if (menuEvent.isKeyboardTrigger()) {
            return; // ignore keyboard-triggered event e.g. by pressing Shift+F10 in Windows
        }

        Vector2i tile = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize());

        var miPlaceHouse = new MenuItem(tt("menu.edit.place_house"));
        miPlaceHouse.setOnAction(actionEvent -> editor.placeHouse(worldMap(), tile));

        var miInsertRow = new MenuItem(tt("menu.edit.insert_row"));
        miInsertRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize()).y();
            editor.setEditedWorldMap(worldMap().insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(tt("menu.edit.delete_row"));
        miDeleteRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize()).y();
            editor.setEditedWorldMap(worldMap().deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(tt("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(ae -> editor.floodWithPellets(tile));
        miFloodWithPellets.setDisable(!editor.canPutPelletOnTile(tile));

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miPlaceHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets);

        contextMenu.show(canvas, menuEvent.getScreenX(), menuEvent.getScreenY());
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode key = e.getCode();
        boolean control = e.isControlDown();

        if (control && key == KeyCode.LEFT) {
            editor.moveCursorAndSetFoodAtTile(Direction.LEFT);
            e.consume();
        }
        else if (control && key == KeyCode.RIGHT) {
            editor.moveCursorAndSetFoodAtTile(Direction.RIGHT);
            e.consume();
        }
        else if (control && key == KeyCode.UP) {
            editor.moveCursorAndSetFoodAtTile(Direction.UP);
            e.consume();
        }
        else if (control && key == KeyCode.DOWN) {
            editor.moveCursorAndSetFoodAtTile(Direction.DOWN);
            e.consume();
        }
        else if (key == KeyCode.LEFT) {
            moveCursor(Direction.LEFT, tile -> true);
        }
        else if (key == KeyCode.RIGHT) {
            moveCursor(Direction.RIGHT, tile -> true);
        }
        else if (key == KeyCode.UP) {
            moveCursor(Direction.UP, tile -> true);
        }
        else if (key == KeyCode.DOWN) {
            moveCursor(Direction.DOWN, tile -> true);
        }
        else if (control && key == KeyCode.SPACE) {
            editor.selectNextPaletteEntry();
        }
    }
}
