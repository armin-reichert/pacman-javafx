/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.layout;

import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui.ActionBindingMap;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.uilib.Ufx.coloredBackground;
import static java.util.Objects.requireNonNull;

public class EditorView implements PacManGames_View {

    private final BorderPane layout = new BorderPane();
    private final TileMapEditor editor;

    public EditorView(TileMapEditor editor) {
        this.editor = requireNonNull(editor);
        // without this, Pac-Man wallpaper shines through!
        layout.setBackground(coloredBackground(Color.web("#dddddd"))); // JavaFX default grey
        layout.setCenter(editor.getContentPane());
        layout.setTop(editor.getMenuBar());
    }

    public TileMapEditor editor() {
        return editor;
    }

    @Override
    public ActionBindingMap actionBindingMap() {
        return ActionBindingMap.EMPTY_MAP;
    }

    @Override
    public Region rootNode() {
        return layout;
    }

    @Override
    public StringProperty title() {
        return editor.titleProperty();
    }
}