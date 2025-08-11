/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui.ActionBindingsManager;
import de.amr.pacmanfx.ui.DefaultActionBindingsManager;
import javafx.beans.binding.StringExpression;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Optional;

import static de.amr.pacmanfx.uilib.Ufx.colorBackground;
import static java.util.Objects.requireNonNull;

public class EditorView implements GameUI_View {

    private final BorderPane layout = new BorderPane();
    private final TileMapEditor editor;

    public EditorView(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
        // without this, Pac-Man wallpaper shines through!
        layout.setBackground(colorBackground(Color.web("#dddddd"))); // JavaFX default grey
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingsManager actionBindingsManager() { return DefaultActionBindingsManager.EMPTY_MAP; }

    @Override
    public Region root() {
        return layout;
    }

    @Override
    public Optional<? extends StringExpression> title() {
        return Optional.of(editor.titleProperty());
    }
}