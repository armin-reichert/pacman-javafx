/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.amr.games.pacman.lib.Globals.assertNotNull;

public class EditorView extends BorderPane implements GameActionProvider {

    private final Map<KeyCodeCombination, GameAction> actionBindings = new HashMap<>();
    private final TileMapEditor editor;
    private Consumer<TileMapEditor> closeAction = editor -> {};

    public EditorView(Stage stage, GameContext context) {
        assertNotNull(stage);
        assertNotNull(context);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey

        editor = new TileMapEditor();
        editor.createUI(stage);

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);

        ResourceManager rm = () -> context.game().getClass();
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            String path = "maps/masonic_%d.world".formatted(mapNumber);
            URL url = rm.url(path);
            if (url != null) {
                try {
                    WorldMap map = new WorldMap(url);
                    editor.addLoadMapMenuItem("Pac-Man XXL " + mapNumber, map);
                } catch (IOException x) {
                    Logger.info("Map could not be loaded from path {}", path);
                }
            } else {
                Logger.info("Map could not be found at path {}", path);
            }
        }

        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());

        editor.init(GameModel.CUSTOM_MAP_DIR);
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