package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.canEditFoodAtTile;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.mirroredTileValue;
import static java.util.Objects.requireNonNull;

/**
 * This method should be used whenever a tile value has to be set and symmetric editing should be executed.
 */
public class Action_SetTileValue extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;
    private final LayerID layerID;
    private final Vector2i tile;
    private final byte code;

    public Action_SetTileValue(TileMapEditor editor, WorldMap worldMap, LayerID layerID, Vector2i tile, byte code) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.layerID = requireNonNull(layerID);
        this.tile = requireNonNull(tile);
        this.code = code;
    }


    @Override
    public Void execute() {
        if (layerID == LayerID.FOOD && !canEditFoodAtTile(worldMap, tile)) {
            return null;
        }

        worldMap.setContent(layerID, tile, code);
        if (layerID == LayerID.TERRAIN) {
            worldMap.setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        }

        if (editor.isSymmetricEditMode()) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            if (layerID == LayerID.FOOD) {
                if (canEditFoodAtTile(worldMap, mirroredTile)) {
                    worldMap.setContent(layerID, mirroredTile, code);
                }
            } else {
                byte mirroredValue = mirroredTileValue(code);
                worldMap.setContent(layerID, mirroredTile, mirroredValue);
                worldMap.setContent(LayerID.FOOD, mirroredTile, FoodTile.EMPTY.code());
            }
        }

        editor.changeManager().setEdited(true);
        editor.changeManager().setWorldMapChanged();

        return null;
    }
}
