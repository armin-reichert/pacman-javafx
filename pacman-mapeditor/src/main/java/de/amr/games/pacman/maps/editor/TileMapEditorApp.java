/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.maps.editor.TileMapEditorViewModel.tt;
import static java.util.stream.IntStream.rangeClosed;

/**
 * @author Armin Reichert
 */
public class TileMapEditorApp extends Application  {

    static final String SAMPLE_MAPS_PATH = "/de/amr/games/pacman/maps/samples/";

    private WorldMap mapPacManGame;
    private List<WorldMap> mapsMsPacManGame;
    private List<WorldMap> mapsPacManXXLGame;

    private TileMapEditor editor;

    @Override
    public void init() throws Exception {
        loadSampleMaps();
        editor = new TileMapEditor();
    }

    @Override
    public void start(Stage stage) {
        try {
            stage.setMinWidth(900);
            stage.setMinHeight(600);
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

        var miQuit = new MenuItem(tt("quit"));
        miQuit.setOnAction(e -> editor.showSaveConfirmationDialog(editor::showSaveDialog, stage::close));
        editor.getFileMenu().getItems().add(miQuit);

        double height = Math.max(0.8 * Screen.getPrimary().getVisualBounds().getHeight(), 600);
        double width = 1.25 * height;
        var scene = new Scene(layout, width, height);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);

        editor.addLoadMapMenuItem("Pac-Man", mapPacManGame);
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(num -> editor.addLoadMapMenuItem("Ms. Pac-Man " + num, mapsMsPacManGame.get(num - 1)));
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(num -> editor.addLoadMapMenuItem("Pac-Man XXL " + num, mapsPacManXXLGame.get(num - 1)));

        stage.titleProperty().bind(editor.titlePy);
        stage.setOnCloseRequest(e -> editor.showSaveConfirmationDialog(editor::showSaveDialog, stage::close));
        stage.show();

        editor.loadMap(mapPacManGame);
        editor.start();
    }

    private void loadSampleMaps() throws IOException {
        mapPacManGame = loadSampleMap("pacman/pacman.world", 1);

        mapsMsPacManGame = new ArrayList<>(6);
        for (int num = 1; num <= 6; ++num) {
            WorldMap worldMap = loadSampleMap("mspacman/mspacman_%d.world", num);
            if (worldMap != null) {
                mapsMsPacManGame.add(worldMap);
            }
        }
        mapsPacManXXLGame = new ArrayList<>(8);
        for (int num = 1; num <= 8; ++num) {
            WorldMap worldMap = loadSampleMap("pacman_xxl/masonic_%d.world", num);
            if (worldMap != null) {
                mapsPacManXXLGame.add(worldMap);
            }
        }
    }

    private WorldMap loadSampleMap(String pattern, int number) {
        URL url = getClass().getResource(SAMPLE_MAPS_PATH + pattern.formatted(number));
        if (url != null) {
            try {
                return new WorldMap(url);
            } catch (IOException x) {
                Logger.error(x);
                return null;
            }
        }
        Logger.error("Could not load world map, pattern={}, number={}", pattern, number);
        return null;
    }
}