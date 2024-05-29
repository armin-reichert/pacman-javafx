/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.games.pacman.model.GameModel.loadMap;
import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Armin Reichert
 */
public class TileMapEditorApp extends Application  {

    private TileMapEditor editor;

    @Override
    public void init() throws Exception {
        editor = new TileMapEditor();
    }

    @Override
    public void start(Stage stage) {
        try {
            doStart(stage);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    private void doStart(Stage stage) {
        editor.createUI(stage);
        addPredefinedMaps();

        var layout = new BorderPane();
        layout.setCenter(editor.getLayout());
        layout.setTop(editor.getMenuBar());

        var miQuit = new MenuItem("Quit");
        miQuit.setOnAction(e -> editor.showConfirmation(editor::saveMapFileAs, stage::close));
        editor.menuFile().getItems().add(miQuit);


        double height = Math.max(0.7 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        double width = 1.9 * height;
        var scene = new Scene(layout, width, height);
        scene.setFill(Color.BLACK);

        stage.setScene(scene);
        stage.titleProperty().bind(editor.titlePy);
        stage.setOnCloseRequest(e -> editor.showConfirmation(editor::saveMapFileAs, stage::close));
        stage.show();

        editor.start();
        editor.loadMap(editor.getPredefinedMap("Pac-Man"));
    }

    private void addPredefinedMaps() {
        editor.addPredefinedMap("Pac-Man", loadMap(getClass().getResource("maps/pacman.world")));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(mapNumber -> editor.addPredefinedMap("Ms. Pac-Man " + mapNumber,
            loadMap(getClass().getResource("/de/amr/games/pacman/mapeditor/mspacman/mspacman_" + mapNumber + ".world")))
        );
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(mapNumber -> editor.addPredefinedMap("Pac-Man XXL " + mapNumber,
            loadMap(getClass().getResource("/de/amr/games/pacman/mapeditor/maps/masonic/masonic_" + mapNumber + ".world")))
        );
    }
}