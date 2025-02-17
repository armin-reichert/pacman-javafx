/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.FoodTiles;
import de.amr.games.pacman.lib.tilemap.TerrainTiles;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.rendering.TerrainColorScheme;
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
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.ArcadeMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditor.tt;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.*;

public class EditCanvas extends Canvas {

    public static final Cursor CURSOR_RUBBER = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    private final TileMapEditor editor;

    private final ObjectProperty<Vector2i> focussedTilePy = new SimpleObjectProperty<>();
    private final IntegerProperty gridSizePy = new SimpleIntegerProperty(8);
    private final ObjectProperty<Image> templateImageGreyPy = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private final ObstacleEditor obstacleEditor;
    private final ContextMenu contextMenu = new ContextMenu();

    private boolean dragging = false;

    public EditCanvas(TileMapEditor editor) {
        this.editor = editor;

        obstacleEditor = new ObstacleEditor();
        obstacleEditor.setEditCallback((tile, value) -> {
            editor.setTileValue(editor.worldMap().terrain(), tile, value);
            editor.setTileValue(editor.worldMap().food(), tile, FoodTiles.EMPTY);
        });
        obstacleEditor.worldMapProperty().bind(worldMapPy);

        gridSizePy.bind(editor.gridSizeProperty());
        worldMapPy.bind(editor.worldMapProperty());
        templateImageGreyPy.bind(editor.templateImageGreyProperty());

        heightProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numRows() * gridSize(),
            worldMapPy, gridSizePy));

        widthProperty().bind(Bindings.createDoubleBinding(
            () -> (double) worldMap().terrain().numCols() * gridSize(),
            worldMapPy, gridSizePy));

        setOnContextMenuRequested(this::onContextMenuRequested);
        setOnMouseClicked(this::onMouseClicked);
        setOnMouseDragged(this::onMouseDragged);
        setOnMouseMoved(this::onMouseMoved);
        setOnMouseReleased(this::onMouseReleased);
        setOnKeyPressed(this::onKeyPressed);

    }

    private int gridSize() { return gridSizePy.get(); }

    private WorldMap worldMap() { return worldMapPy.get(); }

    public ObjectProperty<Vector2i> focussedTileProperty() { return focussedTilePy; }

    public Vector2i focussedTile() { return focussedTilePy.get(); }

    public boolean moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().terrain().outOfBounds(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTilePy.set(nextTile);
                return true;
            }
        }
        return false;
    }

    public void enterInspectMode() {
        setCursor(Cursor.HAND); // TODO use other cursor
        obstacleEditor.setEnabled(false);
    }

    public void enterEditMode() {
        setCursor(Cursor.DEFAULT);
        obstacleEditor.setEnabled(true);
    }

    public void enterEraseMode() {
        setCursor(CURSOR_RUBBER);
        obstacleEditor.setEnabled(false);
    }

    public void draw(TerrainColorScheme colors) {
        GraphicsContext g = getGraphicsContext2D();

        final TileMap terrain = worldMap().terrain(), food = worldMap().food();
        double scaling = gridSize() / (double) TS;

        g.setImageSmoothing(false);

        // Background
        g.setFill(colors.backgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());

        if (templateImageGreyPy.get() != null) {
            int emptyRowsTop = 3, emptyRowsBottom = 2; // TODO
            g.drawImage(templateImageGreyPy.get(),
                0, emptyRowsTop * scaling * TS,
                getWidth(), getHeight() - (emptyRowsTop + emptyRowsBottom) * scaling * TS);
        }

        if (editor.gridVisibleProperty().get()) {
            drawGrid(g);
        }

        // Terrain
        if (editor.terrainVisibleProperty().get()) {
            editor.terrainRendererInEditor().setScaling(scaling);
            editor.terrainRendererInEditor().setColors(colors);
            editor.terrainRendererInEditor().setSegmentNumbersDisplayed(editor.segmentNumbersDisplayedProperty().get());
            editor.terrainRendererInEditor().setObstacleInnerAreaDisplayed(editor.obstacleInnerAreaDisplayedProperty().get());
            editor.terrainRendererInEditor().drawTerrain(g, terrain, worldMap().obstacles());

            byte[][] editedObstacleContent = obstacleEditor.editedContent();
            if (editedObstacleContent != null) {
                for (int row = 0; row < editedObstacleContent.length; ++row) {
                    for (int col = 0; col < editedObstacleContent[0].length; ++col) {
                        Vector2i tile = obstacleEditor.minTile().plus(col, row);
                        editor.terrainRendererInEditor().drawTile(g, tile, editedObstacleContent[row][col]);
                    }
                }
            }
        }

        // Tiles that seem to be wrong
        for (Vector2i tile : editor.tilesWithErrors()) {
            g.setFont(Font.font("sans", gridSize() - 2));
            g.setFill(Color.grayRgb(200, 0.8));
            g.fillText("?", tile.x() * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            if (editor.isSymmetricEditMode()) {
                int x = terrain.numCols() - tile.x() - 1;
                g.fillText("?", x * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            }
        }

        // Vertical separator to indicate symmetric edit mode
        if (editor.isEditMode(EditMode.EDIT) && editor.isSymmetricEditMode()) {
            g.save();
            g.setStroke(Color.YELLOW);
            g.setLineWidth(0.75);
            g.setLineDashes(5, 5);
            g.strokeLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            g.restore();
        }

        // Food
        if (editor.foodVisibleProperty().get()) {
            Color foodColor = getColorFromMap(food, PROPERTY_COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            editor.foodRenderer().setScaling(scaling);
            editor.foodRenderer().setEnergizerColor(foodColor);
            editor.foodRenderer().setPelletColor(foodColor);
            editor.foodRenderer().drawFood(g, food);
        }

        drawActorSprites(g);

        if (focussedTile() != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(focussedTile().x() * gridSize(), focussedTile().y() * gridSize(), gridSize(), gridSize());
        }
    }

    private void drawGrid(GraphicsContext g) {
        g.save();
        g.setStroke(Color.LIGHTGRAY);
        g.setLineWidth(0.25);
        for (int row = 1; row < worldMap().terrain().numRows(); ++row) {
            g.strokeLine(0, row * gridSize(), getWidth(), row * gridSize());
        }
        for (int col = 1; col < worldMap().terrain().numCols(); ++col) {
            g.strokeLine(col * gridSize(), 0, col * gridSize(), getHeight());
        }
        g.restore();
    }

    //TODO move into renderer class
    public void drawActorSprites(GraphicsContext g) {
        drawSprite(g, PROPERTY_POS_PAC, PAC_SPRITE);
        drawSprite(g, PROPERTY_POS_RED_GHOST, RED_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_PINK_GHOST, PINK_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_CYAN_GHOST, CYAN_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_ORANGE_GHOST, ORANGE_GHOST_SPRITE);
        drawSprite(g, PROPERTY_POS_BONUS, BONUS_SPRITE);
    }

    private void drawSprite(GraphicsContext g, String tilePropertyName, RectArea sprite) {
        Vector2i tile = worldMap().terrain().getTileProperty(tilePropertyName, null);
        if (tile != null) {
            drawSprite(g, sprite,
                tile.x() * gridSize() + 0.5 * gridSize(),
                tile.y() * gridSize(), 1.75 * gridSize(), 1.75 * gridSize());
        }
    }

    private void drawSprite(GraphicsContext g, RectArea sprite, double x, double y, double w, double h) {
        double ox = 0.5 * (w - gridSize());
        double oy = 0.5 * (h - gridSize());
        g.drawImage(SPRITE_SHEET, sprite.x(), sprite.y(), sprite.width(), sprite.height(), x - ox, y - oy, w, h);
    }

    private void onMouseClicked(MouseEvent e) {
        Logger.debug("Mouse clicked {}", e);
        if (e.getButton() == MouseButton.PRIMARY) {
            requestFocus();
            contextMenu.hide();
            if (e.getClickCount() == 2 && editor.isEditMode(EditMode.INSPECT)) {
                editor.setEditMode(EditMode.EDIT);
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
                obstacleEditor.endEditing(tileAtMousePosition(e.getX(), e.getY(), gridSize()));
            } else {
                editor.editAtMousePosition(e);
            }
        }
    }

    private void onMouseMoved(MouseEvent e) {
        Vector2i tile = tileAtMousePosition(e.getX(), e.getY(), gridSize());
        focussedTilePy.set(tile);
        switch (editor.editMode()) {
            case EditMode.EDIT -> {
                if (e.isShiftDown()) {
                    switch (editor.selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> {
                            if (editor.selectedPalette().isToolSelected()) {
                                editor.selectedPalette().selectedTool().apply(worldMap().terrain(), focussedTile());
                            }
                            editor.getChangeManager().markTerrainChanged();
                        }
                        case TileMapEditor.PALETTE_ID_FOOD -> {
                            if (editor.selectedPalette().isToolSelected()) {
                                editor.selectedPalette().selectedTool().apply(worldMap().food(), focussedTile());
                            }
                            editor.getChangeManager().markFoodChanged();
                        }
                        default -> {}
                    }
                }
            }
            case EditMode.ERASE -> {
                if (e.isShiftDown()) {
                    switch (editor.selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> editor.clearTerrainTileValue(tile);
                        case TileMapEditor.PALETTE_ID_FOOD -> editor.clearFoodTileValue(tile);
                    }
                }
            }
            case EditMode.INSPECT -> {}
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
            editor.setWorldMap(worldMap().insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(tt("menu.edit.delete_row"));
        miDeleteRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize()).y();
            editor.setWorldMap(worldMap().deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(tt("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(ae -> {
            if (worldMap().terrain().get(tile) == TerrainTiles.EMPTY && worldMap().food().get(tile) == TerrainTiles.EMPTY) {
                editor.floodWithPellets(tile);
            }
        });

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miPlaceHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets);

        contextMenu.show(this, menuEvent.getScreenX(), menuEvent.getScreenY());
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
