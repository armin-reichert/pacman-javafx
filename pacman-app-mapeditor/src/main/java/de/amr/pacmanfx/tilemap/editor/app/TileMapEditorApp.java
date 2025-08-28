/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.app;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.uilib.model3D.Model3DRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;

/**
 * Standalone tile map editor application.
 */
public class TileMapEditorApp extends Application  {

    @Override
    public void start(Stage stage) {
        try {
            Model3DRepository model3DRepository = new Model3DRepository();
            var editor = new TileMapEditor(stage, model3DRepository);

            var miQuit = new MenuItem(translated("quit"));
            miQuit.setOnAction(e -> editor.ui().ifNoUnsavedChangesDo(stage::close));
            editor.ui().menuBar().menuFile().getItems().addAll(new SeparatorMenuItem(), miQuit);

            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
            double height = Math.max(0.8 * screenHeight, 600);
            double width = 1.25 * height;

            var scene = new Scene(editor.ui().layoutPane(), width, height);
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> editor.ui().ifNoUnsavedChangesDo(() -> {
                editor.stop();
                stage.close();
            }));

            editor.init(new File(System.getProperty("user.home")));
            editor.start();

            stage.show();
        } catch (Exception x) {
            Logger.error(x);
        }
    }
}