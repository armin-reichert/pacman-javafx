/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

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

        var layout = new BorderPane();
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());

        var miQuit = new MenuItem(TileMapEditor.tt("quit"));
        miQuit.setOnAction(e -> editor.showSaveConfirmationDialog(editor::showSaveDialog, stage::close));
        editor.menuFile().getItems().add(miQuit);

        double height = Math.max(0.9 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        double width = 1.9 * height;
        var scene = new Scene(layout, width, height);
        scene.setFill(Color.BLACK);

        stage.setScene(scene);
        stage.titleProperty().bind(editor.titlePy);
        stage.setOnCloseRequest(e -> editor.showSaveConfirmationDialog(editor::showSaveDialog, stage::close));
        stage.show();

        String path = "/de/amr/games/pacman/mapeditor/maps/";
        WorldMap pacManMap = new WorldMap(getClass().getResource(path + "pacman.world"));
        List<WorldMap> msPacManMaps = rangeClosed(1, 6)
            .mapToObj(mapNumber -> getClass().getResource(path + "mspacman/mspacman_" + mapNumber + ".world"))
            .map(WorldMap::new)
            .toList();
        List<WorldMap> pacManXXLMaps = rangeClosed(1, 8)
            .mapToObj(mapNumber -> getClass().getResource(path + "masonic/masonic_" + mapNumber + ".world"))
            .map(WorldMap::new)
            .toList();

        editor.addLoadMapMenuItem("Pac-Man", pacManMap);
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(num -> editor.addLoadMapMenuItem("Ms. Pac-Man " + num,msPacManMaps.get(num - 1)));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(num -> editor.addLoadMapMenuItem("Pac-Man XXL " + num,pacManXXLMaps.get(num - 1)));

        editor.loadMap(pacManMap);
        editor.start();
    }
}