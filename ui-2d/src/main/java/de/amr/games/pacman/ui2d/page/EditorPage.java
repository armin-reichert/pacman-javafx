/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.net.URL;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class EditorPage implements Page {

    private final TileMapEditor editor;
    private final GameContext context;
    private final BorderPane pane;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorPage(Window window, GameContext context) {
        this.context = checkNotNull(context);
        editor = new TileMapEditor(GameModel.CUSTOM_MAP_DIR);

        pane = new BorderPane();
        pane.setCenter(editor.getContentPane());
        pane.setTop(editor.getMenuBar());
        editor.createUI(window);

        var miQuitEditor = new MenuItem(context.tt("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.menuFile().getItems().add(miQuitEditor);

        // load maps from core module
        editor.addLoadMapMenuEntry("Pac-Man", loadMap("pacman.world"));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 6; ++mapNumber) {
            WorldMap map = loadMap("mspacman/mspacman_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuEntry("Ms. Pac-Man " + mapNumber, map);
            }
        }
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap("masonic/masonic_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuEntry("Pac-Man XXL " + mapNumber, map);
            }
        }
    }

    public TileMapEditor editor() {
        return editor;
    }

    public void startEditor() {
        editor.start();
    }

    public void setCloseAction(Consumer<TileMapEditor> closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    public Pane rootPane() {
        return pane;
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
}