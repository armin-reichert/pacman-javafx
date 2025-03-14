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
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

import static de.amr.games.pacman.tilemap.editor.TileMapEditor.tt;

/**
 * @author Armin Reichert
 */
public class TileMapEditorApp extends Application  {

    private TileMapEditor editor;

    @Override
    public void start(Stage stage) {
        editor = new TileMapEditor();
        try {
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            editor.createUI(stage);
            createUI(stage);
            editor.init(new File(System.getProperty("user.home")));
            //editor.loadMap(mapPacManGame);
            editor.start();
            stage.show();
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    private void createUI(Stage stage) {
        var layout = new BorderPane();
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());

        var miQuit = new MenuItem(tt("quit"));
        miQuit.setOnAction(e -> editor.executeWithCheckForUnsavedChanges(stage::close));
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuit);

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        double width = 1.25 * height;

        var scene = new Scene(layout, width, height);
        scene.setFill(Color.BLACK);

        stage.setScene(scene);
        stage.setOnCloseRequest(e -> editor.executeWithCheckForUnsavedChanges(() -> {
            editor.stop();
            stage.close();
        }));
        stage.titleProperty().bind(editor.titleProperty());
    }
}