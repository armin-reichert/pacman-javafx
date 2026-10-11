/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.runtime.action.ActionKeyBinding;
import de.amr.pacmanfx.ui.GameEngineUIContext;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;

public class EditorActions {

    private final GameAction<GameEngineUIContext> actionOpenEditor;

    private final Set<ActionKeyBinding> bindings;

    public EditorActions() {

        actionOpenEditor = new GameAction<>("open_editor") {
            @Override
            public void execute(GameEngineUIContext context) {
                openMapEditor(context).ifPresent(editor -> startEditor(context, editor));
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
    public GameAction<GameEngineUIContext> createEditMapFileAction(File mapFile) {

        return new GameAction<>("edit_map_file") {
            @Override
            public void execute(GameEngineUIContext engineContext) {
                openMapEditor(engineContext).ifPresent(editor -> {
                    startEditor(engineContext, editor);
                    if (mapFile != null) {
                        try {
                            editor.editFile(mapFile);
                        } catch (Exception x) {
                            //engine.ui().shortMessage("Cannot edit map file");
                            Logger.error(x, "Cannot edit map file {}", mapFile);
                        }
                    }
                });
            }
        };
    }

    public GameAction<GameEngineUIContext> actionOpenEditor() {
        return actionOpenEditor;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }

    // Private

    private void startEditor(GameEngineUIContext engineContext, TileMapEditor editor) {
        engineContext.engineLife().suspendGame();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor(GameEngineUIContext engineContext) {
        final GameUI ui = engineContext.ui();
        final EditorView editorView = ui.viewManager().reqView(GameViewID.EDITOR, EditorView.class);
        editorView.ensureEditorCreated(engineContext);
        if (!ui.viewManager().trySelectEditorView(engineContext)) {
            ui.shortMessage("Cannot open the map editor.");
            return Optional.empty();
        }
        return Optional.of(editorView.editor());
    }
}