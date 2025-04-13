/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.uilib.Action;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.StringExpression;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class EditorView implements View {

    private final BorderPane layout = new BorderPane();
    private final TileMapEditor editor;

    public EditorView(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
        // without this, Pac-Man wallpaper shines through
        layout.setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());
    }

    @Override
    public Region layoutRoot() {
        return layout;
    }

    @Override
    public void update() {}

    @Override
    public StringExpression title() {
        return editor.titleProperty();
    }

    @Override
    public void bindActions() {}

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return Map.of();
    }
}