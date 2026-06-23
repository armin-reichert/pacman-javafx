/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.mapeditor.editcanvas;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.mapeditor.EditMode;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.TileMapEditorUtils;
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
import de.amr.pacmanfx.uilib.rendering.TerrainMapColoring;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.function.Predicate;

import static de.amr.pacmanfx.mapeditor.TileMapEditorGlobals.ACTOR_SPRITES;
import static de.amr.pacmanfx.mapeditor.TileMapEditorUtils.getColorFromMapLayer;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.COLOR_FOOD;
import static java.util.Objects.requireNonNull;

public class EditCanvas extends Canvas {

    public static final Color GRID_LINE_COLOR = Color.grayRgb(180);
    public static final double GRID_LINE_WIDTH = 0.5;
    public static final Color MAZE_AREA_SEPARATOR_COLOR = Color.grayRgb(222, 0.75);

    static class TerrainAndActorRenderer extends TerrainMapTileRenderer implements ActorSpriteRenderer {
        public TerrainAndActorRenderer(Canvas canvas) {
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
    private final EditCanvasContextMenu contextMenu;

    private final TerrainAndActorRenderer renderer;
    private final FoodMapRenderer foodRenderer;

    private final TileMapEditorUI ui;

    public EditCanvas(TileMapEditorUI ui) {
        this.ui = requireNonNull(ui);

        ctx = getGraphicsContext2D();

        contextMenu = new EditCanvasContextMenu(this, ui);

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
        scalingProperty()                   .bind(gridSize.divide(WorldMap.TS));
        symmetricEditModeProperty()         .bind(ui.editor().symmetricEditModeProperty());
        templateImageGrayProperty()         .bind(ui.editor().templateImageProperty().map(UfxImages::imageToGreyscale));
        terrainVisibleProperty()            .bind(ui.terrainVisibleProperty());
        worldMapProperty()                  .bind(ui.editor().currentWorldMapProperty());

        renderer = new TerrainAndActorRenderer(this);
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
        return new Vector2i(TileMapEditorUtils.fullTiles(mouseX, gridSize()), TileMapEditorUtils.fullTiles(mouseY, gridSize()));
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

    public void draw(TerrainMapColoring colors) {
        final double scaledTileSize = scaling() * WorldMap.TS;
        final TerrainLayer terrain = worldMap().terrainLayer();

        ctx.setImageSmoothing(false);

        ctx.setFill(colors.floorColor());
        ctx.fillRect(0, 0, getWidth(), getHeight());

        // Indicate if edit canvas has focus by a dashed yellow border
        if (isFocused()) {
            ctx.setStroke(Color.YELLOW);
            ctx.setLineWidth(2);
            ctx.setLineDashes(2, 10);
            ctx.strokeRect(0, 0, getWidth(), getHeight());
            ctx.setLineDashes();
        }

        if (templateImageGray.get() != null) {
            ctx.drawImage(templateImageGray.get(),
                0,
                terrain.emptyRowsOverMaze() * scaledTileSize,
                getWidth(),
                getHeight() - (terrain.emptyRowsOverMaze() + terrain.emptyRowsBelowMaze()) * scaledTileSize);
        }

        if (gridVisibleProperty().get()) {
            drawGrid();
        }

        // Indicate top and bottom of maze area with dashed horizontal lines
        ctx.save();
        ctx.setStroke(MAZE_AREA_SEPARATOR_COLOR);
        ctx.setLineWidth(0.75);
        ctx.setLineDashes(5, 5);
        ctx.strokeLine(
            0, terrain.emptyRowsOverMaze() * scaledTileSize,
            getWidth(), terrain.emptyRowsOverMaze() * scaledTileSize
        );
        ctx.strokeLine(
            0, getHeight() - terrain.emptyRowsBelowMaze() * scaledTileSize,
            getWidth(), getHeight() - terrain.emptyRowsBelowMaze() * scaledTileSize
        );
        ctx.restore();

        // Draw terrain
        if (terrainVisible.get()) {
            renderer.setMapColoring(colors);
            renderer.setSegmentNumbersDisplayed(segmentNumbersVisible.get());
            renderer.setObstacleInnerAreaDisplayed(obstacleInnerAreaDisplayed.get());
            renderer.draw(worldMap());
            obstacleEditor.draw(renderer);
        }

        // Indicate tiles that seem to be wrong
        ctx.setFont(Font.font("sans", gridSize() - 2));
        ctx.setFill(Color.grayRgb(200, 0.8));
        for (Vector2i tile : ui.editor().checkResult().tilesWithErrors()) {
            ctx.fillText("?", tile.x() * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            if (symmetricEditMode.get()) {
                final int x = worldMap().numCols() - tile.x() - 1;
                ctx.fillText("?", x * gridSize() + 0.25 * gridSize(), tile.y() * gridSize() + 0.8 * gridSize());
            }
        }

        // Draw vertical separator to indicate symmetric edit mode
        if (editMode.get() == EditMode.EDIT && symmetricEditMode.get()) {
            final double centerX = 0.5 * getWidth();
            ctx.save();
            ctx.setStroke(Color.YELLOW);
            ctx.setLineWidth(0.75);
            ctx.setLineDashes(5, 5);
            ctx.strokeLine(centerX, 0, centerX, getHeight());
            ctx.restore();
        }

        // Draw food
        if (foodVisible.get()) {
            final FoodLayer foodLayer = worldMap().foodLayer();
            final Color foodColor = getColorFromMapLayer(foodLayer, COLOR_FOOD, ArcadeSprites.MS_PACMAN_COLOR_FOOD);
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            foodLayer.tiles().forEach(tile -> foodRenderer.drawTile(tile, foodLayer.content(tile)));
        }

        // Draw actors
        if (actorsVisible.get()) {
            ACTOR_SPRITES.forEach((positionProperty, sprite) -> {
                final Vector2i tile = terrain.getTileProperty(positionProperty);
                if (tile != null) {
                    renderer.drawActorSprite(tile, sprite);
                }
            });
        }

        // Indicate focussed tile by a square
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
        ctx.setLineWidth(GRID_LINE_WIDTH);
        ctx.setStroke(GRID_LINE_COLOR);
        for (int row = 1; row < worldMap().numRows(); ++row) {
            ctx.strokeLine(0, row * gridSize(), getWidth(), row * gridSize());
        }
        for (int col = 1; col < worldMap().numCols(); ++col) {
            ctx.strokeLine(col * gridSize(), 0, col * gridSize(), getHeight());
        }
        ctx.restore();
    }

    // Event handlers

    public void onKeyPressed(KeyEvent event) {
        boolean control = event.isControlDown();
        switch (event.getCode()) {
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
        event.consume();
    }

    public void onMouseClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        event.consume();

        contextMenu.hide();
        requestFocus();
    }

    private void onDragDetected(MouseEvent event) {
        if (editMode.get() != EditMode.EDIT)
            return;

        event.consume();

        final Vector2i tileAtMouse = tileAt(event.getX(), event.getY());
        setDragging(true);
        obstacleEditor.startEditing(tileAtMouse);
    }

    private void onMouseDragged(MouseEvent event) {
        if (!dragging()) {
            return;
        }

        event.consume();

        final Vector2i tileAtMouse = tileAt(event.getX(), event.getY());
        obstacleEditor.continueEditing(tileAtMouse);
    }

    public void onMouseReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        if (dragging()) {
            setDragging(false);
            obstacleEditor.endEditing();
            event.consume();
        } else {
            final Vector2i tile = tileAt(event.getX(), event.getY());
            if (!ui.editModeIs(EditMode.INSPECT)) {
                if (event.isControlDown()) {
                    ui.selectedPaletteID().ifPresent(paletteID -> {
                        switch (paletteID) {
                            case PaletteID.TERRAIN -> {
                                event.consume();
                                new Action_ClearTerrainTile(ui.editor(), tile).execute();
                            }
                            case PaletteID.FOOD -> {
                                event.consume();
                                new Action_ClearFoodTile(ui.editor(), tile).execute();
                            }
                        }
                    });
                } else {
                    ui.selectedPalette().ifPresent(palette -> {
                        event.consume();
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

    public void onContextMenuRequested(ContextMenuEvent event) {
        if (ui.editModeIs(EditMode.INSPECT) || event.isKeyboardTrigger()) {
            return;
        }
        contextMenu.updateState(event);
        contextMenu.show(this, event.getScreenX(), event.getScreenY());
        event.consume();
    }
}
