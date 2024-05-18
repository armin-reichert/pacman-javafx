/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.model.world.WorldMap;
import de.amr.games.pacman.ui.fx.rendering2d.FoodMapRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer.getColorFromMap;

/**
 * @author Armin Reichert
 */
public class TileMapEditor extends Application  {

    public static void main(String[] args) {
        launch();
    }

    static final byte[][] GHOST_HOUSE_SHAPE = {
        {10, 8, 8,14,14, 8, 8,11},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        { 9, 0, 0, 0, 0, 0, 0, 9},
        {13, 8, 8, 8, 8, 8, 8,12}
    };

    static final int DEFAULT_NUM_COLS = 28;
    static final int DEFAULT_NUM_ROWS = 36;

    static final Color DEFAULT_WALL_STROKE_COLOR = Color.GREEN;
    static final Color DEFAULT_WALL_FILL_COLOR = Color.MAROON;
    static final Color DEFAULT_DOOR_COLOR = Color.YELLOW;
    static final Color DEFAULT_FOOD_COLOR = Color.MAGENTA;

    Stage stage;
    Scene scene;
    Pane sceneContent;
    MenuBar menuBar;
    Canvas canvas;
    TextArea terrainMapPropertiesEditor;
    TextArea foodMapPropertiesEditor;
    Label infoLabel;
    FileChooser openDialog;
    Palette terrainPalette;
    Palette foodPalette;

    TileMapEditorTerrainRenderer terrainMapRenderer;
    FoodMapRenderer foodMapRenderer;

    WorldMap map;
    WorldMap[] arcadeMaps = new WorldMap[7];

    Vector2i hoveredTile;
    File lastUsedDir = new File(System.getProperty("user.dir"));
    File currentMapFile;

    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true);
    BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty runtimePreviewPy = new SimpleBooleanProperty(false);

    @Override
    public void init() throws Exception {
        arcadeMaps[0] = WorldMap.copyOf(GameVariant.PACMAN.createWorld(1).map());
        for (int i = 1; i <= 6; ++i) {
            arcadeMaps[i] = WorldMap.copyOf(GameVariant.MS_PACMAN.createWorld(i).map());
        }
        map = new WorldMap(
            new TileMap(DEFAULT_NUM_ROWS, DEFAULT_NUM_COLS),
            new TileMap(DEFAULT_NUM_ROWS, DEFAULT_NUM_COLS));
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        sceneContent = createSceneContent();
        scene = new Scene(sceneContent, height * 1.2, height);
        scene.setFill(Color.BLACK);

        canvas.heightProperty().bind(sceneContent.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() * map.numCols() / map.numRows(), canvas.heightProperty()));

        terrainMapRenderer = new TileMapEditorTerrainRenderer();
        terrainMapRenderer.setWallStrokeColor(DEFAULT_WALL_STROKE_COLOR);
        terrainMapRenderer.setWallFillColor(DEFAULT_WALL_FILL_COLOR);

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(DEFAULT_FOOD_COLOR);

        terrainPalette.setRenderer(terrainMapRenderer);
        foodPalette.setRenderer(foodMapRenderer);

        stage.setTitle("Map Editor");
        stage.setScene(scene);
        stage.show();

        GameClockFX clock = new GameClockFX();
        clock.setTargetFrameRate(20);
        clock.setContinousCallback(() -> {
            updateInfo();
            try {
                draw();
            } catch (Exception x) {
                drawBlueScreen(x);
            }
        });
        clock.start();

        loadMap(arcadeMaps[3]);
    }

    Pane createSceneContent() {

        var menuFile = new Menu("File");

        var miNewMap = new MenuItem("New...");
        miNewMap.setOnAction(e -> createNewMap());

        var miLoadMap = new MenuItem("Load Map...");
        miLoadMap.setOnAction(e -> openMapFile());

        var miSaveMap = new MenuItem("Save Map...");
        miSaveMap.setOnAction(e -> saveMap());

        var miQuit = new MenuItem("Quit");
        miQuit.setOnAction(e -> stage.close());

        menuFile.getItems().addAll(miNewMap, miLoadMap, miSaveMap, miQuit);

        var menuMap = new Menu("Map");

        var miClearTerrain = new MenuItem("Clear Terrain");
        miClearTerrain.setOnAction(e -> map.terrain().clear());

        var miClearFood = new MenuItem("Clear Food");
        miClearFood.setOnAction(e -> map.food().clear());

        var miAddHouse = new MenuItem("Add House");
        miAddHouse.setOnAction(e -> addShape(GHOST_HOUSE_SHAPE, 15, 10));

        Menu menuLoadArcadeMap = new Menu("Load Arcade Map");

        var miLoadPacManMap = new MenuItem("Pac-Man");
        miLoadPacManMap.setOnAction(e -> loadMap(arcadeMaps[0]));
        menuLoadArcadeMap.getItems().add(miLoadPacManMap);

        IntStream.range(1, 7).forEach(i -> {
            var miLoadMsPacManMap = new MenuItem("Ms. Pac-Man " + i);
            miLoadMsPacManMap.setOnAction(e -> loadMap(arcadeMaps[i]));
            menuLoadArcadeMap.getItems().add(miLoadMsPacManMap);
        });

        menuMap.getItems().addAll(miClearTerrain, miClearFood, miAddHouse, menuLoadArcadeMap);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuFile, menuMap);

        openDialog = new FileChooser();
        openDialog.setInitialDirectory(lastUsedDir);

        canvas = new Canvas();
        canvas.setOnMouseClicked(this::onMouseClickedOnCanvas);
        canvas.setOnMouseMoved(this::onMouseMovedOverCanvas);

        terrainMapPropertiesEditor = new TextArea();
        terrainMapPropertiesEditor.setPrefSize(150, 150);

        foodMapPropertiesEditor = new TextArea();
        foodMapPropertiesEditor.setPrefSize(150, 150);

        var cbTerrainVisible = new CheckBox("Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox("Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox("Grid");
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        var cbRuntimePreview = new CheckBox("Runtime View");
        cbRuntimePreview.selectedProperty().bindBidirectional(runtimePreviewPy);

        terrainPalette = new Palette(32, 4, 4, Tiles.TERRAIN_TILES_END);
        terrainPalette.setValues(
            Tiles.EMPTY, Tiles.TUNNEL, Tiles.EMPTY, Tiles.EMPTY,
            Tiles.WALL_H, Tiles.WALL_V, Tiles.DWALL_H, Tiles.DWALL_V,
            Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE,
            Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE
        );

        foodPalette = new Palette(32, 4, 4, Tiles.FOOD_TILES_END);
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
        controlsPane.setSpacing(10);
        controlsPane.getChildren().add(cbRuntimePreview);
        controlsPane.getChildren().add(new HBox(20, new Label("Show"), cbTerrainVisible, cbFoodVisible, cbGridVisible));
        controlsPane.getChildren().add(infoLabel);
        controlsPane.getChildren().add(tabPane);
        controlsPane.getChildren().add(new VBox(new Text("Terrain Map"), terrainMapPropertiesEditor));
        controlsPane.getChildren().add(new VBox(new Text("Food Map"), foodMapPropertiesEditor));

        var contentPane = new BorderPane();

        contentPane.setTop(menuBar);

        var scroll = new ScrollPane(canvas);
        scroll.setFitToHeight(true);

        var hbox = new HBox(scroll, controlsPane);
        hbox.setSpacing(10);
        contentPane.setCenter(hbox);

        return contentPane;
    }

    // TODO use own canvas or Text control
    void drawBlueScreen(Exception drawException) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLUE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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

    void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(g);
        if (terrainVisiblePy.get()) {
            terrainMapRenderer.setScaling(tilePx() / 8);
            terrainMapRenderer.drawMap(g, map.terrain());
            terrainMapRenderer.runtimePreview = runtimePreviewPy.get();
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(tilePx() / 8);
            foodMapRenderer.drawMap(g, map.food());
        }
        double t1 = tilePx();
        if (hoveredTile != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            g.strokeRect(hoveredTile.x() * t1, hoveredTile.y() * t1, t1, t1);
        }
        if (terrainEditedPy.get()) {
            terrainPalette.draw();
        } else {
            foodPalette.draw();
        }
    }

    void drawGrid(GraphicsContext g) {
        if (gridVisiblePy.get() && !runtimePreviewPy.get()) {
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.25);
            double gridSize = tilePx();
            for (int row = 1; row < map.terrain().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, canvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < map.terrain().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, canvas.getHeight());
            }
        }
    }

    void addShape(byte[][] shape, int topLeftRow, int topLeftCol) {
        if (map.terrain() != null) {
            for (int row = 0; row < shape.length; ++row) {
                for (int col = 0; col < shape[0].length; ++col) {
                    map.terrain().set(topLeftRow + row, topLeftCol+ col, shape[row][col]);
                }
            }
        }
    }

    void loadMap(WorldMap other) {
        map = WorldMap.copyOf(other);
        currentMapFile = null;
        foodMapPropertiesEditor.setText(map.food().getPropertiesAsText());
        terrainMapPropertiesEditor.setText(map.terrain().getPropertiesAsText());
        terrainMapRenderer.setWallStrokeColor(getColorFromMap(map.terrain(), "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
        terrainMapRenderer.setWallFillColor(getColorFromMap(map.terrain(), "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
        terrainMapRenderer.setDoorColor(getColorFromMap(map.terrain(), "door_color", DEFAULT_DOOR_COLOR));
        Color foodColor = getColorFromMap(map.food(), "food_color", DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(foodColor);
        foodMapRenderer.setPelletColor(foodColor);
        updateInfo();
    }

    /**
     * @return pixels used by one tile at current window zoom
     */
    double tilePx() {
        return canvas.getHeight() / map.numRows();
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
            stage.setTitle("Map Editor: " + currentMapFile.getPath());
        } else {
            stage.setTitle("Map Editor");
        }
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
                map = new WorldMap(new TileMap(numRows, numCols), new TileMap(numCols, numRows));
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    void openMapFile() {
        openDialog.setInitialDirectory(lastUsedDir);
        openDialog.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Word Map Files", ".world"));
        File file = openDialog.showOpenDialog(stage);
        if (file != null) {
            readMapFile(file);
            updateInfo();
        }
    }

    void readMapFile(File file) {
        if (file.getName().endsWith(".world")) {
            try {
                var r = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                loadMap(new WorldMap(r.lines()));
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

    void saveMap() {
        openDialog.setInitialDirectory(lastUsedDir);
        openDialog.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("World Map Files", ".world"));
        File file = openDialog.showSaveDialog(stage);
        if (file != null) {
            lastUsedDir = file.getParentFile();
            if (file.getName().endsWith(".world")) {
                map.food().setPropertiesFromText(foodMapPropertiesEditor.getText());
                map.terrain().setPropertiesFromText(terrainMapPropertiesEditor.getText());
                map.save(file);
                readMapFile(file);
                updateInfo();
            } else {
                Logger.error("No .world file selected"); //TODO
            }
        }
    }

    static class Palette extends Canvas {

        int numRows;
        int numCols;
        int gridSize;
        byte[] valueAtIndex;
        GraphicsContext g = getGraphicsContext2D();
        TileMapRenderer renderer;
        byte selectedValue;
        int selectedValueRow;
        int selectedValueCol;

        Palette(int gridSize, int numRows, int numCols, byte valueEnd) {
            this.gridSize = gridSize;
            this.numRows = numRows;
            this.numCols = numCols;
            this.valueAtIndex = new byte[numRows*numCols];
            for (int i = 0; i < valueAtIndex.length; ++i) {
                valueAtIndex[i] = i < valueEnd ? (byte) i : Tiles.EMPTY;
            }
            selectedValue = 0;
            selectedValueRow = -1;
            selectedValueCol = -1;
            setWidth(numCols * gridSize);
            setHeight(numRows * gridSize);
            setOnMouseClicked(e -> selectedValue = pickValue(e));
        }

        public void setRenderer(TileMapRenderer renderer) {
            this.renderer = renderer;
        }

        void setValues(byte... values) {
            for (int i = 0; i < values.length; ++i) {
                if (i < valueAtIndex.length) {
                    valueAtIndex[i] = values[i];
                }
            }
        }

        byte pickValue(MouseEvent e) {
            selectedValueRow = (int) e.getY() / gridSize;
            selectedValueCol = (int) e.getX() / gridSize;
            return valueAtIndex[selectedValueRow * numCols + selectedValueCol];
        }

        void draw() {
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (renderer != null) {
                renderer.setScaling((float) gridSize / 8f);
                for (int i = 0; i < numRows * numCols; ++i) {
                    int row = i / numCols, col = i % numCols;
                    renderer.drawTile(g, new Vector2i(col, row), valueAtIndex[i]);
                }
            }
            // Grid lines
            g.setStroke(Color.LIGHTGRAY);
            g.setLineWidth(0.75);
            for (int row = 1; row < numRows; ++row) {
                g.strokeLine(0, row * gridSize, getWidth(), row * gridSize);
            }
            for (int col = 1; col < numCols; ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, getHeight());
            }
            // mark selected cell
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            if (selectedValueRow != -1 && selectedValueCol != -1) {
                g.strokeRect(selectedValueCol * gridSize, selectedValueRow * gridSize, gridSize, gridSize);
            }
        }
    }
}
