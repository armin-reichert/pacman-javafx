/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.action.GameAction;
import de.amr.games.pacman.ui2d.action.GameActionProvider;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
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

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class EditorPage extends BorderPane implements GameActionProvider {

    private static WorldMap loadMap(ResourceManager rm, String relativeMapPath) {
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

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final TileMapEditor editor;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorPage(Stage stage, GameContext context, File customMapDir) {
        assertNotNull(stage);
        assertNotNull(context);
        assertNotNull(customMapDir);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey

        editor = new TileMapEditor();
        editor.createUI(stage);

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.getFileMenu().getItems().add(miQuitEditor);

        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap(() -> context.game().getClass(), "maps/masonic_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuItem("Pac-Man XXL " + mapNumber, map);
            }
        }

        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());

        editor.init(customMapDir);
    }

    @Override
    public void bindGameActions() {}

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
        this.closeAction = assertNotNull(closeAction);
    }
}