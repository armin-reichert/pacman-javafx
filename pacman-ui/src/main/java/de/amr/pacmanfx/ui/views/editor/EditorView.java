/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.editor;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.uilib.view3d.Pac3DShapeFactory;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Supplier;

public class EditorView implements GameView {

    private TileMapEditor editor;

    public EditorView() {}

    public void ensureEditorCreated(PacManGamesEngine app) {
        if (editor == null) {
            editor = new TileMapEditor(app.ui().window().stage(), new Pac3DShapeFactory());
            editor.setOnQuit(_ -> app.ui().viewManager().selectStartPagesView());
            final MenuItem miQuitEditor = new MenuItem(app.translationManager().translate("editor.menu.back_to_game"));
            miQuitEditor.setOnAction(_ -> editor.quit());
            editor.ui().menuSystem().fileMenu().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);
            editor.ui().layoutPane().setBackground(Ufx.paintBackground(Color.valueOf("#dddddd"))); // JavaFX default grey
        }
    }

    @Override
    public void setEngine(PacManGamesEngine engine) {}

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
    public void onInput(PacManGamesEngine app) {
        Logger.warn("I should never get input from the global keyboard!");
    }

    @Override
    public void onQuit() {
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