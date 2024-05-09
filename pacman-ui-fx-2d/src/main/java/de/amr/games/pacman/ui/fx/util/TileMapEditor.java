/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

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

    Stage stage;
    Scene scene;
    MenuBar menuBar;
    Canvas canvas;
    TextArea terrainMapPropertiesEditor;
    TextArea foodMapPropertiesEditor;
    Label infoLabel;
    FileChooser openDialog;
    Palette terrainPalette;
    Palette foodPalette;
    VBox paletteContainer = new VBox();

    TerrainMapRenderer terrainMapRenderer;
    FoodMapRenderer foodMapRenderer;

    TileMap terrainMap;
    TileMap foodMap;

    Vector2i hoveredTile;
    File lastUsedDir = new File(System.getProperty("user.dir"));
    File terrainMapFile;

    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            boolean terrainEdited = get();
            if (terrainEdited) {
                paletteContainer.getChildren().setAll(terrainPalette);
            } else {
                paletteContainer.getChildren().setAll(foodPalette);
            }
            foodPalette.setVisible(!terrainEdited);
            updateInfo();
        }
    };
    BooleanProperty gridVisiblePy = new SimpleBooleanProperty(true);

    World pacManWorld    = GameVariants.PACMAN.createWorld(1);
    World msPacManWorld1 = GameVariants.MS_PACMAN.createWorld(1);
    World msPacManWorld2 = GameVariants.MS_PACMAN.createWorld(2);
    World msPacManWorld3 = GameVariants.MS_PACMAN.createWorld(3);
    World msPacManWorld4 = GameVariants.MS_PACMAN.createWorld(4);

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
        terrainMapRenderer.setWallColor(Color.GREEN);

        foodMapRenderer = new FoodMapRenderer();

        double height = Math.max(0.85 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        scene = new Scene(createSceneContent(), 950, height);
        scene.setFill(Color.BLACK);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.T) {
                terrainEditedPy.set(true);
            }
            else if (e.getCode() == KeyCode.F) {
                terrainEditedPy.set(false);
            }
        });

        // load initial maps
        loadMapsFromWorld(pacManWorld);

        updateInfo();

        canvas.heightProperty().bind(scene.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() * terrainMap.numCols() / terrainMap.numRows(), canvas.heightProperty()));

        stage.setScene(scene);
        stage.setTitle("Map Editor");
        stage.show();

        GameClockFX clock = new GameClockFX();
        clock.setTargetFrameRate(30);
        clock.setContinousCallback(this::draw);
        clock.start();
    }

    void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (terrainVisiblePy.get()) {
            terrainMapRenderer.setScaling(scaling());
            terrainMapRenderer.drawMap(g, terrainMap);
        }
        if (foodVisiblePy.get()) {
            foodMapRenderer.setScaling(scaling());
            foodMapRenderer.drawMap(g, foodMap);
        }
        if (gridVisiblePy.get()) {
            for (int row = 0; row < terrainMap.numRows(); ++row) {
                for (int col = 0; col < terrainMap.numCols(); ++col) {
                    if (hoveredTile != null && hoveredTile.x() == col && hoveredTile.y() == row) {
                        g.setStroke(Color.YELLOW);
                        g.setLineWidth(1);
                    } else {
                        g.setStroke(Color.GRAY);
                        g.setLineWidth(0.5);
                    }
                    double s8 = 8 * scaling();
                    g.strokeRect(col * s8, row * s8, s8, s8);
                }
            }
        }
        if (terrainEditedPy.get()) {
            terrainPalette.draw();
        } else {
            foodPalette.draw();
        }
    }

    Parent createSceneContent() {
        canvas = new Canvas();
        canvas.setOnMouseClicked(this::onMouseClickedOnCanvas);
        canvas.setOnMouseMoved(this::onMouseMovedOverCanvas);

        infoLabel = new Label();

        terrainMapPropertiesEditor = new TextArea();
        terrainMapPropertiesEditor.setPrefSize(220, 150);

        foodMapPropertiesEditor = new TextArea();
        foodMapPropertiesEditor.setPrefSize(220, 100);

        var cbTerrainVisible = new CheckBox("Show Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        var cbFoodVisible = new CheckBox("Show Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        var cbGridVisible = new CheckBox("Show Grid");
        cbGridVisible.selectedProperty().bindBidirectional(gridVisiblePy);

        var cbTerrainEdited = new CheckBox("Edit Terrain");
        cbTerrainEdited.selectedProperty().bindBidirectional(terrainEditedPy);
        cbTerrainEdited.setOnAction(e -> updateInfo());

        terrainPalette = new Palette(32, 4, 4, terrainMapRenderer, Tiles.TERRAIN_TILES_END);
        terrainPalette.setValuesAtIndex(
            Tiles.EMPTY, Tiles.TUNNEL, Tiles.EMPTY, Tiles.EMPTY,
            Tiles.WALL_H, Tiles.WALL_V, Tiles.DWALL_H, Tiles.DWALL_V,
            Tiles.CORNER_NW, Tiles.CORNER_NE, Tiles.CORNER_SW, Tiles.CORNER_SE,
            Tiles.DCORNER_NW, Tiles.DCORNER_NE, Tiles.DCORNER_SW, Tiles.DCORNER_SE
        );

        foodPalette = new Palette(32, 1, 4, foodMapRenderer, Tiles.FOOD_TILES_END);
        foodPalette.setValuesAtIndex(
            Tiles.EMPTY, Tiles.PELLET, Tiles.ENERGIZER, Tiles.EMPTY
        );

        paletteContainer = new VBox(terrainEditedPy.get() ? terrainPalette : foodPalette);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(createFileMenu(), createMapsMenu());

        GridPane controlsContainer = new GridPane();
        controlsContainer.setPrefWidth(350);
        int row = 0;
        controlsContainer.add(cbTerrainVisible,         0, row++);
        controlsContainer.add(cbFoodVisible,            0, row++);
        controlsContainer.add(cbGridVisible,            0, row++);
        controlsContainer.add(new Label(),              0, row++);
        controlsContainer.add(new Text("Terrain Map"),  0, row++);
        controlsContainer.add(terrainMapPropertiesEditor,  0, row++);
        controlsContainer.add(new Text("Food Map"),     0, row++);
        controlsContainer.add(foodMapPropertiesEditor,     0, row++);
        controlsContainer.add(new Label(),              0, row++);
        controlsContainer.add(cbTerrainEdited,          0, row++);
        controlsContainer.add(paletteContainer,         0, row++);
        controlsContainer.add(infoLabel,                0, row++);

        var contentPane = new BorderPane();
        contentPane.setTop(menuBar);

        var hbox = new HBox();
        hbox.setPadding(new Insets(5));
        hbox.setSpacing(20);
        contentPane.setLeft(hbox);

        hbox.getChildren().addAll(canvas, controlsContainer);

        return contentPane;
    }

    Menu createFileMenu() {
        openDialog = new FileChooser();
        openDialog.setInitialDirectory(lastUsedDir);

        var loadMapsItem = new MenuItem("Load Map...");
        loadMapsItem.setOnAction(e -> openMapFiles());

        var saveMapsItem = new MenuItem("Save Map...");
        saveMapsItem.setOnAction(e -> saveMaps());

        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());

        var menu = new Menu("File");
        menu.getItems().addAll(loadMapsItem, saveMapsItem, quitItem);

        return menu;
    }

    Menu createMapsMenu() {
        var clearMapItem = new MenuItem("Clear");
        clearMapItem.setOnAction(e -> clearMap());

        var addHouseItem = new MenuItem("Add House");
        addHouseItem.setOnAction(e -> addTerrainShape(15, 10, GHOST_HOUSE_SHAPE));

        Menu loadPredefinedMapMenu = new Menu("Load Predefined Map");

        var pacManWorldItem = new MenuItem("Pac-Man");
        pacManWorldItem.setOnAction(e -> loadMapsFromWorld(pacManWorld));

        var msPacManWorldItem1 = new MenuItem("Ms. Pac-Man 1");
        msPacManWorldItem1.setOnAction(e -> loadMapsFromWorld(msPacManWorld1));

        var msPacManWorldItem2 = new MenuItem("Ms. Pac-Man 2");
        msPacManWorldItem2.setOnAction(e -> loadMapsFromWorld(msPacManWorld2));

        var msPacManWorldItem3 = new MenuItem("Ms. Pac-Man 3");
        msPacManWorldItem3.setOnAction(e -> loadMapsFromWorld(msPacManWorld3));

        var msPacManWorldItem4 = new MenuItem("Ms. Pac-Man 4");
        msPacManWorldItem4.setOnAction(e -> loadMapsFromWorld(msPacManWorld4));

        loadPredefinedMapMenu.getItems().addAll(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2,
            msPacManWorldItem3, msPacManWorldItem4);

        var menu = new Menu("Map");
        menu.getItems().addAll(clearMapItem, addHouseItem, loadPredefinedMapMenu);

        return menu;
    }

    void loadMapsFromWorld(World world) {
        copyMapsFromWorld(world);
        foodMapPropertiesEditor.setText(foodMap.getPropertiesAsText());
        terrainMapPropertiesEditor.setText(terrainMap.getPropertiesAsText());
        updateInfo();
    }

    void copyMapsFromWorld(World world) {
        terrainMap = new TileMap(world.terrainMap());
        setTerrainColorsFromMap();
        foodMap    = new TileMap(world.foodMap());
        setFoodColorsFromMap();
        terrainMapFile = null;
    }

    double scaling() {
        return canvas.getHeight() / (terrainMap.numRows() * 8);
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
                terrainMap.set(hoveredTile, terrainPalette.selectedValue);
            } else {
                foodMap.set(hoveredTile, foodPalette.selectedValue);
            }
        }
    }

    void editTerrainTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            terrainMap.set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = terrainMap.get(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            terrainMap.set(tile, nextValue);
            updateInfo();
        }
        else {
            terrainMap.set(tile, terrainPalette.selectedValue);
            updateInfo();
        }
    }

    void editFoodTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            foodMap.set(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = foodMap.get(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            foodMap.set(tile, newValue);
            updateInfo();
        }
        else {
            foodMap.set(tile, foodPalette.selectedValue);
            updateInfo();
        }
    }

    void updateInfo() {
        var text = "Tile: ";
        text += hoveredTile != null ? String.format("x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "";
        infoLabel.setText(text);

        if (terrainMapFile != null) {
            stage.setTitle("Map Editor: " + terrainMapFile.getPath());
        } else {
            stage.setTitle("Map Editor");
        }
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
            try {
                foodMap = TileMap.fromURL(foodMapFile.toURI().toURL(), Tiles.FOOD_TILES_END);
                foodMapPropertiesEditor.setText(foodMap.getPropertiesAsText());
                lastUsedDir = foodMapFile.getParentFile();
            } catch (MalformedURLException x) {
                Logger.error("Could not load food map from file {}", foodMapFile);
                Logger.error(x);
            }
            terrainMapFile = new File(basePath + ".terrain");
            try {
                terrainMap = TileMap.fromURL(terrainMapFile.toURI().toURL(), Tiles.TERRAIN_TILES_END);
                setTerrainColorsFromMap();
                terrainMapPropertiesEditor.setText(terrainMap.getPropertiesAsText());
                lastUsedDir = terrainMapFile.getParentFile();
            } catch (MalformedURLException x) {
                Logger.error("Could not load terrain map from file {}", terrainMapFile);
                Logger.error(x);
            }
        }
        updateInfo();
    }

    void setTerrainColorsFromMap() {
        if (terrainMap.getProperty("wall_color") != null) {
            terrainMapRenderer.setWallColor(parseColor(terrainMap.getProperty("wall_color")));
        }
    }

    void setFoodColorsFromMap() {
        if (foodMap.getProperty("food_color") != null) {
            Color foodColor = parseColor(foodMap.getProperty("food_color")) ;
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
        }
    }

    Color parseColor(String colorSpec) {
        try {
            return Color.web(colorSpec);
        } catch (Exception x) {
            Logger.error("Could not parse color from spec '{}'", colorSpec);
            return Color.WHITE;
        }
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
            foodMap.setPropertiesFromText(foodMapPropertiesEditor.getText());
            saveMap(foodMap, new File(basePath + ".food"));
            terrainMap.setPropertiesFromText(terrainMapPropertiesEditor.getText());
            saveMap(terrainMap, new File(basePath + ".terrain"));
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

    void clearMap() {
        if (terrainEditedPy.get()) {
            terrainMap.clear();
        } else {
            foodMap.clear();
        }
    }

    void addTerrainShape(int row, int col, byte[][] shape) {
        for (int r = 0; r < shape.length; ++r) {
            for (int c = 0; c < shape[0].length; ++c) {
                terrainMap.set(row + r, col + c, shape[r][c]);
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

        void setValuesAtIndex(byte... values) {
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
