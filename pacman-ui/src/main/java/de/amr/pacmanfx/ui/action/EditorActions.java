/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class EditorActions {

    private final Game game;
    private final GameAction actionOpenEditor;

    private final Set<ActionKeyBinding> bindings;

    public EditorActions(Game game) {
        this.game = game;

        actionOpenEditor = new GameAction(game, "open_editor") {
            @Override
            protected void doAction() {
                openMapEditor().ifPresent(editor -> startEditor(editor));
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionOpenEditor, combine().alt().shift().key(KeyCode.E))
        );
    }

    /**
     * @param mapFile map file to edit or {@code null}
     * @return action which opens the map editor and edits the given map file if any
     */
    public GameAction createEditMapFileAction(File mapFile) {

        return new GameAction(game, "edit_map_file") {
            @Override
            protected void doAction() {
                openMapEditor().ifPresent(editor -> {
                    startEditor(editor);
                    if (mapFile != null) {
                        try {
                            editor.editFile(mapFile);
                        } catch (Exception x) {
                            game.ui().shortMessage("Cannot edit map file");
                            Logger.error(x, "Cannot edit map file {}", mapFile);
                        }
                    }
                });
            }
        };
    }

    public GameAction actionOpenEditor() {
        return actionOpenEditor;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    // Private

    private void startEditor(TileMapEditor editor) {
        game.stop();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor() {
        final EditorView editorView = game.ui().views().assertView(GameViewID.EDITOR, EditorView.class);
        editorView.ensureEditorCreated(game);
        if (!game.ui().views().trySelectEditorView(game)) {
            game.ui().shortMessage("Cannot open the map editor.");
            return Optional.empty();
        }
        return Optional.of(editorView.editor());
    }
}