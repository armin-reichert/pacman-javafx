/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameActionProvider;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.uilib.Ufx;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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

        editor = new TileMapEditor();
        editor.createUI(stage);
        editor.init(GameModel.CUSTOM_MAP_DIR);

        var miQuitEditor = new MenuItem(context.locText("back_to_game"));
        miQuitEditor.setOnAction(e -> closeAction.accept(editor));
        editor.getFileMenu().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);

        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey
        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());
    }

    @Override
    public void bindGameActions() {}

    @Override
    public Map<KeyCodeCombination, GameAction> actionBindings() {
        return actionBindings;
    }

    public TileMapEditor editor() {
        return editor;
    }

    public void setCloseAction(Consumer<TileMapEditor> closeAction) {
        this.closeAction = assertNotNull(closeAction);
    }
}