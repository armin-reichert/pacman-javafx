package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.model.WorldMapProperty;
import javafx.scene.control.TextInputDialog;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.translated;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.parseSize;

public class Action_ShowNewMapDialog extends AbstractEditorAction {

    public void setPreconfigureMap(boolean preconfigureMap) {
        setArg("preconfigured", preconfigureMap);
    }

    @Override
    public Object execute(TileMapEditor editor) {
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
                        setPreconfiguredMap(editor, sizeInTiles.x(), sizeInTiles.y());
                    } else {
                        editor.setBlankMap(sizeInTiles.x(), sizeInTiles.y());
                    }
                    editor.currentFileProperty().set(null);
                }
            });
        });
        return null;
    }

    private void setPreconfiguredMap(TileMapEditor editor, int tilesX, int tilesY) {
        var worldMap = WorldMap.emptyMap(tilesY, tilesX);
        EditorActions.ADD_BORDER_WALL.setWorldMap(worldMap);
        EditorActions.ADD_BORDER_WALL.execute(editor);
        editor.setDefaultScatterPositions(worldMap);
        if (worldMap.numRows() >= 20) {
            Vector2i houseMinTile = Vector2i.of(tilesX / 2 - 4, tilesY / 2 - 3);
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_PAC,   WorldMapFormatter.formatTile(houseMinTile.plus(3, 11)));
            worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.POS_BONUS, WorldMapFormatter.formatTile(houseMinTile.plus(3, 5)));
            EditorActions.PLACE_ARCADE_HOUSE.setHouseMinTile(houseMinTile);
            EditorActions.PLACE_ARCADE_HOUSE.setWorldMap(worldMap);
            EditorActions.PLACE_ARCADE_HOUSE.execute(editor);
        }
        worldMap.buildObstacleList();
        editor.setDefaultColors(worldMap);
        editor.setEditedWorldMap(worldMap);
    }
}