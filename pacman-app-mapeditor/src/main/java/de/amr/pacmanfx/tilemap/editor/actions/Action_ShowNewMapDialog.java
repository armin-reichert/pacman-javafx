package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.parseSize;

public class Action_ShowNewMapDialog extends AbstractEditorAction<Void> {

    private final boolean preconfigured;

    public Action_ShowNewMapDialog(TileMapEditor editor, boolean preconfigured) {
        super(editor);
        this.preconfigured = preconfigured;
    }

    @Override
    public Void execute() {
        editor.ifNoUnsavedChangesDo(() -> {
            TextInputDialog dialog = createMapSizeInputDialog();
            dialog.showAndWait().ifPresent(input -> {
                Vector2i sizeInTiles = parseSize(input);
                if (sizeInTiles == null) {
                    editor.messageManager().showMessage("Map size not recognized", 2, MessageType.ERROR);
                }
                else if (sizeInTiles.y() < 6) {
                    editor.messageManager().showMessage("Map must have at least 6 rows", 2, MessageType.ERROR);
                }
                else {
                    if (preconfigured) {
                        setPreconfiguredMap(editor, sizeInTiles.x(), sizeInTiles.y());
                    } else {
                        editor.setCurrentWorldMap(editor.createEmptyMap(sizeInTiles.x(), sizeInTiles.y()));
                    }
                    editor.setCurrentFile(null);
                    editor.setTemplateImage(null);
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

    private void setPreconfiguredMap(TileMapEditor editor, int tilesX, int tilesY) {
        var worldMap = WorldMap.emptyMap(tilesY, tilesX);
        if (worldMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(tilesX / 2 - 4, tilesY / 2 - 3);
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PAC,   WorldMapFormatter.formatTile(houseMinTile.plus(3, 11)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_BONUS, WorldMapFormatter.formatTile(houseMinTile.plus(3, 5)));
            new Action_PlaceArcadeHouse(editor, worldMap, houseMinTile).execute();
        }
        worldMap.buildObstacleList();
        editor.setDefaultColors(worldMap);
        editor.setDefaultScatterPositions(worldMap);
        editor.setCurrentWorldMap(worldMap);
        new Action_AddBorderWall(editor, worldMap).execute();
    }
}