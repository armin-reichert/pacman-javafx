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

    Stage stage;
    Scene scene;
    MenuBar menuBar;
    BorderPane contentPane;
    BorderPane canvasContainer;
    Canvas canvas;
    Label infoLabel;
    CheckBox cbTerrainVisible;
    CheckBox cbFoodVisible;
    CheckBox cbTerrainEdited;
    FileChooser openDialog;
    Palette palette;

    TerrainMapRenderer terrainMapRenderer;
    FoodMapRenderer foodMapRenderer;

    TileMap terrainMap;
    TileMap foodMap;

    byte selectedTerrainValue;
    byte selectedFoodValue;
    Vector2i hoveredTile;
    BooleanProperty terrainVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty foodVisiblePy = new SimpleBooleanProperty(true);
    BooleanProperty terrainEditedPy = new SimpleBooleanProperty(true);

    World pacManWorld    = GameVariants.PACMAN.createWorld(1);
    World msPacManWorld1 = GameVariants.MS_PACMAN.createWorld(1);
    World msPacManWorld2 = GameVariants.MS_PACMAN.createWorld(2);
    World msPacManWorld3 = GameVariants.MS_PACMAN.createWorld(3);
    World msPacManWorld4 = GameVariants.MS_PACMAN.createWorld(4);

    @Override
    public void init() throws Exception {
        copyMapsFromWorld(pacManWorld);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        terrainMapRenderer = new TerrainMapRenderer();
        terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));

        foodMapRenderer = new FoodMapRenderer();
        foodMapRenderer.setEnergizerColor(Color.rgb(254, 189, 180));
        foodMapRenderer.setPelletColor(Color.rgb(254, 189, 180));

        scene = new Scene(createSceneContent(), 800, 600);
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

        canvas.heightProperty().bind(scene.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvas.getHeight() * numMapCols() / numMapRows(), canvas.heightProperty()));

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
        if (terrainMap != null && terrainVisiblePy.get()) {
            terrainMapRenderer.setScaling(scaling());
            terrainMapRenderer.drawMap(g, terrainMap);
        }
        if (foodMap != null && foodVisiblePy.get()) {
            foodMapRenderer.setScaling(scaling());
            foodMapRenderer.drawMap(g, foodMap);
        }
        for (int row = 0; row < numMapRows(); ++row) {
            for (int col = 0; col < numMapCols(); ++col) {
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
        palette.draw();
    }

    Parent createSceneContent() {
        canvas = new Canvas();
        canvasContainer = new BorderPane(canvas);

        infoLabel = new Label();

        cbTerrainVisible = new CheckBox("Show Terrain");
        cbTerrainVisible.selectedProperty().bindBidirectional(terrainVisiblePy);

        cbFoodVisible = new CheckBox("Show Food");
        cbFoodVisible.selectedProperty().bindBidirectional(foodVisiblePy);

        cbTerrainEdited = new CheckBox("Edit Terrain");
        cbTerrainEdited.selectedProperty().bindBidirectional(terrainEditedPy);
        cbTerrainEdited.setOnAction(e -> updateInfo());

        palette = new Palette(32, 4, 4);

        GridPane rightPane = new GridPane();
        rightPane.setPrefWidth(200);
        rightPane.add(infoLabel,        0, 0);
        rightPane.add(cbTerrainVisible, 0, 1);
        rightPane.add(cbFoodVisible,    0, 2);
        rightPane.add(cbTerrainEdited,  0, 3);
        rightPane.add(palette,          0, 4);

        contentPane = new BorderPane();
        contentPane.setCenter(canvasContainer);
        contentPane.setRight(rightPane);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(createFileMenu(), createMapsMenu());

        contentPane.setTop(menuBar);
        canvas.heightProperty().bind(canvasContainer.heightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvasContainer.getHeight() / numMapRows() * numMapCols(), canvasContainer.heightProperty()
        ));
        canvas.setOnMouseClicked(this::onMouseClickedOnCanvas);
        canvas.setOnMouseMoved(this::onMouseMovedOverCanvas);

        return contentPane;
    }

    Menu createFileMenu() {
        openDialog = new FileChooser();
        openDialog.setInitialDirectory(new File(System.getProperty("user.dir")));

        var loadTerrainMapItem = new MenuItem("Load Terrain Map...");
        loadTerrainMapItem.setOnAction(e -> loadTerrainMap());

        var loadFoodMapItem = new MenuItem("Load Food Map...");
        loadFoodMapItem.setOnAction(e -> loadFoodMap());

        var saveTerrainMapItem = new MenuItem("Save Terrain Map...");
        saveTerrainMapItem.setOnAction(e -> {
            if (terrainMap != null) {
                saveTerrainMap();
            }
        });

        var saveFoodMapItem = new MenuItem("Save Food Map...");
        saveFoodMapItem.setOnAction(e -> {
            if (foodMap != null) {
                saveFoodMap();
            }
        });

        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());

        var menu = new Menu("File");
        menu.getItems().addAll(loadTerrainMapItem, loadFoodMapItem, saveTerrainMapItem, saveFoodMapItem, quitItem);

        return menu;
    }

    Menu createMapsMenu() {
        var clearMapItem = new MenuItem("Clear");
        clearMapItem.setOnAction(e -> clearMap());

        Menu loadPredefinedMapMenu = new Menu("Load Predefined Map");

        var pacManWorldItem = new MenuItem("Pac-Man");
        pacManWorldItem.setOnAction(e -> {
            copyMapsFromWorld(pacManWorld);
            terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(254, 189, 180));
            foodMapRenderer.setPelletColor(Color.rgb(254, 189, 180));
        });

        var msPacManWorldItem1 = new MenuItem("Ms. Pac-Man 1");
        msPacManWorldItem1.setOnAction(e -> {
            copyMapsFromWorld(msPacManWorld1);
            terrainMapRenderer.setWallColor(Color.rgb(255, 183, 174));
            foodMapRenderer.setEnergizerColor(Color.rgb(222, 222, 255));
            foodMapRenderer.setPelletColor(Color.rgb(222, 222, 255));
        });

        var msPacManWorldItem2 = new MenuItem("Ms. Pac-Man 2");
        msPacManWorldItem2.setOnAction(e -> {
            copyMapsFromWorld(msPacManWorld2);
            terrainMapRenderer.setWallColor(Color.rgb(71, 183, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(255, 255, 0));
            foodMapRenderer.setPelletColor(Color.rgb(255, 255, 0));
        });

        var msPacManWorldItem3 = new MenuItem("Ms. Pac-Man 3");
        msPacManWorldItem3.setOnAction(e -> {
            copyMapsFromWorld(msPacManWorld3);
            terrainMapRenderer.setWallColor(Color.rgb(222, 151, 81));
            foodMapRenderer.setEnergizerColor(Color.rgb(255, 0, 0));
            foodMapRenderer.setPelletColor(Color.rgb(255, 0, 0));
        });

        var msPacManWorldItem4 = new MenuItem("Ms. Pac-Man 4");
        msPacManWorldItem4.setOnAction(e -> {
            copyMapsFromWorld(msPacManWorld4);
            terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(222, 222, 255));
            foodMapRenderer.setPelletColor(Color.rgb(222, 222, 255));
        });

        loadPredefinedMapMenu.getItems().addAll(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2,
            msPacManWorldItem3, msPacManWorldItem4);

        var menu = new Menu("Map");
        menu.getItems().addAll(clearMapItem, loadPredefinedMapMenu);

        return menu;
    }

    void copyMapsFromWorld(World world) {
        terrainMap = new TileMap(world.tileMap());
        foodMap    = new TileMap(world.foodMap());
    }

    int numMapCols() {
        return terrainMap != null ? terrainMap.numCols() : 28;
    }

    int numMapRows() {
        return terrainMap != null ? terrainMap.numRows() : 36;
    }

    double scaling() {
        return canvas.getHeight() / (numMapRows() * 8);
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
                terrainMap.setContent(hoveredTile, selectedTerrainValue);
            } else {
                foodMap.setContent(hoveredTile, selectedFoodValue);
            }
        }
    }

    void editTerrainTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            terrainMap.setContent(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = terrainMap.content(tile);
            byte nextValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            terrainMap.setContent(tile, nextValue);
            updateInfo();
        }
        else {
            terrainMap.setContent(tile, selectedTerrainValue);
            updateInfo();
        }
    }

    void editFoodTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            foodMap.setContent(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) { // cycle through all tile values
            byte content = foodMap.content(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            foodMap.setContent(tile, newValue);
            updateInfo();
        }
        else {
            foodMap.setContent(tile, selectedFoodValue);
            updateInfo();
        }
    }

    void updateInfo() {
        var text = hoveredTile != null ? String.format("Tile: x=%2d y=%2d", hoveredTile.x(), hoveredTile.y()) : "";
        infoLabel.setText(text);
    }

    void loadTerrainMap() {
        File file = openDialog.showOpenDialog(stage);
        if (file != null) {
            try {
                terrainMap = TileMap.fromURL(file.toURI().toURL(), Tiles.TERRAIN_TILES_END);
            } catch (MalformedURLException x) {
                Logger.error("Could not load map.");
                Logger.error(x);
            }
        }
    }

    void loadFoodMap() {
        File file = openDialog.showOpenDialog(stage);
        if (file != null) {
            try {
                foodMap = TileMap.fromURL(file.toURI().toURL(), Tiles.FOOD_TILES_END);
            } catch (MalformedURLException x) {
                Logger.error("Could not load map.");
                Logger.error(x);
            }
        }
    }

    void saveTerrainMap() {
        File file = openDialog.showSaveDialog(stage);
        saveMap(terrainMap, file);
    }

    void saveFoodMap() {
        File file = openDialog.showSaveDialog(stage);
        saveMap(foodMap, file);
    }

    void saveMap(TileMap map, File file) {
        try (FileWriter w = new FileWriter(file, StandardCharsets.UTF_8)) {
            for (int row = 0; row < map.numRows(); ++row) {
                for (int col = 0; col < map.numCols(); ++col) {
                    String valueTxt = String.valueOf(map.content(row, col));
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

    class Palette extends Canvas {

        int numRows;
        int numCols;
        int gridSize;
        GraphicsContext g = getGraphicsContext2D();

        Palette(int gridSize, int numRows, int numCols) {
            this.gridSize = gridSize;
            this.numRows = numRows;
            this.numCols = numCols;
            setWidth(numCols * gridSize);
            setHeight(numRows * gridSize);
            setOnMouseClicked(this::pickTile);
        }

        void pickTile(MouseEvent e) {
            int row = (int) e.getY() / gridSize;
            int col = (int) e.getX() / gridSize;
            Logger.info("Tile row={} col={}", row, col);
            byte b = (byte) (row * numCols + col);
            if (terrainEditedPy.get()) {
                selectedTerrainValue = b < Tiles.TERRAIN_TILES_END ? b : Tiles.EMPTY;
                Logger.info("Selected terrain value: {}", selectedTerrainValue);
            } else {
                selectedFoodValue = b < Tiles.FOOD_TILES_END ? b : Tiles.EMPTY;
                Logger.info("Selected food value: {}", selectedFoodValue);
            }
        }

        void draw() {
            g.setFill(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setStroke(Color.GRAY);
            for (int row = 1; row < numRows; ++row) {
                g.strokeLine(0, row * gridSize, getWidth(), row * gridSize);
            }
            for (int col = 1; col < numCols; ++col) {
                g.strokeLine(col * gridSize, 0, col * gridSize, getHeight());
            }
            TileMapRenderer renderer = terrainEditedPy.get() ? terrainMapRenderer : foodMapRenderer;
            renderer.setScaling(1f * gridSize / 8);
            for (int i = 0; i < numRows * numCols; ++i) {
                int row = i / numCols, col = i % numCols;
                renderer.drawTile(g, new Vector2i(col, row), (byte) i);
            }
            // mark selected entry
            g.setStroke(Color.YELLOW);
            g.setLineWidth(1);
            if (terrainEditedPy.get()) {
                int row = selectedTerrainValue / numCols;
                int col = selectedTerrainValue % numCols;
                g.strokeRect(col * gridSize, row * gridSize, gridSize, gridSize);
            } else {
                int row = selectedFoodValue / numCols;
                int col = selectedFoodValue % numCols;
                g.strokeRect(col * gridSize, row * gridSize, gridSize, gridSize);
            }
        }
    }
}
