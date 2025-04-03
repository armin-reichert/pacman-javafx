/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.tilemap.editor.TileMapEditor;
import de.amr.games.pacman.ui.Action;
import de.amr.games.pacman.ui.View;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.StringExpression;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static de.amr.games.pacman.Globals.assertNotNull;

public class EditorView extends BorderPane implements View {

    private final Map<KeyCodeCombination, Action> actionBindings = new HashMap<>();
    private final TileMapEditor editor;

    public EditorView(TileMapEditor editor) {
        this.editor = assertNotNull(editor);
        // without this, Pac-Man wallpaper shines through
        setBackground(Ufx.coloredBackground(Color.web("#dddddd"))); // JavaFX default grey
        setCenter(editor.getContentPane());
        setTop(editor.getMenuBar());
    }

    @Override
    public Region layoutRoot() {
        return this;
    }

    @Override
    public void onTick() {}

    @Override
    public StringExpression title() {
        return editor.titleProperty();
    }

    @Override
    public void bindGameActions() {}

    @Override
    public Map<KeyCodeCombination, Action> actionBindings() {
        return actionBindings;
    }

    public TileMapEditor editor() {
        return editor;
    }
}