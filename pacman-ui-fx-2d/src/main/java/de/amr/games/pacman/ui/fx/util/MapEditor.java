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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class MapEditor extends Application  {

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

    TileMapRenderer renderer;

    TileMap tileMap;
    TileMap foodMap;

    byte lastSelectedValue;
    Vector2i hoveredTile;

    World pacManWorld    = GameVariants.PACMAN.createWorld(1);
    World msPacManWorld1 = GameVariants.MS_PACMAN.createWorld(1);
    World msPacManWorld2 = GameVariants.MS_PACMAN.createWorld(2);
    World msPacManWorld3 = GameVariants.MS_PACMAN.createWorld(3);
    World msPacManWorld4 = GameVariants.MS_PACMAN.createWorld(4);

    @Override
    public void init() throws Exception {
        loadTerrainMapFromURL(TileMap.class.getResource("/de/amr/games/pacman/maps/pacman_terrain.txt"));
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        renderer = new TileMapRenderer();
        load(pacManWorld, Color.rgb(33, 33, 255));

        scene = new Scene(createSceneContent(), 800, 600);
        scene.setFill(Color.BLACK);

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
        saveItem.setOnAction(e -> saveMap());

        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());

        var menu = new Menu("File");
        menu.getItems().addAll(saveItem, quitItem);

        return menu;
    }

    Menu createWorldMenu() {
        var pacManWorldItem = new RadioMenuItem("Pac-Man");
        pacManWorldItem.setOnAction(e -> load(pacManWorld, Color.rgb(33, 33, 255)));

        var msPacManWorldItem1 = new RadioMenuItem("Ms. Pac-Man Map 1");
        msPacManWorldItem1.setOnAction(e -> load(msPacManWorld1, Color.RED));

        var msPacManWorldItem2 = new RadioMenuItem("Ms. Pac-Man Map 2");
        msPacManWorldItem2.setOnAction(e -> load(msPacManWorld2, Color.rgb(71, 183, 255)));

        var msPacManWorldItem3 = new RadioMenuItem("Ms. Pac-Man Map 3");
        msPacManWorldItem3.setOnAction(e -> load(msPacManWorld3, Color.rgb(222, 151, 81)));

        var msPacManWorldItem4 = new RadioMenuItem("Ms. Pac-Man Map 4");
        msPacManWorldItem4.setOnAction(e -> load(msPacManWorld4, Color.rgb(33, 33, 255)));

        ToggleGroup tg = new ToggleGroup();
        Stream.of(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2, msPacManWorldItem3, msPacManWorldItem4)
            .forEach(item -> item.setToggleGroup(tg));
        pacManWorldItem.setSelected(true);

        var menu = new Menu("World");
        menu.getItems().addAll(pacManWorldItem, msPacManWorldItem1, msPacManWorldItem2, msPacManWorldItem3, msPacManWorldItem4);

        return menu;
    }

    void loadTerrainMapFromURL(URL url) {
        TileMap map = TileMap.fromURL(url, Tiles.TERRAIN_TILES_END);
        if (map != null) {
            Logger.info("Map loaded. {} rows, {} cols", map.numRows(), map.numCols());
        } else {
            Logger.error("Could not load map from URL {}", url);
        }
    }
    void load(World world, Color wallColor) {
        tileMap = world.tileMap();
        foodMap = world.foodMap();
        renderer.setWallColor(wallColor);
    }

    int numMapCols() {
        return tileMap != null ? tileMap.numCols() : 28;
    }

    int numMapRows() {
        return tileMap != null ? tileMap.numRows() : 36;
    }

    double scaling() {
        return canvas.getHeight() / (numMapRows() * 8);
    }

    void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (tileMap != null) {
            renderer.setScaling(scaling());
            renderer.drawMap(g, tileMap);
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

    int viewToTile(double viewLength) {
        return (int) (viewLength / (8 * scaling()));
    }

    void onMouseClickedOnCanvas(MouseEvent e) {
        if (tileMap == null) {
            return;
        }
        var tile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        if (e.getButton() == MouseButton.SECONDARY) {
            tileMap.setContent(tile, Tiles.EMPTY);
            updateHoveredTileInfo();
        }
        else if (e.isShiftDown()) {
            tileMap.setContent(tile, lastSelectedValue);
            updateHoveredTileInfo();
        }
        else {
            byte content = tileMap.content(tile);
            byte newValue = content < Tiles.TERRAIN_TILES_END - 1 ? (byte) (content + 1) : 0;
            tileMap.setContent(tile, newValue);
            lastSelectedValue = newValue;
            updateHoveredTileInfo();
        }
    }

    void onMouseMovedOverCanvas(MouseEvent e) {
        hoveredTile = new Vector2i(viewToTile(e.getX()), viewToTile(e.getY()));
        updateHoveredTileInfo();
    }

    void updateHoveredTileInfo() {
        var text = String.format("Tile: x=%2d y=%2d value=%d",
            hoveredTile.x(), hoveredTile.y(), tileMap.content(hoveredTile));
        infoLabel.setText(text);
    }

    void saveMap() {
        if (tileMap == null) {
            Logger.info("No map loaded");
            return;
        }
        File file = new File("saved_map.txt");
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            for (int row = 0; row < tileMap.numRows(); ++row) {
                fw.write("{");
                for (int col = 0; col < tileMap.numCols(); ++col) {
                    String valueTxt = String.valueOf(tileMap.content(row, col));
                    fw.write(String.format("%2s", valueTxt));
                    if (col < tileMap.numCols() - 1) {
                        fw.write(",");
                    }
                }
                fw.write("},\n");
            }
            Logger.info("Map saved successfully");
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}
