/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.Ufx.colorBackground;
import static java.util.Objects.requireNonNull;

public class EditorView implements GameUI_View {

    private final TileMapEditorUI editorUI;

    public EditorView(TileMapEditorUI editorUI) {
        this.editorUI = requireNonNull(editorUI);
        // without this, Pac-Man wallpaper shines through!
        editorUI.layoutPane().setBackground(colorBackground(Color.web("#dddddd"))); // JavaFX default grey
    }

    public TileMapEditor editor() {
        return editorUI.editor();
    }

    @Override
    public ActionBindingsManager actionBindingsManager() { return DefaultActionBindingsManager.EMPTY_MAP; }

    @Override
    public Region root() {
        return editorUI.layoutPane();
    }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(editorUI.titleProperty()::get);
    }
}