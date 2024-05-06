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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

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
    VBox infoPane;
    Label infoLabel;

    TileMapRenderer terrainMapRenderer;
    TileMapRenderer foodMapRenderer;

    TileMap terrainMap;
    TileMap foodMap;

    byte lastSelectedTerrainValue;
    byte lastSelectedFoodValue;
    Vector2i hoveredTile;
    boolean showTerrain = true;
    boolean showFood = true;
    boolean editTerrain = true;

    World pacManWorld    = GameVariants.PACMAN.createWorld(1);
    World msPacManWorld1 = GameVariants.MS_PACMAN.createWorld(1);
    World msPacManWorld2 = GameVariants.MS_PACMAN.createWorld(2);
    World msPacManWorld3 = GameVariants.MS_PACMAN.createWorld(3);
    World msPacManWorld4 = GameVariants.MS_PACMAN.createWorld(4);

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        setWorld(pacManWorld);

        terrainMapRenderer = new TileMapRenderer();
        terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));

        foodMapRenderer = new TileMapRenderer();
        foodMapRenderer.setRenderTerrain(false);
        foodMapRenderer.setEnergizerColor(Color.rgb(254, 189, 180));
        foodMapRenderer.setPelletColor(Color.rgb(254, 189, 180));

        scene = new Scene(createSceneContent(), 800, 600);
        scene.setFill(Color.BLACK);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.T) {
                editTerrain = true;
                updateInfo();
            }
            else if (e.getCode() == KeyCode.F) {
                editTerrain = false;
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
        if (terrainMap != null && showTerrain) {
            terrainMapRenderer.setScaling(scaling());
            terrainMapRenderer.drawMap(g, terrainMap);
        }
        if (foodMap != null && showFood) {
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
    }

    Parent createSceneContent() {
        canvas = new Canvas();
        canvasContainer = new BorderPane(canvas);

        infoLabel = new Label();
        infoPane = new VBox(infoLabel);
        infoPane.setMinWidth(200);
        infoPane.setMaxWidth(200);

        contentPane = new BorderPane();
        contentPane.setCenter(canvasContainer);
        contentPane.setRight(infoPane);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(createFileMenu(), createWorldMenu());

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
        var saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveMaps());

        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());

        var menu = new Menu("File");
        menu.getItems().addAll(saveItem, quitItem);

        return menu;
    }

    Menu createWorldMenu() {
        var pacManWorldItem = new RadioMenuItem("Pac-Man");
        pacManWorldItem.setOnAction(e -> {
            setWorld(pacManWorld);
            terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(254, 189, 180));
            foodMapRenderer.setPelletColor(Color.rgb(254, 189, 180));
        });

        var msPacManWorldItem1 = new RadioMenuItem("Ms. Pac-Man Map 1");
        msPacManWorldItem1.setOnAction(e -> {
            setWorld(msPacManWorld1);
            terrainMapRenderer.setWallColor(Color.rgb(255, 183, 174));
            foodMapRenderer.setEnergizerColor(Color.rgb(222, 222, 255));
            foodMapRenderer.setPelletColor(Color.rgb(222, 222, 255));
        });

        var msPacManWorldItem2 = new RadioMenuItem("Ms. Pac-Man Map 2");
        msPacManWorldItem2.setOnAction(e -> {
            setWorld(msPacManWorld2);
            terrainMapRenderer.setWallColor(Color.rgb(71, 183, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(255, 255, 0));
            foodMapRenderer.setPelletColor(Color.rgb(255, 255, 0));
        });

        var msPacManWorldItem3 = new RadioMenuItem("Ms. Pac-Man Map 3");
        msPacManWorldItem3.setOnAction(e -> {
            setWorld(msPacManWorld3);
            terrainMapRenderer.setWallColor(Color.rgb(222, 151, 81));
            foodMapRenderer.setEnergizerColor(Color.rgb(255, 0, 0));
            foodMapRenderer.setPelletColor(Color.rgb(255, 0, 0));
        });

        var msPacManWorldItem4 = new RadioMenuItem("Ms. Pac-Man Map 4");
        msPacManWorldItem4.setOnAction(e -> {
            setWorld(msPacManWorld4);
            terrainMapRenderer.setWallColor(Color.rgb(33, 33, 255));
            foodMapRenderer.setEnergizerColor(Color.rgb(222, 222, 255));
            foodMapRenderer.setPelletColor(Color.rgb(222, 222, 255));
        });

        ToggleGroup tg = new ToggleGroup();
        Stream.of(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2, msPacManWorldItem3, msPacManWorldItem4)
            .forEach(item -> item.setToggleGroup(tg));
        pacManWorldItem.setSelected(true);

        var menu = new Menu("World");
        menu.getItems().addAll(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2, msPacManWorldItem3, msPacManWorldItem4);

        return menu;
    }

    void setWorld(World world) {
        terrainMap = world.tileMap();
        foodMap = world.foodMap();
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
        if (terrainMap == null) {
            return;
        }
        if (editTerrain) {
            editTerrainTile(e);
        } else {
            editFoodTile(e);
        }
    }

    void editTerrainTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            terrainMap.setContent(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) {
            terrainMap.setContent(tile, lastSelectedTerrainValue);
            updateInfo();
        }
        else {
            byte content = terrainMap.content(tile);
            byte newValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            terrainMap.setContent(tile, newValue);
            lastSelectedTerrainValue = newValue;
            updateInfo();
        }
    }

    void editFoodTile(MouseEvent e) {
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            foodMap.setContent(tile, Tiles.EMPTY);
            updateInfo();
        }
        else if (e.isShiftDown()) {
            foodMap.setContent(tile, lastSelectedFoodValue);
            updateInfo();
        }
        else {
            byte content = foodMap.content(tile);
            byte newValue = content < Tiles.FOOD_TILES_END - 1 ? (byte) (content + 1) : 0;
            foodMap.setContent(tile, newValue);
            lastSelectedFoodValue = newValue;
            updateInfo();
        }
    }

    void onMouseMovedOverCanvas(MouseEvent e) {
        hoveredTile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        updateInfo();
    }

    void updateInfo() {
        var editModeText = editTerrain ? "Terrain is edited" : "Food is edited";
        var editedTileText = String.format("Tile: x=%2d y=%2d value=%d",
            hoveredTile.x(), hoveredTile.y(), terrainMap.content(hoveredTile));
        infoLabel.setText(editModeText + "\n" + editedTileText);
    }

    void saveMaps() {
        if (terrainMap != null) {
            saveMap(terrainMap, "saved_terrain_map.txt");
        }
        if (foodMap != null) {
            saveMap(foodMap, "saved_food_map.txt");
        }
    }

    void saveMap(TileMap map, String fileName) {
        File file = new File(fileName);
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
            Logger.info("Map {} saved successfully", fileName);
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}
