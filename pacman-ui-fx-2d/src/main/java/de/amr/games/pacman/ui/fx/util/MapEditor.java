package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.world.TileMap;
import de.amr.games.pacman.model.world.Tiles;
import de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.PickResult;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

public class MapEditor extends Application  {

    public static void main(String[] args) {
        launch();
    }

    Scene scene;
    Canvas canvas;
    TileMap tileMap;
    TileMapRenderer renderer;

    @Override
    public void init() throws Exception {
        tileMap = GameVariants.PACMAN.createWorld(1).tileMap();
        renderer = new TileMapRenderer();
        renderer.setWallColor(Color.rgb(33, 33, 255));
    }

    @Override
    public void start(Stage stage) throws Exception {
        Logger.info("Start map editor");
        stage.setScene(createScene());
        stage.setTitle("Map Editor");
        GameClockFX clock = new GameClockFX();
        clock.setContinousCallback(() -> drawCanvas(scaling()));
        stage.show();
        clock.start();
    }

    Scene createScene() {
        scene = new Scene(createSceneContent(), 800, 600);
        scene.setFill(Color.BLACK);
        canvas.heightProperty().bind(scene.heightProperty());
        canvas.widthProperty().bind(Bindings.createDoubleBinding(
            () -> scene.getHeight() / tileMap.numRows() * tileMap.numCols(), scene.heightProperty()
        ));
        canvas.setOnMouseClicked(e -> {
            onMouseClicked(e.getPickResult());
        });
        return scene;
    }

    double scaling() {
        return scene.getHeight() / (tileMap.numRows() * 8);
    }

    Pane createSceneContent() {
        canvas = new Canvas();
        var pane = new BorderPane();
        pane.setCenter(canvas);
        return pane;
    }

    void drawCanvas(double scaling) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        renderer.setScaling(scaling);
        renderer.drawMap(g, tileMap);
        g.setStroke(Color.GRAY);
        g.setLineWidth(0.5);
        tileMap.tiles().forEach(tile -> {
            g.strokeRect(tile.x() * 8 * scaling, tile.y() * 8 * scaling, 8*scaling, 8*scaling);
        });
    }

    void onMouseClicked(PickResult pr) {
        Logger.info("Mouse click {}", pr);
        var point = pr.getIntersectedPoint();
        double factor = 8 * scaling();
        Vector2i tile = new Vector2i((int) (point.getX() / factor), (int) (point.getY() / factor));
        Logger.info("Tile clicked: {}", tile);
        byte content = tileMap.content(tile);
        byte newContent = content < Tiles.TERRAIN_END_MARKER - 1 ? (byte)(content + 1) : 0;
        tileMap.setContent(tile.y(), tile.x(), newContent);
    }
}