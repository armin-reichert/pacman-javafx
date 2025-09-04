/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.tilemap.editor.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.uilib.Ufx;
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
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.ACTOR_SPRITES;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.*;
import static java.util.Objects.requireNonNull;

public class EditCanvas extends Canvas {

    static class EditorTerrainTileRenderer extends TerrainMapTileRenderer implements ActorSpriteRenderer {

        public EditorTerrainTileRenderer(Canvas canvas) {
            super(canvas);
        }
    }

    public static final Cursor CURSOR_RUBBER = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    private final ObjectProperty<EditMode> editMode = new SimpleObjectProperty<>();
    private final ObjectProperty<Vector2i> focussedTile = new SimpleObjectProperty<>();
    private final DoubleProperty           gridSize = new SimpleDoubleProperty(8);
    private final DoubleProperty           scaling = new SimpleDoubleProperty(1);
    private final ObjectProperty<Image>    templateImageGray = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty dragging = new SimpleBooleanProperty(false);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty gridVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty obstacleInnerAreaDisplayed = new SimpleBooleanProperty(false);
    private final BooleanProperty obstaclesJoining = new SimpleBooleanProperty(true);
    private final BooleanProperty segmentNumbersVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty symmetricEditMode = new SimpleBooleanProperty(false);
    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);

    private final GraphicsContext ctx;
    private final ObstacleEditor obstacleEditor;
    private final ContextMenu contextMenu = new ContextMenu();

    private final EditorTerrainTileRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;

    private final EditorUI ui;

    public EditCanvas(EditorUI ui) {
        this.ui = requireNonNull(ui);
        ctx = getGraphicsContext2D();

        obstacleEditor = new ObstacleEditor(ui);
        obstacleEditor.joiningProperty().bind(obstaclesJoiningProperty());
        obstacleEditor.symmetricEditModeProperty().bind(symmetricEditModeProperty());
        obstacleEditor.worldMapProperty().bind(worldMapProperty());

        heightProperty().bind(Bindings.createDoubleBinding(
            () -> {
                WorldMap map = worldMap.get();
                int numRows = map != null ? map.numRows() : 31;
                return numRows * gridSize();
            }, worldMap, gridSize));

        widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                WorldMap map = worldMap.get();
                int numCols = map != null ? map.numCols() : 28;
                return numCols * gridSize();
            }, worldMap, gridSize));


        actorsVisibleProperty().bind(ui.actorsVisibleProperty());
        editModeProperty().bind(ui.editModeProperty());
        foodVisibleProperty().bind(ui.foodVisibleProperty());
        gridSizeProperty().bind(ui.gridSizeProperty());
        gridVisibleProperty().bind(ui.gridVisibleProperty());
        obstacleInnerAreaDisplayedProperty().bind(ui.obstacleInnerAreaDisplayedProperty());
        obstaclesJoiningProperty().bind(ui.obstaclesJoiningProperty());
        segmentNumbersVisibleProperty().bind(ui.segmentNumbersVisibleProperty());
        scalingProperty().bind(gridSize.divide(TS));
        symmetricEditModeProperty().bind(ui.symmetricEditModeProperty());
        templateImageGrayProperty().bind(ui.editor().templateImageProperty().map(Ufx::imageToGreyscale));
        terrainVisibleProperty().bind(ui.terrainVisibleProperty());
        worldMapProperty().bind(ui.editor().currentWorldMapProperty());

        terrainRenderer = new EditorTerrainTileRenderer(this);
        terrainRenderer.scalingProperty().bind(scaling);

        foodRenderer = new FoodMapRenderer(this);
        foodRenderer.scalingProperty().bind(scaling);

        setOnContextMenuRequested(this::onContextMenuRequested);
        setOnKeyPressed(this::onKeyPressed);
        setOnMouseClicked(this::onMouseClicked);
        setOnDragDetected(this::onDragDetected);
        setOnMouseDragged(this::onMouseDragged);
        setOnMouseMoved(this::onMouseMoved);
        setOnMouseReleased(this::onMouseReleased);
    }

    // -- Properties

    public BooleanProperty actorsVisibleProperty() {
        return actorsVisible;
    }

    public BooleanProperty draggingProperty() {
        return dragging;
    }

    public boolean dragging() {
        return dragging.get();
    }

    private void setDragging(boolean b) {
        dragging.set(b);
    }

    public ObjectProperty<EditMode> editModeProperty() {
        return editMode;
    }

    public ObjectProperty<Vector2i> focussedTileProperty() {
        return focussedTile;
    }

    public Vector2i focussedTile() {
        return focussedTile.get();
    }

    public BooleanProperty foodVisibleProperty() {
        return foodVisible;
    }

    public DoubleProperty gridSizeProperty() {
        return gridSize;
    }

    public double gridSize() {
        return gridSize.get();
    }

    public BooleanProperty gridVisibleProperty() {
        return gridVisible;
    }

    public BooleanProperty obstacleInnerAreaDisplayedProperty() {
        return obstacleInnerAreaDisplayed;
    }

    public BooleanProperty obstaclesJoiningProperty() {
        return obstaclesJoining;
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public double scaling() {
        return scaling.get();
    }

    public BooleanProperty segmentNumbersVisibleProperty() {
        return segmentNumbersVisible;
    }

    public BooleanProperty symmetricEditModeProperty() {
        return symmetricEditMode;
    }

    public ObjectProperty<Image> templateImageGrayProperty() {
        return templateImageGray;
    }

    public BooleanProperty terrainVisibleProperty() {
        return terrainVisible;
    }

    public ObjectProperty<WorldMap> worldMapProperty() {
        return worldMap;
    }

    public WorldMap worldMap() { return worldMap.get(); }

    public TerrainMapTileRenderer terrainRenderer() {
        return terrainRenderer;
    }

    public FoodMapRenderer foodRenderer() {
        return foodRenderer;
    }

    public void moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().outOfWorld(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTile.set(nextTile);
            }
        }
    }

    public Vector2i tileAt(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX, gridSize()), fullTiles(mouseY, gridSize()));
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
        double width = getWidth(), height = getHeight();
        ctx.setImageSmoothing(false);

        ctx.setFill(colorScheme.floorColor());
        ctx.fillRect(0, 0, width, height);

        if (templateImageGray.get() != null) {
            ctx.drawImage(templateImageGray.get(),
                0, GameLevel.EMPTY_ROWS_OVER_MAZE * scaling() * TS,
                width, height - (GameLevel.EMPTY_ROWS_OVER_MAZE + GameLevel.EMPTY_ROWS_BELOW_MAZE) * scaling() * TS);
        }

        if (gridVisibleProperty().get()) {
            drawGrid();
        }

        // Indicate start and end of reserved areas at top and bottom
        ctx.save();
        ctx.setStroke(Color.grayRgb(200, 0.75));
        ctx.setLineWidth(0.75);
        ctx.setLineDashes(5, 5);
        ctx.strokeLine(0, GameLevel.EMPTY_ROWS_OVER_MAZE * scaling() * TS, width, GameLevel.EMPTY_ROWS_OVER_MAZE * scaling() * TS);
        ctx.strokeLine(0, height - GameLevel.EMPTY_ROWS_BELOW_MAZE * scaling() * TS, width, height - GameLevel.EMPTY_ROWS_BELOW_MAZE * scaling() * TS);
        ctx.restore();

        // Terrain
        if (terrainVisible.get()) {
            terrainRenderer.setColorScheme(colorScheme);
            terrainRenderer.setSegmentNumbersDisplayed(segmentNumbersVisible.get());
            terrainRenderer.setObstacleInnerAreaDisplayed(obstacleInnerAreaDisplayed.get());
            terrainRenderer.draw(worldMap(), worldMap().obstacles());
            obstacleEditor.draw(terrainRenderer);
        }

        // Tiles that seem to be wrong
        ctx.setFont(Font.font("sans", gridSize() - 2));
        ctx.setFill(Color.grayRgb(200, 0.8));
        for (Vector2i tile : ui.editor().checkResult().tilesWithErrors()) {
            ctx.fillText("?", tile.x() * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            if (symmetricEditMode.get()) {
                int x = worldMap().numCols() - tile.x() - 1;
                ctx.fillText("?", x * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            }
        }

        // Vertical separator to indicate symmetric edit mode
        if (editMode.get() == EditMode.EDIT && symmetricEditMode.get()) {
            ctx.save();
            ctx.setStroke(Color.YELLOW);
            ctx.setLineWidth(0.75);
            ctx.setLineDashes(5, 5);
            ctx.strokeLine(width / 2.0, 0, width / 2.0, height);
            ctx.restore();
        }

        // Food
        if (foodVisible.get()) {
            Color foodColor = getColorFromMap(worldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(ArcadeSprites.MS_PACMAN_COLOR_FOOD));
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            worldMap().tiles().forEach(tile -> foodRenderer.drawTile(tile, worldMap().content(LayerID.FOOD, tile)));
        }

        if (actorsVisible.get()) {
            ACTOR_SPRITES.forEach((positionProperty, sprite) -> {
                Vector2i tile = worldMap().getTerrainTileProperty(positionProperty);
                if (tile != null) {
                    terrainRenderer.drawActorSprite(tile, sprite);
                }
            });
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

    // Event handlers

    public void onKeyPressed(KeyEvent keyEvent) {
        boolean control = keyEvent.isControlDown();
        switch (keyEvent.getCode()) {
            case LEFT  -> moveCursor(Direction.LEFT, tile -> true);
            case RIGHT -> moveCursor(Direction.RIGHT, tile -> true);
            case UP    -> moveCursor(Direction.UP, tile -> true);
            case DOWN  -> moveCursor(Direction.DOWN, tile -> true);
            case SPACE -> {
                if (control) {
                    new Action_SelectNextPaletteEntry(ui).execute();
                }
            }
        }
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            Logger.info("Mouse clicked {}", mouseEvent);
            requestFocus();
            contextMenu.hide();
            mouseEvent.consume();
        }
    }

    private void onDragDetected(MouseEvent mouseEvent) {
        Logger.info("onDragDetected");
        if (editMode.get() == EditMode.EDIT) {
            Vector2i tileAtMouse = tileAt(mouseEvent.getX(), mouseEvent.getY());
            setDragging(true);
            obstacleEditor.startEditing(tileAtMouse);
            Logger.info("Start editing obstacle");
            mouseEvent.consume();
        }
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (!dragging()) {
            return;
        }
        Logger.info("onMouseDragged");
        Vector2i tileAtMouse = tileAt(mouseEvent.getX(), mouseEvent.getY());
        obstacleEditor.continueEditing(tileAtMouse);
        mouseEvent.consume();
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        Logger.info("onMouseReleased");
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        if (dragging()) {
            setDragging(false);
            obstacleEditor.endEditing();
            mouseEvent.consume();
            Logger.info("End editing of obstacle");
        } else {
            Vector2i tile = tileAt(mouseEvent.getX(), mouseEvent.getY());
            if (!ui.editModeIs(EditMode.INSPECT)) {
                if (mouseEvent.isControlDown()) {
                    ui.selectedPaletteID().ifPresent(paletteID -> {
                        switch (paletteID) {
                            case TERRAIN -> {
                                mouseEvent.consume();
                                new Action_ClearTerrainTile(ui.editor(), tile).execute();
                            }
                            case FOOD -> {
                                mouseEvent.consume();
                                new Action_ClearFoodTile(ui.editor(), tile).execute();
                            }
                        }
                    });
                } else {
                    ui.selectedPalette().ifPresent(palette -> {
                        mouseEvent.consume();
                        new Action_ApplySelectedPaletteTool(ui, palette, tile).execute();
                    });
                }
            }
        }
    }

    public void onMouseMoved(MouseEvent mouseEvent) {
        Vector2i tile = tileAt(mouseEvent.getX(), mouseEvent.getY());
        focussedTile.set(tile);
        switch (editMode.get()) {
            case INSPECT -> new Action_IdentifyObstacle(ui, tile).execute();
            case EDIT -> {
                if (mouseEvent.isShiftDown()) {
                    ui.selectedPalette().ifPresent(palette -> {
                        switch (palette.id()) {
                            case TERRAIN, FOOD -> palette.selectedTool()
                                .ifPresent(selectedTool -> selectedTool.editor().accept(focussedTile()));
                        }
                    });
                }
            }
            case ERASE -> {
                if (mouseEvent.isShiftDown()) {
                    ui.selectedPalette().ifPresent(palette -> {
                        switch (palette.id()) {
                            case TERRAIN -> new Action_ClearTerrainTile(ui.editor(), tile).execute();
                            case FOOD -> new Action_ClearFoodTile(ui.editor(), tile).execute();
                        }
                    });
                }
            }
        }
        mouseEvent.consume();
    }

    public void onContextMenuRequested(ContextMenuEvent menuEvent) {
        final TileMapEditor editor = ui.editor();

        if (ui.editModeIs(EditMode.INSPECT)) {
            return;
        }
        if (menuEvent.isKeyboardTrigger()) {
            return;
        }

        Vector2i tile = tileAt(menuEvent.getX(), menuEvent.getY());

        var miPlaceHouse = new MenuItem(translated("menu.edit.place_house"));
        miPlaceHouse.setOnAction(actionEvent -> new Action_PlaceArcadeHouse(editor, worldMap(), tile).execute());

        var miClearFoodAroundHouse = new MenuItem(translated("menu.edit.clear_food_around_house"));
        miClearFoodAroundHouse.setOnAction(actionEvent -> new Action_ClearFoodAroundHouse(editor, worldMap()).execute());

        var miInsertRow = new MenuItem(translated("menu.edit.insert_row"));
        miInsertRow.setOnAction(actionEvent -> {
            int rowIndex = tileAt(menuEvent.getX(), menuEvent.getY()).y();
            editor.setCurrentWorldMap(worldMap().insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(translated("menu.edit.delete_row"));
        miDeleteRow.setOnAction(actionEvent -> {
            int rowIndex = tileAt(menuEvent.getX(), menuEvent.getY()).y();
            editor.setCurrentWorldMap(worldMap().deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(translated("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(actionEvent -> new Action_FloodWithPellets(ui, tile).execute());
        miFloodWithPellets.setDisable(!canPlaceFoodAtTile(worldMap(), tile));

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miPlaceHouse,
            miClearFoodAroundHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets);

        contextMenu.show(this, menuEvent.getScreenX(), menuEvent.getScreenY());
    }
}
