/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.editor;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.subviews.GameUI_SubView;
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.paintBackground;

public class Editor_SubView implements GameUI_SubView {

    private final GameUI ui;
    private final TileMapEditor editor;

    public Editor_SubView(Stage stage, GameUI ui) {
        this.ui = ui;
        editor = new TileMapEditor(stage);
        MenuItem miQuitEditor = createQuitEditorMenuItem();
        editor.ui().menuBar().menuFile().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);
        editor.ui().layoutPane().setBackground(paintBackground(Color.valueOf("#dddddd"))); // JavaFX default grey
    }

    private MenuItem createQuitEditorMenuItem() {
        var miQuitEditor = new MenuItem(ui.access().translations().translate("back_to_game"));
        miQuitEditor.setOnAction(_ -> editor.quit());
        return miQuitEditor;
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsSet actionBindings() { return ActionBindingsSet.NO_BINDINGS; }

    @Override
    public void onEnter() {
        editor.ui().layoutPane().requestFocus();
    }

    @Override
    public void onExit() {
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