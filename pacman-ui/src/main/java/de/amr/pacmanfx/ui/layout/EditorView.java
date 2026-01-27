/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_View;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.Ufx.paintBackground;

public class EditorView implements GameUI_View {

    private final GameUI ui;
    private final TileMapEditor editor;

    public EditorView(Stage stage, GameUI ui) {
        this.ui = ui;
        editor = new TileMapEditor(stage, PacManModel3DRepository.INSTANCE);
        MenuItem miQuitEditor = createQuitEditorMenuItem();
        editor.ui().menuBar().menuFile().getItems().addAll(new SeparatorMenuItem(), miQuitEditor);
        editor.ui().layoutPane().setBackground(paintBackground(Color.valueOf("#dddddd"))); // JavaFX default grey
    }

    private MenuItem createQuitEditorMenuItem() {
        var miQuitEditor = new MenuItem(ui.translate("back_to_game"));
        miQuitEditor.setOnAction(_ -> editor.quit());
        return miQuitEditor;
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsManager actionBindingsManager() { return ActionBindingsManager.EMPTY; }

    @Override
    public void onEnter() {
        editor.ui().layoutPane().requestFocus();
    }

    @Override
    public void onExit() {
    }

    @Override
    public Region root() {
        return editor.ui().layoutPane();
    }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(editor.ui().titleProperty()::get);
    }
}