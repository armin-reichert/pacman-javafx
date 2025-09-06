package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditMode;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseSize;

public class Action_CreateNewMapInteractively extends AbstractEditorUIAction<Void> {

    private final boolean preconfigured;

    public Action_CreateNewMapInteractively(EditorUI ui, boolean preconfigured) {
        super(ui);
        this.preconfigured = preconfigured;
    }

    @Override
    public Void execute() {
        ui.afterCheckForUnsavedChanges(() -> {
            TextInputDialog dialog = createMapSizeInputDialog();
            dialog.showAndWait().ifPresent(input -> {
                Vector2i sizeInTiles = parseSize(input);
                if (sizeInTiles == null) {
                    ui.messageDisplay().showMessage("Map size not recognized", 2, MessageType.ERROR);
                }
                else if (sizeInTiles.y() < 6) {
                    ui.messageDisplay().showMessage("Map must have at least 6 rows", 2, MessageType.ERROR);
                }
                else {
                    if (preconfigured) {
                        createPreconfiguredMap(editor, sizeInTiles.y(), sizeInTiles.x());
                    } else {
                        WorldMap emptyMap = new Action_CreateEmptyMap(editor, sizeInTiles.y(), sizeInTiles.x()).execute();
                        editor.setCurrentWorldMap(emptyMap);
                    }
                    editor.setCurrentFile(null);
                    editor.setTemplateImage(null);
                    if (ui.editModeIs(EditMode.INSPECT)) {
                        ui.setEditMode(EditMode.EDIT);
                        editor.setSymmetricEditMode(true);
                    }
                }
            });
        });
        return null;
    }

    private TextInputDialog createMapSizeInputDialog() {
        var dialog = new TextInputDialog("28x36");
        dialog.setTitle(translated("new_dialog.title"));
        dialog.setHeaderText(translated("new_dialog.header_text"));
        dialog.setContentText(translated("new_dialog.content_text"));
        return dialog;
    }

    private void createPreconfiguredMap(TileMapEditor editor, int numRows, int numCols) {
        editor.setCurrentWorldMap(WorldMap.emptyMap(numCols, numRows));
        WorldMap worldMap = editor.currentWorldMap();
        new Action_SetDefaultMapColors(editor).execute();
        new Action_SetDefaultScatterPositions(editor).execute();
        new Action_AddBorderWall(editor).execute();
        if (worldMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(numCols / 2 - 4, numRows / 2 - 3);
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PAC,   formatTile(houseMinTile.plus(3, 11)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_BONUS, formatTile(houseMinTile.plus(3, 5)));
            new Action_PlaceArcadeHouse(editor, houseMinTile).execute();
        }
        worldMap.buildObstacleList();
    }
}