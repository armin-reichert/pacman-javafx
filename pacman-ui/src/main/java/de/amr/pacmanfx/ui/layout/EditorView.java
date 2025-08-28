/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.ActionBindingsManager;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.uilib.Ufx.colorBackground;
import static java.util.Objects.requireNonNull;

public class EditorView implements GameUI_View {

    private final TileMapEditor editor;

    public EditorView(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
        // without this, Pac-Man wallpaper shines through!
        editor.ui().layoutPane().setBackground(colorBackground(Color.web("#dddddd"))); // JavaFX default grey
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsManager actionBindingsManager() { return DefaultActionBindingsManager.EMPTY_MAP; }

    @Override
    public Region root() {
        return editor.ui().layoutPane();
    }

    @Override
    public Optional<Supplier<String>> titleSupplier() {
        return Optional.of(editor.titleProperty()::get);
    }
}