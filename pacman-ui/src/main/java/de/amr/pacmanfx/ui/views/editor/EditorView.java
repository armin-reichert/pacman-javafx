/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.editor;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.views.GameView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;

public class EditorView implements GameView {

    private final TileMapEditor editor;

    public EditorView(Stage stage) {
        editor = new TileMapEditor(stage);
        editor.ui().layoutPane().setBackground(paintBackground(Color.valueOf("#dddddd"))); // JavaFX default grey
    }

    public void connect(Game game) {
        final MenuItem miQuitEditor = new MenuItem(game.ui().translations().translate("back_to_game"));
        miQuitEditor.setOnAction(_ -> editor.quit());
        editor.ui().menuSystem().fileMenu().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsRegistry actionBindings() { return ActionBindingsRegistry.NO_BINDINGS; }

    @Override
    public void onEnter() {
        editor.ui().layoutPane().requestFocus();
    }

    @Override
    public void onExit() {}

    @Override
    public void onInput(Input input) {
        Logger.warn("I should never get input from the global keyboard!");
    }

    @Override
    public void handleQuit(Game game) {
        editor.quit();
    }

    @Override
    public Region rootPane() {
        return editor.ui().layoutPane();
    }

    @Override
    public Optional<Supplier<String>> optTitleSupplier() {
        return Optional.of(editor.ui().titleProperty()::get);
    }
}