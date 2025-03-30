/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor.app;

import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

import static de.amr.games.pacman.tilemap.editor.TileMapEditor.tt;

/**
 * Standalone tile map editor application.
 */
public class TileMapEditorApp extends Application  {

    @Override
    public void start(Stage stage) {
        var editor = new TileMapEditor();
        editor.createUI(stage);

        var miQuit = new MenuItem(tt("quit"));
        miQuit.setOnAction(e -> editor.executeWithCheckForUnsavedChanges(stage::close));
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);

        var layout = new BorderPane();
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());

        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        double height = Math.max(0.8 * screenHeight, 600);
        double width = 1.25 * height;

        var scene = new Scene(layout, width, height);
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> editor.executeWithCheckForUnsavedChanges(() -> {
            editor.stop();
            stage.close();
        }));

        editor.init(new File(System.getProperty("user.home")));
        editor.start(stage);

        stage.show();
    }
}