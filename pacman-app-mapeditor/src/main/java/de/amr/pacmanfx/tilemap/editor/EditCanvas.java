/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.TerrainMapColorScheme;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
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

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.ArcadeMap.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static java.util.Objects.requireNonNull;

public class EditCanvas extends Canvas {

    public static final Cursor CURSOR_RUBBER = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    private final ObjectProperty<Vector2i> focussedTile = new SimpleObjectProperty<>();
    private final IntegerProperty gridSize = new SimpleIntegerProperty(8);
    private final ObjectProperty<Image> templateImageGray = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty gridVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty segmentNumbersVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);

    private final GraphicsContext ctx;
    private final TileMapEditor editor;
    private final ObstacleEditor obstacleEditor;
    private final ContextMenu contextMenu = new ContextMenu();

    private final TerrainTileMapRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final EditorActorRenderer actorRenderer;

    private boolean dragging = false;

    public EditCanvas(TileMapEditor editor, ObstacleEditor obstacleEditor) {
        this.editor = requireNonNull(editor);
        this.obstacleEditor = requireNonNull(obstacleEditor);

        ctx = getGraphicsContext2D();

        heightProperty().bind(Bindings.createDoubleBinding(
            () -> {
                WorldMap map = worldMap.get();
                int numRows = map != null ? map.numRows() : 31;
                return Double.valueOf(numRows * gridSize());
            }, worldMap, gridSize));

        widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                WorldMap map = worldMap.get();
                int numCols = map != null ? map.numCols() : 28;
                return Double.valueOf(numCols * gridSize());
            }, worldMap, gridSize));

        terrainRenderer = new TerrainTileMapRenderer(this);
        foodRenderer = new FoodMapRenderer(this);
        actorRenderer = new EditorActorRenderer(this);

        setOnContextMenuRequested(this::onContextMenuRequested);
        setOnMouseClicked(this::onMouseClicked);
        setOnMouseDragged(this::onMouseDragged);
        setOnMouseMoved(this::onMouseMoved);
        setOnMouseReleased(this::onMouseReleased);
        setOnKeyPressed(this::onKeyPressed);
    }

    public IntegerProperty gridSizeProperty() {
        return gridSize;
    }

    public int gridSize() { return gridSize.get(); }

    public ObjectProperty<Image> templateImageGrayProperty() {
        return templateImageGray;
    }

    public ObjectProperty<WorldMap> worldMapProperty() {
        return worldMap;
    }

    public WorldMap worldMap() { return worldMap.get(); }

    public BooleanProperty actorsVisibleProperty() {
        return actorsVisible;
    }

    public BooleanProperty foodVisibleProperty() {
        return foodVisible;
    }

    public BooleanProperty gridVisibleProperty() {
        return gridVisible;
    }

    public BooleanProperty segmentNumbersVisibleProperty() {
        return segmentNumbersVisible;
    }

    public BooleanProperty terrainVisibleProperty() {
        return terrainVisible;
    }

    public TerrainTileMapRenderer terrainRenderer() {
        return terrainRenderer;
    }

    public FoodMapRenderer foodRenderer() {
        return foodRenderer;
    }

    public ObjectProperty<Vector2i> focussedTileProperty() { return focussedTile; }

    public Vector2i focussedTile() { return focussedTile.get(); }

    public boolean moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().outOfWorld(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTile.set(nextTile);
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

    public void draw(TerrainMapColorScheme colorScheme) {
        double scaling = gridSize() / (double) TS;
        double width = getWidth(), height = getHeight();
        ctx.setImageSmoothing(false);

        ctx.setFill(colorScheme.floorColor());
        ctx.fillRect(0, 0, width, height);

        if (templateImageGray.get() != null) {
            ctx.drawImage(templateImageGray.get(),
                0, EMPTY_ROWS_BEFORE_MAZE * scaling * TS,
                width, height - (EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE) * scaling * TS);
        }

        if (gridVisibleProperty().get()) {
            drawGrid();
        }

        // Indicate start and end of reserved areas at top and bottom
        ctx.save();
        ctx.setStroke(Color.grayRgb(200, 0.75));
        ctx.setLineWidth(0.75);
        ctx.setLineDashes(5, 5);
        ctx.strokeLine(0, EMPTY_ROWS_BEFORE_MAZE * scaling * TS, width, EMPTY_ROWS_BEFORE_MAZE * scaling * TS);
        ctx.strokeLine(0, height - EMPTY_ROWS_BELOW_MAZE * scaling * TS, width, height - EMPTY_ROWS_BELOW_MAZE * scaling * TS);
        ctx.restore();

        // Terrain
        if (terrainVisible.get()) {
            terrainRenderer.setScaling(scaling);
            terrainRenderer.setColorScheme(colorScheme);
            terrainRenderer.setSegmentNumbersDisplayed(editor.isSegmentNumbersVisible());
            terrainRenderer.setObstacleInnerAreaDisplayed(editor.isObstacleInnerAreaDisplayed());
            terrainRenderer.draw(worldMap(), worldMap().obstacles());
            obstacleEditor.draw(terrainRenderer);
        }

        // Tiles that seem to be wrong
        ctx.setFont(Font.font("sans", gridSize() - 2));
        ctx.setFill(Color.grayRgb(200, 0.8));
        for (Vector2i tile : editor.tilesWithErrors()) {
            ctx.fillText("?", tile.x() * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            if (editor.isSymmetricEditMode()) {
                int x = worldMap().numCols() - tile.x() - 1;
                ctx.fillText("?", x * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            }
        }

        // Vertical separator to indicate symmetric edit mode
        if (editor.isEditMode(EditMode.EDIT) && editor.isSymmetricEditMode()) {
            ctx.save();
            ctx.setStroke(Color.YELLOW);
            ctx.setLineWidth(0.75);
            ctx.setLineDashes(5, 5);
            ctx.strokeLine(width / 2.0, 0, width / 2.0, height);
            ctx.restore();
        }

        // Food
        if (foodVisible.get()) {
            Color foodColor = getColorFromMap(worldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodRenderer.setScaling(scaling);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            worldMap().tiles().forEach(tile -> foodRenderer.drawTile(tile, worldMap().content(LayerID.FOOD, tile)));
        }

        if (actorsVisible.get()) {
            double gridSize = editor.gridSizeProperty().get();
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_PAC), PAC_SPRITE, gridSize);
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_RED_GHOST), RED_GHOST_SPRITE, gridSize);
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_PINK_GHOST), PINK_GHOST_SPRITE, gridSize);
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_CYAN_GHOST), CYAN_GHOST_SPRITE, gridSize);
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_ORANGE_GHOST), ORANGE_GHOST_SPRITE, gridSize);
            actorRenderer.drawActorSprite(worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS), BONUS_SPRITE, gridSize);
        }

        if (focussedTile() != null) {
            ctx.save();
            ctx.setLineWidth(1);
            ctx.setStroke(Color.YELLOW);
            ctx.strokeRect(focussedTile().x() * gridSize(), focussedTile().y() * gridSize(), gridSize(), gridSize());
            ctx.restore();
        }
    }

    private void drawGrid() {
        ctx.save();
        ctx.setLineWidth(0.5);
        ctx.setStroke(Color.grayRgb(180));
        for (int row = 1; row < worldMap().numRows(); ++row) {
            ctx.strokeLine(0, row * gridSize(), getWidth(), row * gridSize());
        }
        for (int col = 1; col < worldMap().numCols(); ++col) {
            ctx.strokeLine(col * gridSize(), 0, col * gridSize(), getHeight());
        }
        ctx.restore();
    }

    private void onMouseClicked(MouseEvent e) {
        Logger.debug("Mouse clicked {}", e);
        if (e.getButton() == MouseButton.PRIMARY) {
            requestFocus();
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
        focussedTile.set(tile);
        switch (editor.editMode()) {
            case EDIT -> {
                if (e.isShiftDown()) {
                    switch (editor.selectedPaletteID()) {
                        case TileMapEditor.PALETTE_ID_TERRAIN -> {
                            if (editor.selectedPalette().isToolSelected()) {
                                editor.selectedPalette().selectedTool().apply(worldMap(), LayerID.TERRAIN, focussedTile());
                            }
                            editor.getChangeManager().setEdited(true);
                            editor.getChangeManager().setWorldMapChanged();
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
            return;
        }

        Vector2i tile = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize());

        var miPlaceHouse = new MenuItem(translated("menu.edit.place_house"));
        miPlaceHouse.setOnAction(actionEvent -> editor.placeArcadeHouse(worldMap(), tile));

        var miInsertRow = new MenuItem(translated("menu.edit.insert_row"));
        miInsertRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize()).y();
            editor.setEditedWorldMap(worldMap().insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(translated("menu.edit.delete_row"));
        miDeleteRow.setOnAction(actionEvent -> {
            int rowIndex = tileAtMousePosition(menuEvent.getX(), menuEvent.getY(), gridSize()).y();
            editor.setEditedWorldMap(worldMap().deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(translated("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(ae -> editor.floodWithFoodValue(tile, FoodTile.PELLET.code()));
        miFloodWithPellets.setDisable(!editor.canEditFoodAtTile(tile));

        var miClearPellets = new MenuItem(translated("menu.edit.clear_food"));
        miClearPellets.setOnAction(ae -> editor.floodWithFoodValue(tile, FoodTile.EMPTY.code()));
        miClearPellets.setDisable(!editor.canEditFoodAtTile(tile));

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miPlaceHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets,
            miClearPellets);

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
