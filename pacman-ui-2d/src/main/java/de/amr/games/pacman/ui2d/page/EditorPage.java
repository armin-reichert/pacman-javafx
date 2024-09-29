/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class EditorPage extends BorderPane implements Page {

    private final Stage stage;
    private final TileMapEditor editor;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorPage(Stage stage, GameContext context, File customMapDir) {
        this.stage = checkNotNull(stage);
        checkNotNull(context);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey

        editor = new TileMapEditor(customMapDir);
        editor.createUI(stage);

        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.menuFile().getItems().add(miQuitEditor);

        // load maps from core module
        editor.addLoadMapMenuItem("Pac-Man", loadMap("pacman.world"));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 6; ++mapNumber) {
            WorldMap map = loadMap("mspacman/mspacman_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Ms. Pac-Man " + mapNumber, map);
            }
        }
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap("masonic/masonic_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Pac-Man XXL " + mapNumber, map);
            }
        }
    }

    public void startEditor(WorldMap map) {
        if (map != null) {
            editor.setMap(map);
        }
        stage.titleProperty().bind(editor.titlePy);
        editor.start();
    }

    public void setCloseAction(Consumer<TileMapEditor> closeAction) {
        this.closeAction = closeAction;
    }

    private WorldMap loadMap(String relativeMapPath) {
        ResourceManager core = () -> GameModel.class;
        URL url = core.url("/de/amr/games/pacman/maps/" + relativeMapPath);
        if (url != null) {
            WorldMap map = new WorldMap(url);
            Logger.info("Map loaded from URL {}", url);
            return map;
        }
        Logger.error("Could not find map at path {}", relativeMapPath);
        return null;
    }

    @Override
    public Pane rootPane() {
        return this;
    }

    @Override
    public void onPageSelected() {
    }

    @Override
    public void setSize(double width, double height) {
    }
}