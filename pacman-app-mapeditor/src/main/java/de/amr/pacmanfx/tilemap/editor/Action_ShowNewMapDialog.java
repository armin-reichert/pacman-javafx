package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.parseSize;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.translated;

public class Action_ShowNewMapDialog extends AbstractEditorAction {

    public void setPreconfigureMap(boolean preconfigureMap) {
        setArg("preconfigured", preconfigureMap);
    }

    @Override
    public void execute(TileMapEditor editor) {
        boolean preconfigured = getArg("preconfigured", Boolean.class);
        editor.executeWithCheckForUnsavedChanges(() -> {
            var dialog = new TextInputDialog("28x36");
            dialog.setTitle(translated("new_dialog.title"));
            dialog.setHeaderText(translated("new_dialog.header_text"));
            dialog.setContentText(translated("new_dialog.content_text"));
            dialog.showAndWait().ifPresent(text -> {
                Vector2i sizeInTiles = parseSize(text);
                if (sizeInTiles == null) {
                    editor.showMessage("Map size not recognized", 2, MessageType.ERROR);
                }
                else if (sizeInTiles.y() < 6) {
                    editor.showMessage("Map must have at least 6 rows", 2, MessageType.ERROR);
                }
                else {
                    if (preconfigured) {
                        editor.setPreconfiguredMap(sizeInTiles.x(), sizeInTiles.y());
                    } else {
                        editor.setBlankMap(sizeInTiles.x(), sizeInTiles.y());
                    }
                    editor.currentFileProperty().set(null);
                }
            });
        });
    }
}