/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;
import static java.util.Objects.requireNonNull;

public class EditorActions {

    private final GameAction<EngineUIContext> actionOpenEditor;

    private final Set<ActionKeyBinding<EngineUIContext>> bindings;

    public EditorActions() {

        actionOpenEditor = new GameAction<>("open_editor") {
            @Override
            public void execute(EngineUIContext context) {
                openMapEditor(context).ifPresent(editor -> startEditor(context, editor));
            }
        };

        bindings = Set.of(
            new ActionKeyBinding<>(actionOpenEditor, combine().alt().shift().key(KeyCode.E))
        );
    }

    /**
     * @param mapFile map file to edit or {@code null}
     * @return action which opens the map editor and edits the given map file if any
     */
    public GameAction<EngineUIContext> createEditMapFileAction(File mapFile) {

        return new GameAction<>("edit_map_file") {
            @Override
            public void execute(EngineUIContext engine) {
                openMapEditor(engine).ifPresent(editor -> {
                    startEditor(engine, editor);
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

    public GameAction<EngineUIContext> actionOpenEditor() {
        return actionOpenEditor;
    }

    public Set<ActionKeyBinding<EngineUIContext>> bindings() {
        return bindings;
    }

    // Private

    private void startEditor(EngineUIContext engine, TileMapEditor editor) {
        engine.lifeCycle().endSession();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor(EngineUIContext engine) {
        final GameUI ui = engine.ui();
        final EditorView editorView = engine.viewManager().editorView();
        editorView.ensureEditorCreated(engine);
        if (!trySelectEditorView(engine)) {
            ui.showMessage("Cannot open the map editor.");
            return Optional.empty();
        }
        return Optional.of(editorView.editor());
    }

    private boolean trySelectEditorView(EngineUIContext engine) {
        requireNonNull(engine);
        if (canOpenEditor(engine)) {
            engine.viewManager().selectEditorView();
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }

    private boolean canOpenEditor(EngineUIContext engine) {
        if (engine.viewManager().isStartPagesViewSelected()) {
            return true;
        }
        if (engine.viewManager().isGamePlayViewSelected()) {
            if (engine.optCurrentGame().isEmpty()) return true;
            return !engine.optCurrentGame().get().session().booleanValue(SessionKey.GAME_RUNNING);
        }
        return false;
    }
}