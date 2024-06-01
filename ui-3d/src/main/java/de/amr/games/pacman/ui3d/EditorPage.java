/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d;

import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.ui2d.page.Page;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * @author Armin Reichert
 */
public class EditorPage extends BorderPane implements Page {

    private final BorderPane pane;

    public EditorPage(TileMapEditor editor) {
        pane = new BorderPane();
        pane.setCenter(editor.getLayout());
        pane.setTop(editor.getMenuBar());
    }

    @Override
    public Pane rootPane() {
        return pane;
    }
}
