package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class MapEditor extends Application  {

    public static void main(String[] args) {
        launch();
    }


    Stage stage;
    Scene scene;
    BorderPane contentPane;
    BorderPane canvasContainer;
    Canvas canvas;
    TileMap tileMap;
    TileMap foodMap;
    TileMapRenderer renderer;
    MenuBar menuBar;

    byte lastSelectedValue;

    @Override
    public void init() throws Exception {
        renderer = new TileMapRenderer();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Logger.info("Start map editor");
        stage.setScene(createScene());
        stage.setTitle("Map Editor");
        GameClockFX clock = new GameClockFX();
        clock.setContinousCallback(this::drawCanvas);
        stage.show();
        clock.start();
    }

    public void setWorld(World world, Color wallColor) {
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

    Scene createScene() {
        scene = new Scene(createSceneContent(), 800, 600);
        scene.setFill(Color.BLACK);

        canvas.heightProperty().bind(scene.heightProperty().multiply(0.95));
        canvas.widthProperty().bind(
            Bindings.createDoubleBinding(
                () -> canvas.getHeight() * numMapCols() / numMapRows(),
                canvas.heightProperty()));
        return scene;
    }

    double scaling() {
        return canvas.getHeight() / (numMapRows() * 8);
    }

    Parent createSceneContent() {
        canvas = new Canvas();
        canvasContainer = new BorderPane(canvas);
        contentPane = new BorderPane(canvasContainer);
        createMenus();

        contentPane.setTop(menuBar);
        canvas.heightProperty().bind(canvasContainer.heightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> canvasContainer.getHeight() / numMapRows() * numMapCols(), canvasContainer.heightProperty()
        ));
        canvas.setOnMouseClicked(this::onMouseClicked);

        return contentPane;
    }

    void createMenus() {
        menuBar = new MenuBar();
        var fileMenu = new Menu("File");
        var saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveMap());
        fileMenu.getItems().add(saveItem);
        var quitItem = new MenuItem("Quit");
        quitItem.setOnAction(e -> stage.close());
        fileMenu.getItems().add(quitItem);
        menuBar.getMenus().add(fileMenu);
    }

    void drawCanvas() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setStroke(Color.GRAY);
        g.setLineWidth(0.5);
        double s8 = 8 * scaling();
        renderer.setScaling(scaling());
        if (tileMap != null) {
            renderer.drawMap(g, tileMap);
        }
        for (int row = 0; row < numMapRows(); ++row) {
            for (int col = 0; col < numMapCols(); ++col) {
                g.strokeRect(col * s8, row * s8, s8, s8);
            }
        }
    }

    void onMouseClicked(MouseEvent e) {
        Logger.info("Mouse click {}", e);
        if (tileMap == null) {
            return;
        }
        double factor = 8 * scaling();
        Vector2i tile = new Vector2i((int) (e.getX() / factor), (int) (e.getY() / factor));
        if (e.getButton() == MouseButton.SECONDARY) {
            tileMap.setContent(tile.y(), tile.x(), Tiles.EMPTY);
        }
        else if (e.isShiftDown()) {
            tileMap.setContent(tile.y(), tile.x(), lastSelectedValue);
        }
        else {
            byte content = tileMap.content(tile);
            byte newValue = content < Tiles.TERRAIN_END_MARKER - 1 ? (byte) (content + 1) : 0;
            tileMap.setContent(tile.y(), tile.x(), newValue);
            lastSelectedValue = newValue;
        }
    }

    void saveMap() {
        if (tileMap == null) {
            Logger.info("No map loaded");
            return;
        }
        File file = new File("saved_map.txt");
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            for (int row = 0; row < tileMap.numRows(); ++row) {
                writer.write("{");
                for (int col = 0; col < tileMap.numCols(); ++col) {
                    String valueTxt = String.valueOf(tileMap.content(row, col));
                    writer.write(String.format("%2s", valueTxt));
                    if (col < tileMap.numCols() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("},\n");
            }
            Logger.info("Map saved successfully");
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}
