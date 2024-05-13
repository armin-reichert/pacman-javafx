/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapMaze;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

import static de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer.getTileMapColor;

/**
 * @author Armin Reichert
 */
public class TileMapEditor extends Application  {

    public static void main(String[] args) {
        launch();
    }

    record MapCollection(TileMap terrainMap, TileMap foodMap) {
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

    TerrainMapRenderer terrainMapRenderer;
    FoodMapRenderer foodMapRenderer;

    MapCollection mapCollection;

    Vector2i hoveredTile;
    File lastUsedDir = new File(System.getProperty("user.dir"));
    File terrainMapFile;

    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true);
    BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);

    MapCollection mcPacMan;
    MapCollection mcMsPacMan1;
    MapCollection mcMsPacMan2;
    MapCollection mcMsPacMan3;
    MapCollection mcMsPacMan4;
    MapCollection mcMsPacMan5;
    MapCollection mcMsPacMan6;

    private void loadPredefinedMaps() {
        var pacMan    = GameVariant.PACMAN.createWorld(new MapMaze(1, 1));
        var msPacMan1 = GameVariant.MS_PACMAN.createWorld(new MapMaze(1, 1));
        var msPacMan2 = GameVariant.MS_PACMAN.createWorld(new MapMaze(2, 2));
        var msPacMan3 = GameVariant.MS_PACMAN.createWorld(new MapMaze(3, 3));
        var msPacMan4 = GameVariant.MS_PACMAN.createWorld(new MapMaze(4, 4));
        var msPacMan5 = GameVariant.MS_PACMAN.createWorld(new MapMaze(3, 5));
        var msPacMan6 = GameVariant.MS_PACMAN.createWorld(new MapMaze(4, 6));

        mcPacMan    = new MapCollection(pacMan.terrainMap(), pacMan.foodMap());
        mcMsPacMan1 = new MapCollection(msPacMan1.terrainMap(), msPacMan1.foodMap());
        mcMsPacMan2 = new MapCollection(msPacMan2.terrainMap(), msPacMan2.foodMap());
        mcMsPacMan3 = new MapCollection(msPacMan3.terrainMap(), msPacMan3.foodMap());
        mcMsPacMan4 = new MapCollection(msPacMan4.terrainMap(), msPacMan4.foodMap());
        mcMsPacMan5 = new MapCollection(msPacMan5.terrainMap(), msPacMan5.foodMap());
        mcMsPacMan6 = new MapCollection(msPacMan6.terrainMap(), msPacMan6.foodMap());
    }

    @Override
    public void init() throws Exception {
        try {
            loadPredefinedMaps();
            mapCollection = new MapCollection(
                new TileMap(DEFAULT_NUM_ROWS, DEFAULT_NUM_COLS),
                new TileMap(DEFAULT_NUM_ROWS, DEFAULT_NUM_COLS));
        } catch (Exception x) {
            x.printStackTrace(System.err);
            Platform.exit();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        terrainMapRenderer = new TerrainMapRenderer() {
            @Override
            public void drawTunnel(GraphicsContext g, Vector2i tile) {
                g.setFill(Color.GRAY);
                g.fillRect(tile.x() * s(8), tile.y() * s(8), s(8), s(8));
            }
        };
        terrainMapRenderer.setWallStrokeColor(DEFAULT_WALL_STROKE_COLOR);
        terrainMapRenderer.setWallFillColor(DEFAULT_WALL_FILL_COLOR);

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setPelletColor(DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(DEFAULT_FOOD_COLOR);

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        sceneContent = createSceneContent();

        scene = new Scene(sceneContent, 1050, height);
        scene.setFill(Color.BLACK);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.T) {
                terrainEditedPy.set(true);
            }
            else if (e.getCode() == KeyCode.F) {
                terrainEditedPy.set(false);
            }
        });

        canvas.heightProperty().bind(sceneContent.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() * mapCollection.terrainMap().numCols()/ mapCollection.terrainMap().numRows(), canvas.heightProperty()));

        stage.setScene(scene);
        stage.setTitle("Map Editor");
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
        if (mapCollection.terrainMap() != null && terrainVisiblePy.get()) {
            terrainMapRenderer.setScaling(scaling());
            terrainMapRenderer.drawMap(g, mapCollection.terrainMap());
        }
        if (mapCollection.foodMap() != null && foodVisiblePy.get()) {
            foodMapRenderer.setScaling(scaling());
            foodMapRenderer.drawMap(g, mapCollection.foodMap());
        }
        if (hoveredTile != null) {
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            double s8 = 8 * scaling();
            g.strokeRect(hoveredTile.x() * s8, hoveredTile.y() * s8, s8, s8);
        }
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
            double gridSize = 8 * scaling();
            for (int row = 1; row < mapCollection.terrainMap().numRows(); ++row) {
                g.strokeLine(0, row * gridSize, canvas.getWidth(), row * gridSize);
            }
            for (int col = 1; col < mapCollection.terrainMap().numCols(); ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, canvas.getHeight());
            }
        }
    }

    Pane createSceneContent() {
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(createFileMenu(), createMapsMenu());

        canvas = new Canvas();
        canvas.setOnMouseClicked(this::onMouseClickedOnCanvas);
        canvas.setOnMouseMoved(this::onMouseMovedOverCanvas);

        terrainMapPropertiesEditor = new TextArea();
        terrainMapPropertiesEditor.setPrefSize(220, 200);

        foodMapPropertiesEditor = new TextArea();
        foodMapPropertiesEditor.setPrefSize(220, 200);

        var cbTerrainVisible = new CheckBox("Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox("Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox("Grid");
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        terrainPalette = new Palette(32, 4, 4, terrainMapRenderer, Tiles.TERRAIN_TILES_END);
        terrainPalette.setValues(
            Tiles.EMPTY, Tiles.TUNNEL, Tiles.EMPTY, Tiles.EMPTY,
            Tiles.WALL_H, Tiles.WALL_V, Tiles.DWALL_H, Tiles.DWALL_V,
            Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE,
            Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE
        );

        foodPalette = new Palette(32, 4, 4, foodMapRenderer, Tiles.FOOD_TILES_END);
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
        controlsPane.setPrefWidth(350);
        controlsPane.getChildren().add(new HBox(20, new Label("Show"), cbTerrainVisible, cbFoodVisible, cbGridVisible));
        controlsPane.getChildren().add(infoLabel);
        controlsPane.getChildren().add(tabPane);
        controlsPane.getChildren().add(new VBox(new Text("Terrain Map"), terrainMapPropertiesEditor));
        controlsPane.getChildren().add(new VBox(new Text("Food Map"), foodMapPropertiesEditor));

        var contentPane = new BorderPane();

        contentPane.setTop(menuBar);
        var scroll = new ScrollPane(canvas);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(true);
        contentPane.setCenter(scroll);
        contentPane.setRight(controlsPane);

        return contentPane;
    }

    Menu createFileMenu() {
        openDialog = new FileChooser();
        openDialog.setInitialDirectory(lastUsedDir);

        var newMapsItem = new MenuItem("New...");
        newMapsItem.setOnAction(e -> createNewMap());

        var loadMapsItem = new MenuItem("Load Map...");
        loadMapsItem.setOnAction(e -> openMapFiles());

        var saveMapsItem = new MenuItem("Save Map...");
        saveMapsItem.setOnAction(e -> saveMaps());

        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());

        var menu = new Menu("File");
        menu.getItems().addAll(newMapsItem, loadMapsItem, saveMapsItem, quitItem);

        return menu;
    }

    Menu createMapsMenu() {
        var clearTerrainMapItem = new MenuItem("Clear Terrain");
        clearTerrainMapItem.setOnAction(e -> {
            if (mapCollection.terrainMap() != null) {
                mapCollection.terrainMap().clear();
            }
        });

        var clearFoodMapItem = new MenuItem("Clear Food");
        clearFoodMapItem.setOnAction(e -> {
            if (mapCollection.foodMap() != null) {
                mapCollection.foodMap().clear();
            }
        });

        var addHouseItem = new MenuItem("Add House");
        addHouseItem.setOnAction(e -> addShape(GHOST_HOUSE_SHAPE, 15, 10));

        Menu loadPredefinedMapMenu = new Menu("Load Predefined");

        var pacManWorldItem = new MenuItem("Pac-Man");
        pacManWorldItem.setOnAction(e -> loadPredefined(mcPacMan));

        var msPacManItem1 = new MenuItem("Ms. Pac-Man 1");
        msPacManItem1.setOnAction(e -> loadPredefined(mcMsPacMan1));

        var msPacManItem2 = new MenuItem("Ms. Pac-Man 2");
        msPacManItem2.setOnAction(e -> loadPredefined(mcMsPacMan2));

        var msPacManItem3 = new MenuItem("Ms. Pac-Man 3");
        msPacManItem3.setOnAction(e -> loadPredefined(mcMsPacMan3));

        var msPacManItem4 = new MenuItem("Ms. Pac-Man 4");
        msPacManItem4.setOnAction(e -> loadPredefined(mcMsPacMan4));

        var msPacManItem5 = new MenuItem("Ms. Pac-Man 5");
        msPacManItem5.setOnAction(e -> {
            loadPredefined(mcMsPacMan5);
            terrainMapRenderer.setWallStrokeColor(Color.web(mapCollection.terrainMap().getProperty("wall_stroke_color.5")));
            terrainMapRenderer.setWallFillColor(Color.web(mapCollection.terrainMap().getProperty("wall_fill_color.5")));
            foodMapRenderer.setEnergizerColor(Color.web(mapCollection.foodMap.getProperty("food_color.5")));
            foodMapRenderer.setPelletColor(Color.web(mapCollection.foodMap.getProperty("food_color.5")));
        });

        var msPacManItem6 = new MenuItem("Ms. Pac-Man 6");
        msPacManItem6.setOnAction(e -> {
            loadPredefined(mcMsPacMan6);
            terrainMapRenderer.setWallStrokeColor(Color.web(mapCollection.terrainMap().getProperty("wall_stroke_color.6")));
            terrainMapRenderer.setWallFillColor(Color.web(mapCollection.terrainMap().getProperty("wall_fill_color.6")));
            foodMapRenderer.setEnergizerColor(Color.web(mapCollection.foodMap.getProperty("food_color.6")));
            foodMapRenderer.setPelletColor(Color.web(mapCollection.foodMap.getProperty("food_color.6")));
        });

        loadPredefinedMapMenu.getItems().addAll(pacManWorldItem, msPacManItem1, msPacManItem2,
            msPacManItem3, msPacManItem4, msPacManItem5, msPacManItem6);

        var menu = new Menu("Map");
        menu.getItems().addAll(clearTerrainMapItem, clearFoodMapItem, addHouseItem, loadPredefinedMapMenu);

        return menu;
    }

    void addShape(byte[][] shape, int topLeftRow, int topLeftCol) {
        if (mapCollection.terrainMap() != null) {
            for (int row = 0; row < shape.length; ++row) {
                for (int col = 0; col < shape[0].length; ++col) {
                    mapCollection.terrainMap().set(topLeftRow + row, topLeftCol+ col, shape[row][col]);
                }
            }
        }
    }

    void loadPredefined(MapCollection predefinedMaps) {
        terrainMapFile = null;
        mapCollection = new MapCollection(TileMap.copy(predefinedMaps.terrainMap()), TileMap.copy(predefinedMaps.foodMap()));
        setTerrainColorsFromMap(predefinedMaps.terrainMap());
        terrainMapPropertiesEditor.setText(predefinedMaps.terrainMap().getPropertiesAsText());
        setFoodColorsFromMap(predefinedMaps.foodMap());
        foodMapPropertiesEditor.setText(predefinedMaps.foodMap().getPropertiesAsText());
        updateInfo();
    }

    void setTerrainColorsFromMap(TileMap terrainMap) {
        terrainMapRenderer.setWallStrokeColor(getTileMapColor(terrainMap, "wall_stroke_color", DEFAULT_WALL_STROKE_COLOR));
        terrainMapRenderer.setWallFillColor(getTileMapColor(terrainMap, "wall_fill_color", DEFAULT_WALL_FILL_COLOR));
    }

    void setFoodColorsFromMap(TileMap foodMap) {
        Color foodColor = getTileMapColor(foodMap, "food_color", DEFAULT_FOOD_COLOR);
        foodMapRenderer.setEnergizerColor(foodColor);
        foodMapRenderer.setPelletColor(foodColor);
    }

    double scaling() {
        return canvas.getHeight() / (mapCollection.terrainMap().numRows() * 8);
    }

    int viewToTile(double viewLength) {
        return (int) (viewLength / (8 * scaling()));
    }

    void onMouseClickedOnCanvas(MouseEvent e) {
        if (terrainEditedPy.get()) {
            editTerrainTile(e);
        } else {
            editFoodTile(e);
        }
    }

    void onMouseMovedOverCanvas(MouseEvent e) {
        hoveredTile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        updateInfo();
        if (e.isShiftDown()) {
            if (terrainEditedPy.get()) {
                mapCollection.terrainMap().set(hoveredTile, terrainPalette.selectedValue);
            } else {
                mapCollection.foodMap().set(hoveredTile, foodPalette.selectedValue);
            }
        }
    }

    void editTerrainTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            mapCollection.terrainMap().set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = mapCollection.terrainMap().get(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            mapCollection.terrainMap().set(tile, nextValue);
            updateInfo();
        }
        else {
            mapCollection.terrainMap().set(tile, terrainPalette.selectedValue);
            updateInfo();
        }
    }

    void editFoodTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            mapCollection.foodMap().set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = mapCollection.foodMap().get(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            mapCollection.foodMap().set(tile, newValue);
            updateInfo();
        }
        else {
            mapCollection.foodMap().set(tile, foodPalette.selectedValue);
            updateInfo();
        }
    }

    void updateInfo() {
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "n/a";
        infoLabel.setText(text);

        if (terrainMapFile != null) {
            stage.setTitle("Map Editor: " + terrainMapFile.getPath());
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
                mapCollection = new MapCollection(new TileMap(numRows, numCols), new TileMap(numCols, numRows));
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    void openMapFiles() {
        openDialog.setInitialDirectory(lastUsedDir);
        File file = openDialog.showOpenDialog(stage);
        if (file != null) {
            loadMapFiles(file);
        }
    }

    void loadMapFiles(File anyMapFile) {
        if (anyMapFile.getName().endsWith(".terrain") || anyMapFile.getName().endsWith(".food")) {
            int lastDot = anyMapFile.getPath().lastIndexOf('.');
            String basePath = anyMapFile.getPath().substring(0, lastDot);
            File foodMapFile = new File(basePath + ".food");
            TileMap foodMap = null;
            TileMap terrainMap = null;
            try {
                foodMap = TileMap.fromURL(foodMapFile.toURI().toURL(), Tiles.FOOD_TILES_END);
                setFoodColorsFromMap(foodMap);
                foodMapPropertiesEditor.setText(foodMap.getPropertiesAsText());
                lastUsedDir = foodMapFile.getParentFile();
            } catch (Exception x) {
                Logger.error("Could not load food map from file {}", foodMapFile);
                Logger.error(x);
            }
            terrainMapFile = new File(basePath + ".terrain");
            try {
                terrainMap = TileMap.fromURL(terrainMapFile.toURI().toURL(), Tiles.TERRAIN_TILES_END);
                setTerrainColorsFromMap(terrainMap);
                terrainMapPropertiesEditor.setText(mapCollection.terrainMap().getPropertiesAsText());
                lastUsedDir = terrainMapFile.getParentFile();
            } catch (MalformedURLException x) {
                Logger.error("Could not load terrain map from file {}", terrainMapFile);
                Logger.error(x);
            }
            mapCollection = new MapCollection(terrainMap, foodMap);
        }
        updateInfo();
    }

    void saveMaps() {
        openDialog.setInitialDirectory(lastUsedDir);
        File file = openDialog.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        lastUsedDir = file.getParentFile();
        if (file.getName().endsWith(".terrain") || file.getName().endsWith(".food")) {
            int lastDot = file.getPath().lastIndexOf('.');
            String basePath = file.getPath().substring(0, lastDot);
            mapCollection.foodMap().setPropertiesFromText(foodMapPropertiesEditor.getText());
            saveMap(mapCollection.foodMap(), new File(basePath + ".food"));
            mapCollection.terrainMap().setPropertiesFromText(terrainMapPropertiesEditor.getText());
            saveMap(mapCollection.terrainMap(), new File(basePath + ".terrain"));
            // reload changes if any
            loadMapFiles(new File(basePath + ".terrain"));
        }
    }

    void saveMap(TileMap map, File file) {
        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            map.write(w);
            Logger.info("Tile map saved to file '{}'.", file);
        } catch (Exception x) {
            Logger.error(x);
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

        Palette(int gridSize, int numRows, int numCols, TileMapRenderer renderer, byte valueEnd) {
            this.gridSize = gridSize;
            this.numRows = numRows;
            this.numCols = numCols;
            this.valueAtIndex = new byte[numRows*numCols];
            this.renderer = renderer;
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
            renderer.setScaling(1f * gridSize / 8);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                renderer.drawTile(g, new Vector2i(col, row), valueAtIndex[i]);
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
