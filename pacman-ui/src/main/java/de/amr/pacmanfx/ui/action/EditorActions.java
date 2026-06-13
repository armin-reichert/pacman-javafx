package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameConstants;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import org.tinylog.Logger;

import java.io.File;
import java.util.Optional;

public class EditorActions {

    private final Game game;
    private final GameAction actionOpenEditor;

    public EditorActions(Game game) {
        this.game = game;

        actionOpenEditor = new GameAction(game, "open_editor") {
            @Override
            protected void doAction() {
                openMapEditor().ifPresent(editor -> startEditor(editor));
            }
        };
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
                            game.shortMessage("Cannot edit map file");
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

    private void startEditor(TileMapEditor editor) {
        game.stop();
        editor.init(GameConstants.CUSTOM_MAP_DIR);
        editor.start();
    }

    private Optional<TileMapEditor> openMapEditor() {
        final SubViewManager subViews = game.ui().subViews();
        subViews.ensureEditorViewCreated();

        final TileMapEditor editor = subViews.optEditorView().map(EditorView::editor).orElse(null);
        if (editor == null) {
            game.shortMessage("Cannot access the map editor.");
            return Optional.empty();
        }

        if (!subViews.trySelectEditorView()) {
            game.shortMessage("Cannot open the map editor.");
            return Optional.empty();
        }

        return Optional.of(editor);
    }
}