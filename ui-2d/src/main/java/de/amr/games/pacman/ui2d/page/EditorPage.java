/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.ui2d.scene.GameSceneContext;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import static de.amr.games.pacman.ui2d.PacManGames2dUI.KEY_FULLSCREEN;

/**
 * @author Armin Reichert
 */
public class EditorPage extends BorderPane implements Page {

    private final BorderPane pane;
    private final GameSceneContext context;

    public EditorPage(TileMapEditor editor, GameSceneContext context) {
        pane = new BorderPane();
        pane.setCenter(editor.getLayout());
        pane.setTop(editor.getMenuBar());
        this.context = context;
    }

    @Override
    public Pane rootPane() {
        return pane;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public void setSize(double width, double height) {
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(KEY_FULLSCREEN)) {
            context.actionHandler().setFullScreen(true);
        }
    }
}
