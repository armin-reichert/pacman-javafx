/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Globals;
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
    Label infoLabel;
    CheckBox cbTerrainVisible;
    CheckBox cbFoodVisible;
    CheckBox cbTerrainEdited;
    FileChooser openDialog;
    Palette terrainPalette;
    Palette foodPalette;

    TerrainMapRenderer terrainMapRenderer;
    FoodMapRenderer foodMapRenderer;

    TileMap terrainMap;
    TileMap foodMap;

    Vector2i hoveredTile;
    File lastUsedDir = new File(System.getProperty("user.dir"));

    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            boolean terrainEdited = get();
            terrainPalette.setVisible(terrainEdited);
            foodPalette.setVisible(!terrainEdited);
        }
    };

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

        // set initial maps
        copyMapsFromWorld(pacManWorld);

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        scene = new Scene(createSceneContent(), 850, height);
        scene.setFill(Color.BLACK);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.T) {
                terrainEditedPy.set(true);
                updateInfo();
            }
            else if (e.getCode() == KeyCode.F) {
                terrainEditedPy.set(false);
                updateInfo();
            }
        });
        updateInfo();

        canvas.heightProperty().bind(scene.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() * terrainMap.numCols() / terrainMap.numRows(), canvas.heightProperty()));

        stage.setScene(scene);
        stage.setTitle("Map Editor");
        stage.show();

        GameClockFX clock = new GameClockFX();
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

        cbTerrainVisible = new CheckBox("Show Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        cbFoodVisible = new CheckBox("Show Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        cbTerrainEdited = new CheckBox("Edit Terrain");
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
        StackPane paletteContainer = new StackPane(foodPalette, terrainPalette);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(createFileMenu(), createMapsMenu());


        GridPane controlsContainer = new GridPane();
        controlsContainer.setPrefWidth(350);
        controlsContainer.add(infoLabel,        0, 0);
        controlsContainer.add(cbTerrainVisible, 0, 1);
        controlsContainer.add(cbFoodVisible,    0, 2);
        controlsContainer.add(cbTerrainEdited,  0, 3);
        controlsContainer.add(paletteContainer,   0, 4);

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

        var loadMapsItem = new MenuItem("Load Maps...");
        loadMapsItem.setOnAction(e -> loadMaps());

        var saveMapsItem = new MenuItem("Save Maps...");
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
        pacManWorldItem.setOnAction(e -> copyMapsFromWorld(pacManWorld));

        var msPacManWorldItem1 = new MenuItem("Ms. Pac-Man 1");
        msPacManWorldItem1.setOnAction(e -> copyMapsFromWorld(msPacManWorld1));

        var msPacManWorldItem2 = new MenuItem("Ms. Pac-Man 2");
        msPacManWorldItem2.setOnAction(e -> copyMapsFromWorld(msPacManWorld2));

        var msPacManWorldItem3 = new MenuItem("Ms. Pac-Man 3");
        msPacManWorldItem3.setOnAction(e -> copyMapsFromWorld(msPacManWorld3));

        var msPacManWorldItem4 = new MenuItem("Ms. Pac-Man 4");
        msPacManWorldItem4.setOnAction(e -> copyMapsFromWorld(msPacManWorld4));

        loadPredefinedMapMenu.getItems().addAll(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2,
            msPacManWorldItem3, msPacManWorldItem4);

        var menu = new Menu("Map");
        menu.getItems().addAll(clearMapItem, addHouseItem, loadPredefinedMapMenu);

        return menu;
    }

    void copyMapsFromWorld(World world) {
        terrainMap = new TileMap(world.tileMap());
        setTerrainColorsFromMap();
        foodMap    = new TileMap(world.foodMap());
        setFoodColorsFromMap();
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
        text += "\n\n";
        text += terrainMap.getCommentSection();
        text += "\n";
        text += foodMap.getCommentSection();
        text += "\n\n";
        infoLabel.setText(text);
    }

    void loadMaps() {
        openDialog.setInitialDirectory(lastUsedDir);
        File file = openDialog.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        if (file.getName().endsWith(".terrain") || file.getName().endsWith(".food")) {
            int lastDot = file.getPath().lastIndexOf('.');
            String basePath = file.getPath().substring(0, lastDot);
            File foodMapFile = new File(basePath + ".food");
            try {
                foodMap = TileMap.fromURL(foodMapFile.toURI().toURL(), Tiles.FOOD_TILES_END);
                lastUsedDir = foodMapFile.getParentFile();
            } catch (MalformedURLException x) {
                Logger.error("Could not load food map from file {}", foodMapFile);
                Logger.error(x);
            }
            File terrainMapFile = new File(basePath + ".terrain");
            try {
                terrainMap = TileMap.fromURL(terrainMapFile.toURI().toURL(), Tiles.TERRAIN_TILES_END);
                setTerrainColorsFromMap();
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
            terrainMapRenderer.setWallColor(rgbToColor(terrainMap.getProperty("wall_color")));
        }
    }

    void setFoodColorsFromMap() {
        if (foodMap.getProperty("food_color") != null) {
            Color foodColor = rgbToColor(foodMap.getProperty("food_color")) ;
            foodMapRenderer.setEnergizerColor(foodColor);
            foodMapRenderer.setPelletColor(foodColor);
        }
    }

    Color rgbToColor(String rgb) {
        if (rgb.startsWith("rgb(") && rgb.endsWith(")")) {
            rgb = rgb.substring(4, rgb.length()-1);
            var colors = rgb.split(",");
            if (colors.length == 3) {
                try {
                    int r = Globals.clamp(Integer.parseInt(colors[0].trim()), 0, 255);
                    int g = Globals.clamp(Integer.parseInt(colors[1].trim()), 0, 255);
                    int b = Globals.clamp(Integer.parseInt(colors[2].trim()), 0, 255);
                    return Color.rgb(r,g,b);
                }
                catch (Exception x) {
                    Logger.error(x);
                    return Color.WHITE;
                }
            }
        }
        return Color.WHITE;
    }

    void saveMaps() {
        openDialog.setInitialDirectory(lastUsedDir);
        File file = openDialog.showSaveDialog(stage);
        if (file == null) {
            return;
        }
        if (file.getName().endsWith(".terrain") || file.getName().endsWith(".food")) {
            int lastDot = file.getPath().lastIndexOf('.');
            String basePath = file.getPath().substring(0, lastDot);
            File foodMapFile = new File(basePath + ".food");
            saveMap(foodMap, foodMapFile);
            File terrainMapFile = new File(basePath + ".terrain");
            saveMap(terrainMap, terrainMapFile);

        } else {
            Logger.error("No map file selected for saving");
        }
        lastUsedDir = file.getParentFile();

    }

    void saveMap(TileMap map, File file) {
        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write(map.getCommentSection());
            for (int row = 0; row < map.numRows(); ++row) {
                for (int col = 0; col < map.numCols(); ++col) {
                    String valueTxt = String.valueOf(map.get(row, col));
                    w.write(String.format("%2s", valueTxt));
                    if (col < map.numCols() - 1) {
                        w.write(",");
                    }
                }
                w.write("\n");
            }
            Logger.info("File '{}' saved.", file.getPath());
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

        Palette(int gridSize, int numRows, int numCols, TileMapRenderer renderer, byte valueEnd) {
            this.gridSize = gridSize;
            this.numRows = numRows;
            this.numCols = numCols;
            this.valueAtIndex = new byte[numRows*numCols];
            this.renderer = renderer;
            for (int i = 0; i < valueAtIndex.length; ++i) {
                valueAtIndex[i] = i < valueEnd ? (byte) i : Tiles.EMPTY;
            }
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
            int row = (int) e.getY() / gridSize;
            int col = (int) e.getX() / gridSize;
            return valueAtIndex[row * numCols + col];
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
            // mark selected value
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            int row = selectedValue / numCols;
            int col = selectedValue % numCols;
            g.strokeRect(col * gridSize, row * gridSize, gridSize, gridSize);
        }
    }
}
