/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.actions.*;
import de.amr.pacmanfx.mapeditor.palette.PaletteID;
import de.amr.pacmanfx.mapeditor.rendering.ActorSpriteRenderer;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import de.amr.pacmanfx.mapeditor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.model.world.FoodLayer;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.UfxImages;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.FoodMapRenderer;
import de.amr.pacmanfx.uilib.rendering.TerrainMapColorScheme;
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
import static de.amr.pacmanfx.mapeditor.EditorGlobals.ACTOR_SPRITES;
import static de.amr.pacmanfx.mapeditor.EditorGlobals.translated;
import static de.amr.pacmanfx.mapeditor.EditorUtil.getColorFromMapLayer;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.COLOR_FOOD;
import static java.util.Objects.requireNonNull;

public class EditCanvas extends Canvas {

    static class EditRenderer extends TerrainMapTileRenderer implements ActorSpriteRenderer {
        public EditRenderer(Canvas canvas) {
            super(canvas);
        }
    }

    private static final ResourceManager RESOURCE_MANAGER = () -> EditCanvas.class;

    public static final Cursor CURSOR_RUBBER = RESOURCE_MANAGER.cursor("/de/amr/pacmanfx/mapeditor/graphics/radiergummi.jpg");

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

    private final EditRenderer renderer;
    private final FoodMapRenderer foodRenderer;

    private final TileMapEditorUI ui;

    public EditCanvas(TileMapEditorUI ui) {
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


        actorsVisibleProperty()             .bind(ui.actorsVisibleProperty());
        editModeProperty()                  .bind(ui.editModeProperty());
        foodVisibleProperty()               .bind(ui.foodVisibleProperty());
        gridSizeProperty()                  .bind(ui.gridSizeProperty());
        gridVisibleProperty()               .bind(ui.gridVisibleProperty());
        obstacleInnerAreaDisplayedProperty().bind(ui.obstacleInnerAreaDisplayedProperty());
        obstaclesJoiningProperty()          .bind(ui.obstaclesJoiningProperty());
        segmentNumbersVisibleProperty()     .bind(ui.segmentNumbersVisibleProperty());
        scalingProperty()                   .bind(gridSize.divide(TS));
        symmetricEditModeProperty()         .bind(ui.editor().symmetricEditModeProperty());
        templateImageGrayProperty()         .bind(ui.editor().templateImageProperty().map(UfxImages::imageToGreyscale));
        terrainVisibleProperty()            .bind(ui.terrainVisibleProperty());
        worldMapProperty()                  .bind(ui.editor().currentWorldMapProperty());

        renderer = new EditRenderer(this);
        renderer.scalingProperty().bind(scaling);

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
        return renderer;
    }

    public FoodMapRenderer foodRenderer() {
        return foodRenderer;
    }

    public void moveCursor(Direction dir, Predicate<Vector2i> canMoveIntoTile) {
        if (focussedTile() != null) {
            Vector2i nextTile = focussedTile().plus(dir.vector());
            if (!worldMap().terrainLayer().outOfBounds(nextTile) && canMoveIntoTile.test(nextTile)) {
                focussedTile.set(nextTile);
            }
        }
    }

    public Vector2i tileAt(double mouseX, double mouseY) {
        return new Vector2i(EditorUtil.fullTiles(mouseX, gridSize()), EditorUtil.fullTiles(mouseY, gridSize()));
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
        final TerrainLayer terrain = worldMap().terrainLayer();
        final double scaledTileSize = scaling() * TS;

        ctx.setImageSmoothing(false);

        ctx.setFill(colorScheme.floorColor());
        ctx.fillRect(0, 0, width, height);

        if (isFocused()) {
            ctx.setStroke(Color.YELLOW);
            ctx.setLineWidth(2);
            ctx.setLineDashes(2, 10);
            ctx.strokeRect(0, 0, width, height);
            ctx.setLineDashes();
        }

        if (templateImageGray.get() != null) {
            ctx.drawImage(templateImageGray.get(),
                0,
                terrain.emptyRowsOverMaze() * scaledTileSize,
                width,
                height - (terrain.emptyRowsOverMaze() + terrain.emptyRowsBelowMaze()) * scaledTileSize);
        }

        if (gridVisibleProperty().get()) {
            drawGrid();
        }

        // Indicate start and end of reserved areas at top and bottom
        ctx.save();
        ctx.setStroke(Color.grayRgb(200, 0.75));
        ctx.setLineWidth(0.75);
        ctx.setLineDashes(5, 5);
        ctx.strokeLine(0, terrain.emptyRowsOverMaze() * scaledTileSize, width, terrain.emptyRowsOverMaze() * scaledTileSize);
        ctx.strokeLine(0, height - terrain.emptyRowsBelowMaze() * scaledTileSize, width, height - terrain.emptyRowsBelowMaze() * scaledTileSize);
        ctx.restore();

        // Terrain
        if (terrainVisible.get()) {
            renderer.setColorScheme(colorScheme);
            renderer.setSegmentNumbersDisplayed(segmentNumbersVisible.get());
            renderer.setObstacleInnerAreaDisplayed(obstacleInnerAreaDisplayed.get());
            renderer.draw(worldMap());
            obstacleEditor.draw(renderer);
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
            FoodLayer foodLayer = worldMap().foodLayer();
            Color foodColor = getColorFromMapLayer(foodLayer, COLOR_FOOD, ArcadeSprites.MS_PACMAN_COLOR_FOOD);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            foodLayer.tiles().forEach(tile -> foodRenderer.drawTile(tile, foodLayer.content(tile)));
        }

        if (actorsVisible.get()) {
            ACTOR_SPRITES.forEach((positionProperty, sprite) -> {
                Vector2i tile = terrain.getTileProperty(positionProperty);
                if (tile != null) {
                    renderer.drawActorSprite(tile, sprite);
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
            case LEFT  -> moveCursor(Direction.LEFT, _ -> true);
            case RIGHT -> moveCursor(Direction.RIGHT, _ -> true);
            case UP    -> moveCursor(Direction.UP, _ -> true);
            case DOWN  -> moveCursor(Direction.DOWN, _ -> true);
            case SPACE -> {
                if (control) {
                    new Action_SelectNextPaletteEntry(ui).execute();
                }
            }
        }
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY)
            return;

        Logger.info("Mouse clicked {}", mouseEvent);
        requestFocus();
        contextMenu.hide();
        mouseEvent.consume();
    }

    private void onDragDetected(MouseEvent mouseEvent) {
        if (editMode.get() != EditMode.EDIT)
            return;

        Logger.info("onDragDetected");
        Vector2i tileAtMouse = tileAt(mouseEvent.getX(), mouseEvent.getY());
        setDragging(true);
        obstacleEditor.startEditing(tileAtMouse);
        Logger.info("Start editing obstacle");
        mouseEvent.consume();
    }

    private void onMouseDragged(MouseEvent mouseEvent) {
        if (!dragging()) {
            return;
        }
        Logger.debug("onMouseDragged");
        Vector2i tileAtMouse = tileAt(mouseEvent.getX(), mouseEvent.getY());
        obstacleEditor.continueEditing(tileAtMouse);
        mouseEvent.consume();
    }

    public void onMouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY)
            return;

        Logger.info("onMouseReleased");
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
                            case PaletteID.TERRAIN -> {
                                mouseEvent.consume();
                                new Action_ClearTerrainTile(ui.editor(), tile).execute();
                            }
                            case PaletteID.FOOD -> {
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
            case INSPECT -> new Action_IdentifyTileAndObstacle(ui, tile).execute();
            case EDIT -> {
                if (mouseEvent.isShiftDown()) {
                    ui.selectedPalette().ifPresent(palette -> {
                        switch (palette.id()) {
                            case PaletteID.TERRAIN, PaletteID.FOOD -> palette.selectedTool()
                                .ifPresent(selectedTool -> selectedTool.editor().accept(focussedTile()));
                        }
                    });
                }
            }
            case ERASE -> {
                if (mouseEvent.isShiftDown()) {
                    ui.selectedPalette().ifPresent(palette -> {
                        switch (palette.id()) {
                            case PaletteID.TERRAIN -> new Action_ClearTerrainTile(ui.editor(), tile).execute();
                            case PaletteID.FOOD -> new Action_ClearFoodTile(ui.editor(), tile).execute();
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

        var miCarveTunnel = new MenuItem(translated("menu.edit.carve_tunnel"));
        miCarveTunnel.setOnAction(_ -> new Action_CarveTunnel(editor, tile).execute());

        var miPlaceHouse = new MenuItem(translated("menu.edit.place_house"));
        miPlaceHouse.setOnAction(_ -> new Action_MoveArcadeHouse(editor, tile).execute());

        var miClearFoodAroundHouse = new MenuItem(translated("menu.edit.clear_food_around_house"));
        miClearFoodAroundHouse.setOnAction(_ -> new Action_ClearFoodAroundHouse(editor, worldMap()).execute());

        var miInsertRow = new MenuItem(translated("menu.edit.insert_row"));
        miInsertRow.setOnAction(_ -> {
            int rowIndex = tileAt(menuEvent.getX(), menuEvent.getY()).y();
            editor.setCurrentWorldMap(worldMap().insertRowBeforeIndex(rowIndex));
        });

        var miDeleteRow = new MenuItem(translated("menu.edit.delete_row"));
        miDeleteRow.setOnAction(_ -> {
            int rowIndex = tileAt(menuEvent.getX(), menuEvent.getY()).y();
            editor.setCurrentWorldMap(worldMap().deleteRowAtIndex(rowIndex));
        });

        var miFloodWithPellets = new MenuItem(translated("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(_ -> new Action_FloodWithPellets(editor, tile).execute());
        miFloodWithPellets.setDisable(!EditorUtil.canPlaceFoodAtTile(worldMap(), tile));

        contextMenu.getItems().setAll(
            miInsertRow,
            miDeleteRow,
            new SeparatorMenuItem(),
            miCarveTunnel,
            miPlaceHouse,
            miClearFoodAroundHouse,
            new SeparatorMenuItem(),
            miFloodWithPellets);

        contextMenu.show(this, menuEvent.getScreenX(), menuEvent.getScreenY());
    }
}
