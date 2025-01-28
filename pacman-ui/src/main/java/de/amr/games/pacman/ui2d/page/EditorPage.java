/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.lib.Ufx;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Armin Reichert
 */
public class EditorPage extends BorderPane implements GameActionProvider {

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final TileMapEditor editor;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorPage(Stage stage, GameContext context, File customMapDir) {
        Globals.assertNotNull(stage);
        Globals.assertNotNull(context);
        Globals.assertNotNull(customMapDir);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey

        editor = new TileMapEditor(customMapDir);
        editor.createUI(stage);

        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.getFileMenu().getItems().add(miQuitEditor);

        /*
        editor.addLoadMapMenuItem("Pac-Man", loadMap("pacman/pacman.world"));
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 6; ++mapNumber) {
            WorldMap map = loadMap("mspacman/mspacman_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Ms. Pac-Man " + mapNumber, map);
            }
        }
        editor.getLoadMapMenu().getItems().add(new SeparatorMenuItem());
        */

        //TODO reconsider this
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap(() -> context.game().getClass(), "maps/masonic_%d.world".formatted(mapNumber));
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
            editor.setWorldMap(map);
        }
        editor.start();
    }

    public void setCloseAction(Consumer<TileMapEditor> closeAction) {
        this.closeAction = closeAction;
    }

    private WorldMap loadMap(ResourceManager rm, String relativeMapPath) {
        URL url = rm.url(relativeMapPath);
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
}