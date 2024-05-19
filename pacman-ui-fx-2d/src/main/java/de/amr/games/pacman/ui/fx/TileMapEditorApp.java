/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.ui.fx.util.TileMapEditor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class TileMapEditorApp extends Application  {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        var editor = new TileMapEditor(stage);
        editor.setOwnerWindow(stage);

        var layout = new BorderPane();
        layout.setCenter(editor.getUi());
        layout.setTop(editor.getMenuBar());

        var miQuit = new MenuItem("Quit");
        miQuit.setOnAction(e -> stage.close());
        editor.getMenuFile().getItems().add(miQuit);

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        var scene = new Scene(layout, height * 1.2, height);
        scene.setFill(Color.BLACK);

        stage.setScene(scene);
        stage.titleProperty().bind(editor.titlePy);
        stage.show();

        editor.loadMap(editor.getMsPacManMap(4));
        editor.setRuntimePreview(true);
        editor.startClock();
    }
}
