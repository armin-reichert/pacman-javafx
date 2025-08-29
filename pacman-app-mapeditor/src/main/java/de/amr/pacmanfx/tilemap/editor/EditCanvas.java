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
import de.amr.pacmanfx.tilemap.editor.actions.*;
import de.amr.pacmanfx.tilemap.editor.rendering.EditorActorRenderer;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
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

import java.io.File;
import java.util.function.Predicate;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class EditCanvas extends Canvas {

    public static final Cursor CURSOR_RUBBER = Cursor.cursor(urlString("graphics/radiergummi.jpg"));

    private final ObjectProperty<EditMode> editMode = new SimpleObjectProperty<>();
    private final ObjectProperty<Vector2i> focussedTile = new SimpleObjectProperty<>();
    private final DoubleProperty gridSize = new SimpleDoubleProperty(8);
    private final DoubleProperty scaling = new SimpleDoubleProperty(1);
    private final ObjectProperty<Image> templateImageGray = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private final BooleanProperty actorsVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty foodVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty gridVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty obstacleInnerAreaDisplayed = new SimpleBooleanProperty(true);
    private final BooleanProperty obstaclesJoining = new SimpleBooleanProperty(true);
    private final BooleanProperty segmentNumbersVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty symmetricEditMode = new SimpleBooleanProperty(true);
    private final BooleanProperty terrainVisible = new SimpleBooleanProperty(true);

    private final GraphicsContext ctx;
    private final ObstacleEditor obstacleEditor;
    private final ContextMenu contextMenu = new ContextMenu();

    private final TerrainTileMapRenderer terrainRenderer;
    private final FoodMapRenderer foodRenderer;
    private final EditorActorRenderer actorRenderer;

    private final TileMapEditorUI ui;

    private boolean dragging = false;

    public EditCanvas(TileMapEditorUI ui, TileMapEditor.ChangeManager changeManager) {
        this.ui = requireNonNull(ui);

        obstacleEditor = new ObstacleEditor(changeManager);
        obstacleEditor.joiningProperty().bind(obstaclesJoiningProperty());
        obstacleEditor.symmetricEditModeProperty().bind(symmetricEditModeProperty());
        obstacleEditor.worldMapProperty().bind(worldMapProperty());

        ctx = getGraphicsContext2D();

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

        scaling.bind(gridSize.divide(TS));

        terrainRenderer = new TerrainTileMapRenderer(this);
        terrainRenderer.scalingProperty().bind(scaling);

        foodRenderer = new FoodMapRenderer(this);
        foodRenderer.scalingProperty().bind(scaling);

        actorRenderer = new EditorActorRenderer(this);
        actorRenderer.scalingProperty().bind(scaling);

        setOnMouseDragged(this::onMouseDragged);
    }

    public ObstacleEditor obstacleEditor() {
        return obstacleEditor;
    }

    // -- Properties

    public ObjectProperty<EditMode> editModeProperty() {
        return editMode;
    }

    public BooleanProperty actorsVisibleProperty() {
        return actorsVisible;
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


    public TerrainTileMapRenderer terrainRenderer() {
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

    public void draw(TileMapEditor.ChangeManager changeManager, TerrainMapColorScheme colorScheme) {
        double width = getWidth(), height = getHeight();
        ctx.setImageSmoothing(false);

        ctx.setFill(colorScheme.floorColor());
        ctx.fillRect(0, 0, width, height);

        if (templateImageGray.get() != null) {
            ctx.drawImage(templateImageGray.get(),
                0, EMPTY_ROWS_BEFORE_MAZE * scaling() * TS,
                width, height - (EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE) * scaling() * TS);
        }

        if (gridVisibleProperty().get()) {
            drawGrid();
        }

        // Indicate start and end of reserved areas at top and bottom
        ctx.save();
        ctx.setStroke(Color.grayRgb(200, 0.75));
        ctx.setLineWidth(0.75);
        ctx.setLineDashes(5, 5);
        ctx.strokeLine(0, EMPTY_ROWS_BEFORE_MAZE * scaling() * TS, width, EMPTY_ROWS_BEFORE_MAZE * scaling() * TS);
        ctx.strokeLine(0, height - EMPTY_ROWS_BELOW_MAZE * scaling() * TS, width, height - EMPTY_ROWS_BELOW_MAZE * scaling() * TS);
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
        for (Vector2i tile : changeManager.tilesWithErrors()) {
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
            Color foodColor = getColorFromMap(worldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, parseColor(MS_PACMAN_COLOR_FOOD));
            foodRenderer.setEnergizerColor(foodColor);
            foodRenderer.setPelletColor(foodColor);
            worldMap().tiles().forEach(tile -> foodRenderer.drawTile(tile, worldMap().content(LayerID.FOOD, tile)));
        }

        if (actorsVisible.get()) {
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_PAC), PAC_MAN);
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_RED_GHOST), RED_GHOST);
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_PINK_GHOST), PINK_GHOST);
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_CYAN_GHOST), CYAN_GHOST);
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_ORANGE_GHOST), ORANGE_GHOST);
            actorRenderer.drawActor(worldMap().getTerrainTileProperty(WorldMapProperty.POS_BONUS), STRAWBERRY);
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

    public void onMouseClicked(TileMapEditor editor, MouseEvent mouseEvent) {
        Logger.debug("Mouse clicked {}", mouseEvent);
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            requestFocus();
            contextMenu.hide();
            if (mouseEvent.getClickCount() == 2 && editor.editModeIs(EditMode.INSPECT)) {
                editor.ui().showEditHelpText();
            }
        }
    }

    private void onMouseDragged(MouseEvent event) {
        Logger.debug("Mouse dragged {}", event);
        if (!dragging) {
            Vector2i dragStartTile = tileAt(event.getX(), event.getY());
            obstacleEditor.startEditing(dragStartTile);
            dragging = true;
            Logger.debug("Dragging started at tile {}", dragStartTile);
        } else {
            obstacleEditor.continueEditing(tileAt(event.getX(), event.getY()));
        }
    }

    public void onMouseReleased(TileMapEditor editor, MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY) return;
        if (dragging) {
            dragging = false;
            obstacleEditor.endEditing();
        } else {
            Vector2i tile = tileAt(mouseEvent.getX(), mouseEvent.getY());
            if (editor.editModeIs(EditMode.INSPECT)) {
                new Action_IdentifyObstacle(editor, tile).execute();
            } else {
                boolean erase = mouseEvent.isControlDown();
                new Action_EditTile(editor, tile, erase).execute();
            }
        }
    }

    public void onMouseMoved(TileMapEditor editor, MouseEvent mouseEvent) {
        Vector2i tile = tileAt(mouseEvent.getX(), mouseEvent.getY());
        focussedTile.set(tile);
        switch (editMode.get()) {
            case INSPECT -> {}
            case EDIT -> {
                if (mouseEvent.isShiftDown()) {
                    switch (editor.paletteID()) {
                        case PALETTE_ID_TERRAIN -> {
                            Palette palette = ui.selectedPalette();
                            if (palette.isToolSelected()) {
                                palette.selectedTool().editor().accept(LayerID.TERRAIN, focussedTile());
                            }
                        }
                        case PALETTE_ID_FOOD -> {
                            Palette palette = ui.selectedPalette();
                            if (palette.isToolSelected()) {
                                palette.selectedTool().editor().accept(LayerID.FOOD, focussedTile());
                            }
                        }
                        default -> {}
                    }
                }
            }
            case ERASE -> {
                if (mouseEvent.isShiftDown()) {
                    switch (ui.selectedPaletteID()) {
                        case PALETTE_ID_TERRAIN -> new Action_ClearTerrainTile(editor, tile).execute();
                        case PALETTE_ID_FOOD -> new Action_ClearFoodTile(editor, tile).execute();
                    }
                }
            }
        }
    }

    public void onFileDropped(TileMapEditor editor, File file) {
        if (isWorldMapFile(file)) {
            new Action_ReplaceCurrentWorldMapChecked(editor, file).execute();
        }
        else if (isImageFile(file) && !editor.editModeIs(EditMode.INSPECT)) {
            Image image = loadImage(file).orElse(null);
            if (image == null) {
                ui.messageDisplay().showMessage("Could not open image file '%s'".formatted(file), 3, MessageType.ERROR);
                return;
            }
            if (!isTemplateImageSizeOk(image)) {
                editor.ui().messageDisplay().showMessage("Template image file '%s' has dubios size".formatted(file), 3, MessageType.ERROR);
                return;
            }
            editor.setTemplateImage(image);
            new Action_CreateMapFromTemplate(editor, image).execute();
            ui.selectTemplateImageTab();
            ui.messageDisplay().showMessage("Select colors for tile identification!", 10, MessageType.INFO);
        }
    }

    private boolean isTemplateImageSizeOk(Image image) {
        return image.getHeight() % TS == 0 && image.getWidth() % TS == 0;
    }

    public void onContextMenuRequested(TileMapEditor editor, ContextMenuEvent menuEvent) {
        if (editor.editModeIs(EditMode.INSPECT)) {
            return;
        }
        if (menuEvent.isKeyboardTrigger()) {
            return;
        }

        Vector2i tile = tileAt(menuEvent.getX(), menuEvent.getY());

        var miPlaceHouse = new MenuItem(translated("menu.edit.place_house"));
        miPlaceHouse.setOnAction(actionEvent -> new Action_PlaceArcadeHouse(editor, worldMap(), tile).execute());

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
        miFloodWithPellets.setOnAction(ae -> new Action_FloodWithPellets(editor, tile, FoodTile.PELLET).execute());
        miFloodWithPellets.setDisable(!canEditFoodAtTile(editor.currentWorldMap(), tile));

        var miClearPellets = new MenuItem(translated("menu.edit.clear_food"));
        miClearPellets.setOnAction(ae -> new Action_FloodWithPellets(editor, tile, FoodTile.EMPTY).execute());
        miClearPellets.setDisable(!canEditFoodAtTile(editor.currentWorldMap(), tile));

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

    public void onKeyPressed(TileMapEditor editor, KeyEvent keyEvent) {
        KeyCode key = keyEvent.getCode();
        boolean control = keyEvent.isControlDown();

        if (key == KeyCode.LEFT) {
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
            new Action_SelectNextPaletteEntry(editor).execute();
        }
    }
}
