/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class EditorPage extends BorderPane implements Page {

    private static final String MAP_ROOT_PATH = "/de/amr/games/pacman/maps/";

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final TileMapEditor editor;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorPage(Stage stage, GameContext context, File customMapDir) {
        checkNotNull(stage);
        checkNotNull(context);
        checkNotNull(customMapDir);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey

        editor = new TileMapEditor(customMapDir);
        editor.createUI(stage);

        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.getFileMenu().getItems().add(miQuitEditor);

        editor.addLoadMapMenuItem("Pac-Man", loadMap("pacman/pacman.world"));
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 6; ++mapNumber) {
            WorldMap map = loadMap("mspacman/mspacman_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Ms. Pac-Man " + mapNumber, map);
            }
        }
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap("pacman_xxl/masonic_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Pac-Man XXL " + mapNumber, map);
            }
        }
    }

    @Override
    public void bindGameActions() {
    }

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public void startEditor(WorldMap map) {
        if (map != null) {
            editor.setMap(map);
        }
        editor.start();
    }

    public void setCloseAction(Consumer<TileMapEditor> closeAction) {
        this.closeAction = closeAction;
    }

    private WorldMap loadMap(String relativeMapPath) {
        ResourceManager rm = () -> GameModel.class;
        URL url = rm.url(MAP_ROOT_PATH + relativeMapPath);
        if (url != null) {
            try {
                WorldMap map = new WorldMap(url);
                Logger.info("Map loaded from URL {}", url);
                return map;
            } catch (IOException x) {
                Logger.error(x);
                Logger.error("Could not load map at path {}", relativeMapPath);
                return null;
            }
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