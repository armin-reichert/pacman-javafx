/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.app;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.translated;

/**
 * Standalone tile map editor application.
 */
public class TileMapEditorApp extends Application  {

    @Override
    public void start(Stage stage) {
        try {
            var editor = new TileMapEditor(stage);

            var miQuit = new MenuItem(translated("quit"));
            miQuit.setOnAction(_ -> editor.ui().afterCheckForUnsavedChanges(stage::close));

            editor.ui().menuBar().menuFile().getItems().addAll(new SeparatorMenuItem(), miQuit);

            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
            double height = Math.max(0.8 * screenHeight, 600);
            double width = 1.25 * height;

            var scene = new Scene(editor.ui().layoutPane(), width, height);
            stage.setScene(scene);

            editor.init(new File(System.getProperty("user.home")));
            editor.start();

            stage.show();
        } catch (Exception x) {
            Logger.error(x, "Could not start tile map editor application");
        }
    }
}