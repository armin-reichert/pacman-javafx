/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemapeditor;

import de.amr.games.pacman.lib.TileMap;
import de.amr.games.pacman.lib.Tiles;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.WorldMap;
import de.amr.games.pacman.ui.fx.tilemap.FoodMapRenderer;
import de.amr.games.pacman.ui.fx.tilemap.TileMapRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class TileMapEditor  {

    static final byte[][] GHOST_HOUSE_SHAPE = {
        { 0, 0, 0,20, 0, 0, 0, 0},
        {10, 8, 8,14,14, 8, 8,11},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9,22, 0,21, 0,23, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {13, 8, 8, 8, 8, 8, 8,12}
    };

    static final Color DEFAULT_WALL_STROKE_COLOR = Color.GREEN;
    static final Color DEFAULT_WALL_FILL_COLOR = Color.MAROON;
    static final Color DEFAULT_DOOR_COLOR = Color.YELLOW;
    static final Color DEFAULT_FOOD_COLOR = Color.MAGENTA;

    public ObjectProperty<String> titlePy = new SimpleObjectProperty<>(this, "title", "Map Editor");

    Window ownerWindow;

    MenuBar menuBar;
    Menu menuFile;
    Menu menuEdit;
    Menu menuLoadMap;

    Pane layout;
    Canvas editCanvas;
    Canvas previewCanvas;
    PropertyEditor terrainMapPropertiesEditor;
    PropertyEditor foodMapPropertiesEditor;
    Label infoLabel;
    FileChooser openDialog;
    Palette terrainPalette;
    Palette foodPalette;

    Timeline clock;

    TileMapEditorTerrainRenderer terrainMapRenderer;
    boolean pathsUpToDate;
    FoodMapRenderer foodMapRenderer;

    WorldMap map;
    WorldMap[] arcadeMaps = new WorldMap[7];
    WorldMap[] masonicMaps = new WorldMap[8];

    Vector2i hoveredTile;
    File lastUsedDir = new File(System.getProperty("user.dir"));
    File currentMapFile;

    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true);
    BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);

    //TODO resources must be loaded differently
    public TileMapEditor(Stage stage) {
        terrainMapRenderer = new TileMapEditorTerrainRenderer();
        terrainMapRenderer.setWallStrokeColor(DEFAULT_WALL_STROKE_COLOR);
        terrainMapRenderer.setWallFillColor(DEFAULT_WALL_FILL_COLOR);

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(DEFAULT_FOOD_COLOR);

        ownerWindow = stage;

        createLayout();
        createMenus();

        arcadeMaps[0] = loadMap("maps/pacman.world", getClass());
        for (int i = 1; i <= 6; ++i) {
            arcadeMaps[i] = loadMap("maps/mspacman/mspacman_" + i + ".world", getClass());
        }
        for (int i = 1; i <= 8; ++i) {
            masonicMaps[i-1] = loadMap("maps/masonic/masonic_" + i + ".world", getClass());
        }
        setMap(arcadeMaps[0]);

        editCanvas.heightProperty().bind(layout.heightProperty().multiply(0.95));
        editCanvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> editCanvas.getHeight() * map.numCols() / map.numRows(), editCanvas.heightProperty()));

        previewCanvas.widthProperty().bind(editCanvas.widthProperty());
        previewCanvas.heightProperty().bind(editCanvas.heightProperty());

        int fps = 30;
        clock = new Timeline(30, new KeyFrame(Duration.millis(1000.0/fps),e -> {
            updateInfo();
            try {
                draw();
            } catch (Exception x) {
                x.printStackTrace(System.err);
                drawBlueScreen(x);
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void createLayout() {
        openDialog = new FileChooser();
        openDialog.setInitialDirectory(lastUsedDir);

        editCanvas = new Canvas();
        editCanvas.setOnMouseClicked(this::onMouseClickedOnCanvas);
        editCanvas.setOnMouseMoved(this::onMouseMovedOverCanvas);

        previewCanvas = new Canvas();

        terrainMapPropertiesEditor = new PropertyEditor("Terrain");
        foodMapPropertiesEditor = new PropertyEditor("Food");

        var cbTerrainVisible = new CheckBox("Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox("Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox("Grid");
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        terrainPalette = new Palette(32, 3, 8, Tiles.TERRAIN_TILES_END, terrainMapRenderer);
        terrainPalette.setValues(
            Tiles.EMPTY, Tiles.TUNNEL, Tiles.PAC_HOME, Tiles.DOOR,
            Tiles.SCATTER_TARGET_RED, Tiles.SCATTER_TARGET_PINK, Tiles.SCATTER_TARGET_CYAN, Tiles.SCATTER_TARGET_ORANGE,
            Tiles.HOME_RED_GHOST, Tiles.HOME_PINK_GHOST, Tiles.HOME_CYAN_GHOST, Tiles.HOME_ORANGE_GHOST,
            Tiles.WALL_H, Tiles.WALL_V, Tiles.DWALL_H, Tiles.DWALL_V,
            Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE,
            Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE
        );

        foodPalette = new Palette(32, 1, 4, Tiles.FOOD_TILES_END, foodMapRenderer);
        foodPalette.setValues(
            Tiles.EMPTY, Tiles.PELLET, Tiles.ENERGIZER, Tiles.EMPTY
        );

        TabPane tabPane = new TabPane();
        var terrainPaletteTab = new Tab("Terrain", terrainPalette);
        terrainPaletteTab.setClosable(false);
        terrainPaletteTab.setOnSelectionChanged(e -> terrainEditedPy.set(terrainPaletteTab.isSelected()));
        var foodPaletteTab = new Tab("Food", foodPalette);
        foodPaletteTab.setClosable(false);
        foodPaletteTab.setOnSelectionChanged(e -> terrainEditedPy.set(!foodPaletteTab.isSelected()));
        tabPane.getTabs().addAll(terrainPaletteTab, foodPaletteTab);

        infoLabel = new Label();

        VBox controlsPane = new VBox();
        controlsPane.setMinWidth(200);
        controlsPane.setSpacing(10);
        controlsPane.getChildren().add(new HBox(20, new Label("Show"), cbTerrainVisible, cbFoodVisible, cbGridVisible));
        controlsPane.getChildren().add(infoLabel);
        controlsPane.getChildren().add(tabPane);
        controlsPane.getChildren().add(terrainMapPropertiesEditor);
        controlsPane.getChildren().add(foodMapPropertiesEditor);

        terrainMapPropertiesEditor.setPadding(new Insets(10,0,0,0));
        foodMapPropertiesEditor.setPadding(new Insets(10,0,0,0));

        var editCanvasScrollPane = new ScrollPane(editCanvas);
        editCanvasScrollPane.setFitToHeight(true);

        var previewCanvasScrollPane = new ScrollPane(previewCanvas);
        previewCanvasScrollPane.setFitToHeight(true);

        var hbox = new HBox(editCanvasScrollPane, controlsPane, previewCanvasScrollPane);
        hbox.setSpacing(10);

        layout = new BorderPane(hbox);
    }

    private void createMenus() {
        createFileMenu();
        createActionsMenu();
        createLoadMapMenu();
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuFile, menuEdit, menuLoadMap);
    }

    private void createFileMenu() {
        var miNewMap = new MenuItem("New...");
        miNewMap.setOnAction(e -> createNewMap());

        var miOpenMapFile = new MenuItem("Open...");
        miOpenMapFile.setOnAction(e -> openMapFile());

        var miSaveMapFileAs = new MenuItem("Save As...");
        miSaveMapFileAs.setOnAction(e -> saveMapFileAs());

        menuFile = new Menu("File");
        menuFile.getItems().addAll(miNewMap, miOpenMapFile, miSaveMapFileAs);
    }

    private void createActionsMenu() {
        var miAddBorder = new MenuItem("Add Border");
        miAddBorder.setOnAction(e -> addBorder(map.terrain(), 3, 2));

        var miClearTerrain = new MenuItem("Clear Terrain");
        miClearTerrain.setOnAction(e -> map.terrain().clear());

        var miClearFood = new MenuItem("Clear Food");
        miClearFood.setOnAction(e -> map.food().clear());

        var miAddHouse = new MenuItem("Add House");
        miAddHouse.setOnAction(e -> addHouse());

        menuEdit = new Menu("Edit");
        menuEdit.getItems().addAll(miAddBorder, miAddHouse, miClearTerrain, miClearFood);
    }

    private void createLoadMapMenu() {
        menuLoadMap = new Menu("Load Map");

        var miLoadPacManMap = new MenuItem("Pac-Man");
        miLoadPacManMap.setOnAction(e -> loadMap(arcadeMaps[0]));
        menuLoadMap.getItems().add(miLoadPacManMap);

        menuLoadMap.getItems().add(new SeparatorMenuItem());

        IntStream.rangeClosed(1, 6).forEach(i -> {
            var mi = new MenuItem("Ms. Pac-Man " + i);
            mi.setOnAction(e -> loadMap(arcadeMaps[i]));
            menuLoadMap.getItems().add(mi);
        });

        menuLoadMap.getItems().add(new SeparatorMenuItem());

        IntStream.rangeClosed(1, 8).forEach(i -> {
            var mi = new MenuItem("Pac-Man XXL " + i);
            mi.setOnAction(e -> loadMap(masonicMaps[i-1]));
            menuLoadMap.getItems().add(mi);
        });
    }

    private void updatePaths() {
        if (!pathsUpToDate) {
            map.terrain().computePaths();
            pathsUpToDate = true;
        }
    }

    private void invalidatePaths() {
        pathsUpToDate = false;
    }

    private static WorldMap loadMap(String path, Class<?> loadingClass) {
        try {
            var url = loadingClass.getResource(path);
            if (url != null) {
                return new WorldMap(url);
            }
        } catch (Exception x) {
            Logger.error(x);
        }
        return null;
    }

    void addShape(byte[][] shape, int topLeftRow, int topLeftCol) {
        for (int row = 0; row < shape.length; ++row) {
            for (int col = 0; col < shape[0].length; ++col) {
                map.terrain().set(topLeftRow + row, topLeftCol+ col, shape[row][col]);
            }
        }
        invalidatePaths();
    }

    private void addHouse() {
        addShape(GHOST_HOUSE_SHAPE, 14, 10);
        map.terrain().set(26, 13, Tiles.PAC_HOME);
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
    }

    private void setMap(WorldMap other) {
        map = other;
        foodMapPropertiesEditor.edit(map.food().getProperties());
        terrainMapPropertiesEditor.edit(map.terrain().getProperties());
        invalidatePaths();
        updatePaths();
    }


    public void loadMap(WorldMap other) {
        setMap(WorldMap.copyOf(other));
        currentMapFile = null;
        updateInfo();
    }

    void createNewMap() {
        TextInputDialog dialog = new TextInputDialog("28x36");
        dialog.setTitle("Map Size");
        dialog.setHeaderText("Enter Map Size (cols x rows)");
        dialog.setContentText("Map Size:");
        dialog.showAndWait().ifPresent(text -> {
            String[] tuple = text.split("x");
            try {
                int numCols = Integer.parseInt(tuple[0].trim());
                int numRows = Integer.parseInt(tuple[1].trim());
                var newMap = new WorldMap(new TileMap(numRows, numCols), new TileMap(numRows, numCols));
                newMap.terrain().setProperty("wall_stroke_color", PropertyEditor.formatColor(DEFAULT_WALL_STROKE_COLOR));
                newMap.terrain().setProperty("wall_fill_color",   PropertyEditor.formatColor(DEFAULT_WALL_FILL_COLOR));
                newMap.terrain().setProperty("door_color",        PropertyEditor.formatColor(DEFAULT_DOOR_COLOR));
                newMap.food().setProperty("food_color",           PropertyEditor.formatColor(DEFAULT_FOOD_COLOR));
                setMap(newMap);
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    void openMapFile() {
        openDialog.setInitialDirectory(lastUsedDir);
        openDialog.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Word Map Files", ".world"));
        File file = openDialog.showOpenDialog(ownerWindow);
        if (file != null) {
            readMapFile(file);
            updateInfo();
        }
    }

    void readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            try {
                loadMap(new WorldMap(file));
                lastUsedDir = file.getParentFile();
                currentMapFile = file;
                Logger.info("Map read from file {}", file);
            }
            catch (Exception x) {
                Logger.error("Could not load world map from file {}", file);
                Logger.error(x);
            }
        }
        updateInfo();
    }

    void saveMapFileAs() {
        openDialog.setInitialDirectory(lastUsedDir);
        openDialog.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("World Map Files", ".world"));
        File file = openDialog.showSaveDialog(ownerWindow);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                map.save(file);
                readMapFile(file);
                updateInfo();
            } else {
                Logger.error("No .world file selected"); //TODO
            }
        }
    }


    public WorldMap getPacManMap() {
        return arcadeMaps[0];
    }

    public WorldMap getMsPacManMap(int mapNumber) {
        return 1 <= mapNumber && mapNumber <= 6 ? arcadeMaps[mapNumber] : arcadeMaps[1];
    }

    public WorldMap getMasonicMap(int mapNumber) {
        return 1 <= mapNumber && mapNumber <= 8 ? masonicMaps[mapNumber-1] : masonicMaps[0];
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


    // TODO use own canvas or Text control
    void drawBlueScreen(Exception drawException) {
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

    void drawEditCanvas() {
        GraphicsContext g = editCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, editCanvas.getWidth(), editCanvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(tilePx() / 8);
            terrainMapRenderer.setWallStrokeColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
            terrainMapRenderer.setWallFillColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
            terrainMapRenderer.setDoorColor(TileMapRenderer.getColorFromMap(map.terrain(), "door_color", DEFAULT_DOOR_COLOR));
            terrainMapRenderer.runtimePreview = false;
            terrainMapRenderer.drawMap(g, map.terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(tilePx() / 8);
            Color foodColor = TileMapRenderer.getColorFromMap(map.food(), "food_color", DEFAULT_FOOD_COLOR);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map.food());
        }
        double t1 = tilePx();
        if (hoveredTile != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(hoveredTile.x() * t1, hoveredTile.y() * t1, t1, t1);
        }
    }

    void drawPreviewCanvas() {
        GraphicsContext g = previewCanvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (terrainVisiblePy.get()) {
            updatePaths();
            terrainMapRenderer.setScaling(tilePx() / 8);
            terrainMapRenderer.setWallStrokeColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
            terrainMapRenderer.setWallFillColor(TileMapRenderer.getColorFromMap(map.terrain(), "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
            terrainMapRenderer.setDoorColor(TileMapRenderer.getColorFromMap(map.terrain(), "door_color", DEFAULT_DOOR_COLOR));
            terrainMapRenderer.runtimePreview = true;
            terrainMapRenderer.drawMap(g, map.terrain());
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(tilePx() / 8);
            Color foodColor = TileMapRenderer.getColorFromMap(map.food(), "food_color", DEFAULT_FOOD_COLOR);
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
            foodMapRenderer.drawMap(g, map.food());
        }
    }

    void draw() {
        drawEditCanvas();
        drawPreviewCanvas();
        if (terrainEditedPy.get()) {
            terrainPalette.draw();
        } else {
            foodPalette.draw();
        }
    }

    void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get()) {
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = tilePx();
            for (int row = 1; row < map.terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, editCanvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < map.terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, editCanvas.getHeight());
            }
        }
    }
    /**
     * @return pixels used by one tile at current window zoom
     */
    double tilePx() {
        return editCanvas.getHeight() / map.numRows();
    }

    /**
     * @param pixels number of pixels
     * @return number of full tiles spanned by pixels
     */
    int fullTiles(double pixels) {
        return (int) (pixels / tilePx());
    }

    void onMouseClickedOnCanvas(MouseEvent e) {
        if (terrainEditedPy.get()) {
            editTerrainTile(e);
        } else {
            editFoodTile(e);
        }
    }

    void onMouseMovedOverCanvas(MouseEvent e) {
        hoveredTile = new Vector2i(fullTiles(e.getX()), fullTiles(e.getY()));
        updateInfo();
        if (e.isShiftDown()) {
            if (terrainEditedPy.get()) {
                map.terrain().set(hoveredTile, terrainPalette.selectedValue);
            } else {
                map.food().set(hoveredTile, foodPalette.selectedValue);
            }
        }
    }

    void editTerrainTile(MouseEvent e) {
        var tile = new Vector2i(fullTiles(e.getX()), fullTiles(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            map.terrain().set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = map.terrain().get(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            map.terrain().set(tile, nextValue);
            updateInfo();
        }
        else {
            map.terrain().set(tile, terrainPalette.selectedValue);
            updateInfo();
        }
        invalidatePaths();
    }

    void editFoodTile(MouseEvent e) {
        var tile = new Vector2i(fullTiles(e.getX()), fullTiles(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            map.food().set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = map.food().get(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            map.food().set(tile, newValue);
            updateInfo();
        }
        else {
            map.food().set(tile, foodPalette.selectedValue);
            updateInfo();
        }
    }

    void updateInfo() {
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "n/a";
        infoLabel.setText(text);

        if (currentMapFile != null) {
            titlePy.set("Map Editor: " + currentMapFile.getPath());
        } else {
            titlePy.set("Map Editor");
        }
    }

}