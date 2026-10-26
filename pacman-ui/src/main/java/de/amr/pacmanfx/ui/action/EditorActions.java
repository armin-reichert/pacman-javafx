/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.engine.input.KeyCodeCombinationBuilder.combine;
import static java.util.Objects.requireNonNull;

public class EditorActions {

    private final GameAction<EngineContext> actionOpenEditor;

    private final Set<ActionKeyBinding<EngineContext>> bindings;

    public EditorActions() {

        actionOpenEditor = new GameAction<>("open_editor") {
            @Override
            public void execute(EngineContext context) {
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
    public GameAction<EngineContext> createEditMapFileAction(File mapFile) {

        return new GameAction<>("edit_map_file") {
            @Override
            public void execute(EngineContext engine) {
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

    public GameAction<EngineContext> actionOpenEditor() {
        return actionOpenEditor;
    }

    public Set<ActionKeyBinding<EngineContext>> bindings() {
        return bindings;
    }

    // Private

    private void startEditor(EngineContext engine, TileMapEditor editor) {
        engine.lifeCycle().endSession();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor(EngineContext engine) {
        final GameUI ui = engine.ui();
        final EditorView editorView = engine.viewManager().assertView(GameViewID.EDITOR, EditorView.class);
        editorView.ensureEditorCreated(engine);
        if (!trySelectEditorView(engine)) {
            ui.showMessage("Cannot open the map editor.");
            return Optional.empty();
        }
        return Optional.of(editorView.editor());
    }

    private boolean trySelectEditorView(EngineContext engine) {
        requireNonNull(engine);
        if (engine.viewManager().optView(GameViewID.EDITOR, EditorView.class).isEmpty()) {
            Logger.info("Editor view has not been created yet");
            return false;
        }
        if (canOpenEditor(engine)) {
            engine.viewManager().currentViewIDProperty().set(GameViewID.EDITOR);
            return true;
        }
        else {
            Logger.info("Editor cannot open in current state");
            return false;
        }
    }

    private boolean canOpenEditor(EngineContext engine) {
        if (engine.viewManager().isSelected(GameViewID.START_PAGES)) {
            return true;
        }
        if (engine.viewManager().isSelected(GameViewID.GAMEPLAY)) {
            if (engine.optCurrentGame().isEmpty()) return true;
            return !engine.optCurrentGame().get().session().booleanValue(SessionKey.GAME_RUNNING);
        }
        return false;
    }
}