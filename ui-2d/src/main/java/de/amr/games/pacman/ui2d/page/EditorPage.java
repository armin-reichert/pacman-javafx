/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameKeys;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class EditorPage implements Page {

    private final BorderPane pane;
    private final GameContext context;

    public EditorPage(TileMapEditor editor, GameContext context) {
        checkNotNull(editor);
        this.context = checkNotNull(context);
        pane = new BorderPane();
        pane.setCenter(editor.getLayout());
        pane.setTop(editor.getMenuBar());
    }

    @Override
    public Pane rootPane() {
        return pane;
    }

    @Override
    public void handleKeyboardInput() {
        if (GameKeys.FULLSCREEN.pressed()) {
            context.actionHandler().setFullScreen(true);
        }
    }
}