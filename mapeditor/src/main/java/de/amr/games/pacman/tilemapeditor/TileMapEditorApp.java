/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemapeditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
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
    public void start(Stage stage) {
        try {
            editor = new TileMapEditor();

            var layout = new BorderPane();
            layout.setCenter(editor.getLayout());
            layout.setTop(editor.getMenuBar());

            var miQuit = new MenuItem("Quit");
            miQuit.setOnAction(e -> editor.showQuitConfirmation(stage, stage::close));
            editor.menuFile().getItems().add(miQuit);

            addPredefinedMaps(editor.menuLoadMap());

            double height = Math.max(0.7 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
            double width = 1.9 * height;
            var scene = new Scene(layout, width, height);
            scene.setFill(Color.BLACK);

            stage.setScene(scene);
            stage.titleProperty().bind(editor.titlePy);
            stage.setOnCloseRequest(e -> editor.showQuitConfirmation(stage, stage::close));
            stage.show();

            editor.setOwnerWindow(stage);
            editor.loadMap(editor.getPredefinedMap("Pac-Man XXL 4"));
            editor.start();
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    private void addPredefinedMaps(Menu menu) {
        editor.addPredefinedMap("Pac-Man", loadMap(getClass().getResource("maps/pacman.world")));
        menu.getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(mapNumber -> editor.addPredefinedMap("Ms. Pac-Man " + mapNumber,
            loadMap(getClass().getResource("maps/mspacman/mspacman_" + mapNumber + ".world")))
        );
        menu.getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(mapNumber -> editor.addPredefinedMap("Pac-Man XXL " + mapNumber,
            loadMap(getClass().getResource("maps/masonic/masonic_" + mapNumber + ".world")))
        );
    }
}