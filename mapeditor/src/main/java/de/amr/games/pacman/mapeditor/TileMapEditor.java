/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.Tiles;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.mapeditor.TileMapUtil.*;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    private static final ResourceBundle TEXTS = ResourceBundle.getBundle("de.amr.games.pacman.mapeditor.texts");

    public static String tt(String key, Object... args) {
        return MessageFormat.format(TEXTS.getString(key), args);
    }

    private static final String PALETTE_TERRAIN = "Terrain";
    private static final String PALETTE_ACTORS  = "Actors";
    private static final String PALETTE_FOOD    = "Food";

    private static WorldMap createNewMap(int numRows, int numCols) {
        var map = new WorldMap(numRows, numCols);
        map.terrain().setProperty(PROPERTY_COLOR_WALL_STROKE, DEFAULT_COLOR_WALL_STROKE);
        map.terrain().setProperty(PROPERTY_COLOR_WALL_FILL, DEFAULT_COLOR_WALL_FILL);
        map.terrain().setProperty(PROPERTY_COLOR_DOOR, DEFAULT_COLOR_DOOR);
        map.terrain().setProperty(PROPERTY_POS_PAC,          formatTile(DEFAULT_POS_PAC));
        map.terrain().setProperty(PROPERTY_POS_RED_GHOST,    formatTile(DEFAULT_POS_RED_GHOST));
        map.terrain().setProperty(PROPERTY_POS_PINK_GHOST,   formatTile(DEFAULT_POS_PINK_GHOST));
        map.terrain().setProperty(PROPERTY_POS_CYAN_GHOST,   formatTile(DEFAULT_POS_CYAN_GHOST));
        map.terrain().setProperty(PROPERTY_POS_ORANGE_GHOST, formatTile(DEFAULT_POS_ORANGE_GHOST));
        map.food().setProperty(PROPERTY_COLOR_FOOD, DEFAULT_FOOD_COLOR);
        return map;
    }

    private static final RectShape GHOST_HOUSE_SHAPE = new RectShape(new byte[][] {
        { 0, 0, 0,20, 0, 0, 0, 0},
        {10, 8, 8,14,14, 8, 8,11},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9,22, 0,21, 0,23, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {13, 8, 8, 8, 8, 8, 8,12}
    });

    public final ObjectProperty<String> titlePy = new SimpleObjectProperty<>(this, "title", "Map Editor");

    public final BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);

    public final BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);

    public final BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);

    public final BooleanProperty editingEnabledPy = new SimpleBooleanProperty(false);

    public final BooleanProperty previewVisiblePy = new SimpleBooleanProperty(this, "previewVisible", true);

    public final ObjectProperty<Integer> gridSizePy = new SimpleObjectProperty<>(this, "gridSize", 16) {
        @Override
        protected void invalidated() {
            Logger.info("Grid size: {}", get());
            invalidatePaths();
            draw();
        }
    };

    private Window ownerWindow;
    private MenuBar menuBar;
    private Menu menuFile;
    private Menu menuEdit;
    private Menu menuLoadMap;
    private Pane layout;
    private Canvas editCanvas;
    private Canvas previewCanvas;
    private Label infoLabel;
    private FileChooser fileChooser;

    private final Map<String, Palette> palettes = new HashMap<>();

    private TabPane palettesTab;

    private PropertyEditor terrainMapPropertiesEditor;
    private TileMapEditorTerrainRenderer terrainMapRenderer;
    private boolean pathsUpToDate;

    private PropertyEditor foodMapPropertiesEditor;
    private FoodMapRenderer foodMapRenderer;

    private final ObjectProperty<WorldMap> mapPy = new SimpleObjectProperty<>();
    private final Map<String, WorldMap> predefinedMaps = new HashMap<>();

    private final Text editHint = new Text(tt("click_to_start"));
    private boolean unsavedChanges;
    private Vector2i hoveredTile;
    private File lastUsedDir;
    private File currentMapFile;

    private Timeline clock;

    public TileMapEditor() {
        mapPy.set(createNewMap(36, 28));
        lastUsedDir = new File(System.getProperty("user.home"));
    }

    public TileMapEditor(File workDir) {
        mapPy.set(createNewMap(36, 28));
        lastUsedDir = workDir;
    }

    public WorldMap map() {
        return mapPy.get();
    }

    public void createUI(Window ownerWindow) {
        this.ownerWindow = ownerWindow;

        terrainMapRenderer = new TileMapEditorTerrainRenderer();
        terrainMapRenderer.setWallStrokeColor(parseColor(DEFAULT_COLOR_WALL_STROKE));
        terrainMapRenderer.setWallFillColor(parseColor(DEFAULT_COLOR_WALL_FILL));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
        foodMapRenderer.setEnergizerColor(TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));

        createLayout();
        createMenus();

        // Note: this must be done after having loaded the initial map!
        editCanvas.heightProperty().bind(Bindings.createDoubleBinding(
                () -> (double) mapPy.get().numRows() * gridSize(), mapPy, gridSizePy));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
                () -> (double) mapPy.get().numCols() * gridSize(), mapPy, gridSizePy));
        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        int fps = 10;
        clock = new Timeline(fps, new KeyFrame(Duration.millis(1000.0 / fps), e -> {
            try {
                updateInfo();
                draw();
            } catch (Exception x) {
                x.printStackTrace(System.err);
                drawBlueScreen(x);
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        clock.play();
    }

    public void stop() {
        clock.stop();
        editingEnabledPy.set(false);
        unsavedChanges = false;
    }

    private void createLayout() {
        fileChooser = new FileChooser();
        var worldExtensionFilter = new FileChooser.ExtensionFilter("World Map Files", "*.world");
        fileChooser.getExtensionFilters().add(worldExtensionFilter);
        fileChooser.setSelectedExtensionFilter(worldExtensionFilter);
        fileChooser.setInitialDirectory(lastUsedDir);

        editCanvas = new Canvas();
        editCanvas.setOnMouseClicked(this::onMouseClickedOnEditCanvas);
        editCanvas.setOnMouseMoved(this::onMouseMovedOverEditCanvas);

        previewCanvas = new Canvas();

        var editCanvasScroll = new ScrollPane(editCanvas);
        editCanvasScroll.setFitToHeight(true);

        var previewCanvasScroll = new ScrollPane(previewCanvas);
        previewCanvasScroll.setFitToHeight(true);
        previewCanvasScroll.vvalueProperty().bindBidirectional(editCanvasScroll.vvalueProperty());
        previewCanvasScroll.visibleProperty().bind(previewVisiblePy);

        var cbPreviewVisible = new CheckBox(tt("show_preview"));
        cbPreviewVisible.selectedProperty().bindBidirectional(previewVisiblePy);

        var cbTerrainVisible = new CheckBox(tt("terrain"));
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox(tt("pellets"));
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox(tt("grid"));
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        var spinnerGridSize = new Spinner<Integer>(8, 48, 16);
        spinnerGridSize.getValueFactory().valueProperty().bindBidirectional(gridSizePy);
        var gridSizeLabel = new Label(tt("grid_size"));
        var gridSizeEditor = new HBox(gridSizeLabel, spinnerGridSize);
        gridSizeEditor.setSpacing(3);
        gridSizeEditor.setAlignment(Pos.BASELINE_LEFT);

        var terrainPalette = new Palette(32, 4, 4, terrainMapRenderer);
        terrainPalette.setTools(
            terrainPalette.changeTileValueTool(Tiles.WALL_H, "Horiz. Wall"),
            terrainPalette.changeTileValueTool(Tiles.WALL_V, "Vert. Wall"),
            terrainPalette.changeTileValueTool(Tiles.DWALL_H, "Hor. Double-Wall"),
            terrainPalette.changeTileValueTool(Tiles.DWALL_V, "Vert. Double-Wall"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_NW, "NW Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_NE, "NE Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_SW, "SW Corner"),
            terrainPalette.changeTileValueTool(Tiles.CORNER_SE, "SE Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_NW, "NW Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_NE, "NE Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_SW, "SW Corner"),
            terrainPalette.changeTileValueTool(Tiles.DCORNER_SE, "SE Corner"),
            terrainPalette.changeTileValueTool(Tiles.EMPTY, "Empty Space"),
            terrainPalette.changeTileValueTool(Tiles.TUNNEL, "Tunnel"),
            terrainPalette.changeTileValueTool(Tiles.DOOR, "Door")
        );
        palettes.put(PALETTE_TERRAIN, terrainPalette);

        var actorPalette = new Palette(32, 3, 4, terrainMapRenderer);
        actorPalette.setTools(
            actorPalette.changePropertyValueTool(PROPERTY_POS_RED_GHOST, "Red Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_PINK_GHOST, "Pink Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_CYAN_GHOST, "Cyan Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_ORANGE_GHOST, "Orange Ghost"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_RED_GHOST, "Red Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"),
            actorPalette.changePropertyValueTool(PROPERTY_POS_PAC, "Pac-Man")
        );
        palettes.put(PALETTE_ACTORS, actorPalette);

        var foodPalette = new Palette(32, 1, 4, foodMapRenderer);
        foodPalette.setTools(
            foodPalette.changeTileValueTool(Tiles.EMPTY, "No Food"),
            foodPalette.changeTileValueTool(Tiles.PELLET, "Pellet"),
            foodPalette.changeTileValueTool(Tiles.ENERGIZER, "Energizer")
        );
        palettes.put(PALETTE_FOOD, foodPalette);

        var terrainPaletteTab = new Tab(tt("terrain"), terrainPalette);
        terrainPaletteTab.setClosable(false);
        terrainPaletteTab.setUserData(PALETTE_TERRAIN);

        var actorPaletteTab = new Tab(tt("actors"), actorPalette);
        actorPaletteTab.setClosable(false);
        actorPaletteTab.setUserData(PALETTE_ACTORS);

        var foodPaletteTab = new Tab(tt("pellets"), foodPalette);
        foodPaletteTab.setClosable(false);
        foodPaletteTab.setUserData(PALETTE_FOOD);

        palettesTab = new TabPane(terrainPaletteTab, actorPaletteTab, foodPaletteTab);

        terrainMapPropertiesEditor = new PropertyEditor(tt("terrain"), this);
        terrainMapPropertiesEditor.enabledPy.bind(editingEnabledPy);

        foodMapPropertiesEditor = new PropertyEditor(tt("pellets"), this);
        foodMapPropertiesEditor.enabledPy.bind(editingEnabledPy);

        infoLabel = new Label();

        VBox controlsPane = new VBox();
        controlsPane.setSpacing(10);
        controlsPane.setMinWidth(200);
        controlsPane.getChildren().add(cbPreviewVisible);
        controlsPane.getChildren().add(new HBox(20, cbTerrainVisible, cbFoodVisible, cbGridVisible));
        controlsPane.getChildren().add(gridSizeEditor);
        controlsPane.getChildren().add(infoLabel);
        controlsPane.getChildren().add(palettesTab);
        controlsPane.getChildren().add(terrainMapPropertiesEditor);
        controlsPane.getChildren().add(foodMapPropertiesEditor);

        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var splitPane = new SplitPane(editCanvasScroll, controlsPane, previewCanvasScroll);
        //splitPane.setDividerPositions(0.45, 0.55);
        layout = new BorderPane(splitPane);
    }

    private void createMenus() {
        createFileMenu();
        createEditMenu();

        menuLoadMap = new Menu(tt("menu.load_map"));
        menuLoadMap.disableProperty().bind(editingEnabledPy.not());
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuFile, menuEdit, menuLoadMap);
    }

    private void createFileMenu() {
        var miNewMap = new MenuItem(tt("menu.file.new"));
        miNewMap.setOnAction(e -> showCreateNewMapDialog());

        var miOpenMapFile = new MenuItem(tt("menu.file.open"));
        miOpenMapFile.setOnAction(e -> openMapFile());

        var miSaveMapFileAs = new MenuItem(tt("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(e -> saveMapFileAs());

        menuFile = new Menu(tt("menu.file"));
        menuFile.getItems().addAll(miNewMap, miOpenMapFile, miSaveMapFileAs);
    }

    private void createEditMenu() {
        var miAddBorder = new MenuItem(tt("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> addBorder(map().terrain(), 3, 2));

        var miClearTerrain = new MenuItem(tt("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> map().terrain().clear());

        var miClearFood = new MenuItem(tt("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> map().food().clear());

        var miAddHouse = new MenuItem(tt("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> addHouse());

        menuEdit = new Menu(tt("menu.edit"));
        menuEdit.getItems().addAll(miAddBorder, miAddHouse, miClearTerrain, miClearFood);
        menuEdit.disableProperty().bind(editingEnabledPy.not());
    }

    public void addPredefinedMap(String description, WorldMap map) {
        checkNotNull(description);
        checkNotNull(map);
        predefinedMaps.put(description, map);
        var miLoadMap = new MenuItem(description);
        miLoadMap.setOnAction(e -> loadMap(map));
        menuLoadMap.getItems().add(miLoadMap);
    }

    public WorldMap getPredefinedMap(String description) {
        if (!predefinedMaps.containsKey(description)) {
            Logger.error("No predefind map '{}' exists", description);
            return null;
        }
        return predefinedMaps.get(description);
    }

    private void updateInfo() {
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "n/a";
        infoLabel.setText(text);
        if (currentMapFile != null) {
            titlePy.set("Map Editor: " + currentMapFile.getPath());
        } else {
            titlePy.set("Map Editor");
        }
    }

    private void updatePaths() {
        if (!pathsUpToDate) {
            map().terrain().computePaths();
            pathsUpToDate = true;
        }
    }

    private void invalidatePaths() {
        pathsUpToDate = false;
    }

    public void markMapEdited() {
        unsavedChanges = true;
    }

    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    private void addHouse() {
        int row = map().numRows() / 2 - 4;
        int col = map().numCols() / 2 - 4;
        GHOST_HOUSE_SHAPE.addToMap(map().terrain(), row, col);
        invalidatePaths();
        markMapEdited();
    }

    private void addBorder(TileMap terrain, int emptyRowsTop, int emptyRowsBottom) {
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

        invalidatePaths();
        markMapEdited();
    }

    public void setMap(WorldMap other) {
        checkNotNull(other);
        mapPy.set(other);
        foodMapPropertiesEditor.edit(map().food().getProperties());
        terrainMapPropertiesEditor.edit(map().terrain().getProperties());
        invalidatePaths();
        updatePaths();
        Logger.debug("Edit canvas size: w={} h={}", editCanvas.getWidth(), editCanvas.getHeight());
    }

    public void loadMap(WorldMap otherMap) {
        checkNotNull(otherMap);
        if (hasUnsavedChanges()) {
            showConfirmation(this::saveMapFileAs, () -> {
                setMap(new WorldMap(otherMap));
                currentMapFile = null;
            });
        } else {
            setMap(new WorldMap(otherMap));
            currentMapFile = null;
        }
    }

    private void showCreateNewMapDialog() {
        TextInputDialog dialog = new TextInputDialog("28x36");
        dialog.setTitle("Map Size");
        dialog.setHeaderText("Enter Map Size (cols x rows)");
        dialog.setContentText("Map Size:");
        dialog.showAndWait().ifPresent(text -> {
            String[] tuple = text.split("x");
            try {
                int numCols = Integer.parseInt(tuple[0].trim());
                int numRows = Integer.parseInt(tuple[1].trim());
                var newMap = createNewMap(numRows, numCols);
                setMap(newMap);
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    private void openMapFile() {
        fileChooser.setTitle("Open Pac-Man Map");
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showOpenDialog(ownerWindow);
        if (file != null) {
            readMapFile(file);
        }
    }

    private void readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            loadMap(new WorldMap(file));
            lastUsedDir = file.getParentFile();
            currentMapFile = file;
            Logger.info("Map read from file {}", file);
        }
    }

    public void saveMapFileAs() {
        fileChooser.setTitle("Save Pac-Man Map");
        fileChooser.setInitialDirectory(lastUsedDir);
        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                map().save(file);
                unsavedChanges = false;
                readMapFile(file);
            } else {
                Logger.error("No .world file selected"); //TODO
            }
        }
    }

    public void showConfirmation(Runnable saveAction, Runnable dontSaveAction) {
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("There are unsaved changes");
            alert.setHeaderText("Save changes?");
            alert.setContentText("You can save your changes or leave without saving");
            var saveChoice = new ButtonType("Save Changes");
            var dontSaveChoice = new ButtonType("Don't Save");
            var cancelChoice = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(saveChoice, dontSaveChoice, cancelChoice);
            alert.showAndWait().ifPresent(choice -> {
                if (choice == saveChoice) {
                    saveAction.run();
                } else if (choice == dontSaveChoice) {
                    dontSaveAction.run();
                }
            });
        } else {
            stop();
            dontSaveAction.run();
        }
    }

    public Pane getLayout() {
        return layout;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu menuFile() {
        return menuFile;
    }

    public Menu menuLoadMap() {
        return menuLoadMap;
    }

    /**
     * @return pixels used by one tile at current window zoom
     */
    private int gridSize() {
        return gridSizePy.get();
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    private int fullTiles(double pixels) {
        return (int) (pixels / gridSize());
    }

    // TODO use own canvas or Text control
    private void drawBlueScreen(Exception drawException) {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLUE);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        g.setStroke(Color.WHITE);
        g.setFont(Font.font("Monospace", 12));
        try {
            Logger.error(drawException);
            var trace = new StringWriter();
            drawException.printStackTrace(new PrintWriter(trace));
            g.strokeText(trace.toString(), 0, 20);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Logger.error(e);
        }
    }

    private void drawEditingHint(GraphicsContext g) {
        double s = gridSize() / 8.0;
        editHint.setFont(Font.font("Sans", FontWeight.BLACK, 16 * s));
        double x = 16 * s;
        double y = 24 * s;
        g.setFont(editHint.getFont());
        g.setStroke(Color.LIGHTGREEN);
        g.setLineWidth(3);
        g.strokeText(editHint.getText(), x, y);
        g.setFill(Color.DARKGREEN);
        g.fillText(editHint.getText(), x, y);
    }

    private void drawEditCanvas() {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(false);
            terrainMapRenderer.drawMap(g, map().terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
        if (!editingEnabledPy.get()) {
            drawEditingHint(g);
        } else {
            if (hoveredTile != null) {
                double tilePx = gridSize();
                g.setStroke(Color.YELLOW);
                g.setLineWidth(1);
                g.strokeRect(hoveredTile.x() * tilePx, hoveredTile.y() * tilePx, tilePx, tilePx);
            }
        }
    }

    private void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(gridSize() / 8.0);
            terrainMapRenderer.setWallStrokeColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_STROKE, parseColor(DEFAULT_COLOR_WALL_STROKE)));
            terrainMapRenderer.setWallFillColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_WALL_FILL, parseColor(DEFAULT_COLOR_WALL_FILL)));
            terrainMapRenderer.setDoorColor(getColorFromMap(map().terrain(), PROPERTY_COLOR_DOOR, parseColor(DEFAULT_COLOR_DOOR)));
            terrainMapRenderer.setRuntimeMode(true);
            terrainMapRenderer.drawMap(g, map().terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(gridSize() / 8.0);
            Color foodColor = getColorFromMap(map().food(), PROPERTY_COLOR_FOOD, TileMapUtil.parseColor(DEFAULT_FOOD_COLOR));
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map().food());
        }
    }

    private void draw() {
        drawEditCanvas();
        drawPreviewCanvas();
        palettes.get(selectedPaletteID()).draw();
    }

    private void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get()) {
            g.save();
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = gridSize();
            for (int row = 1; row < map().terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, editCanvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < map().terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, editCanvas.getHeight());
            }
            g.restore();
        }
    }

    private Vector2i tileAtMousePosition(double mouseX, double mouseY) {
        return new Vector2i(fullTiles(mouseX), fullTiles(mouseY));
    }

    private void onMouseClickedOnEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get()) {
            editingEnabledPy.set(true);
            return;
        }
        switch (selectedPaletteID()) {
            case PALETTE_TERRAIN -> editTerrainMapTile(e);
            case PALETTE_ACTORS  -> {
                if (selectedPalette().selectedTool != null) {
                    Vector2i tile = tileAtMousePosition(e.getX(), e.getY());
                    selectedPalette().selectedTool.apply(map().terrain(), tile);
                    markMapEdited();
                    terrainMapPropertiesEditor.updateEditors();
                }
            }
            case PALETTE_FOOD    -> editFoodMapTile(e);
            default -> Logger.error("Unknown palette selection");
        }
    }

    private String selectedPaletteID() {
        return (String) palettesTab.getSelectionModel().getSelectedItem().getUserData();
    }

    private Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }

    private void onMouseMovedOverEditCanvas(MouseEvent e) {
        if (!editingEnabledPy.get()) {
            return;
        }
        hoveredTile = tileAtMousePosition(e.getX(), e.getY());
        if (e.isShiftDown()) {
            switch (selectedPaletteID()) {
                case PALETTE_TERRAIN -> {
                    if (selectedPalette().selectedTool != null) {
                        selectedPalette().selectedTool.apply(map().terrain(), hoveredTile);
                    }
                    markMapEdited();
                    invalidatePaths();
                }
                case PALETTE_FOOD -> {
                    if (selectedPalette().selectedTool != null) {
                        selectedPalette().selectedTool.apply(map().food(), hoveredTile);
                    }
                    markMapEdited();
                }
                default -> {}
            }
        }
    }

    private void editTerrainMapTile(MouseEvent e) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            map().terrain().set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = map().terrain().get(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            map().terrain().set(tile, nextValue);
        }
        else {
            if (selectedPalette().selectedTool != null) {
                selectedPalette().selectedTool.apply(map().terrain(), tile);
            }
        }
        invalidatePaths();
        markMapEdited();
    }

    private void editFoodMapTile(MouseEvent e) {
        var tile = tileAtMousePosition(e.getX(), e.getY());
        if (e.getButton() == MouseButton.SECONDARY) {
            map().food().set(tile, Tiles.EMPTY);
        }
        else if (e.isShiftDown()) {
            // cycle through all palette values
            byte content = map().food().get(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            map().food().set(tile, newValue);
        }
        else {
            if (selectedPalette().selectedTool != null) {
                selectedPalette().selectedTool.apply(map().food(), tile);
            }
        }
        markMapEdited();
    }
}