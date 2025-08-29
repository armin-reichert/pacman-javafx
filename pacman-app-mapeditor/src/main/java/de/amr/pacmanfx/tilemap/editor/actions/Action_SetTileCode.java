package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.canEditFoodAtTile;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.mirroredTileCode;
import static java.util.Objects.requireNonNull;

/**
 * This action should be used whenever a tile value has to be set and symmetric editing should be taken into account.
 */
public class Action_SetTileCode extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;
    private final LayerID layerID;
    private final Vector2i tile;
    private final byte code;

    public Action_SetTileCode(TileMapEditor editor, WorldMap worldMap, LayerID layerID, Vector2i tile, byte code) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.layerID = requireNonNull(layerID);
        this.tile = requireNonNull(tile);
        this.code = code;
    }

    public Action_SetTileCode(TileMapEditor editor, WorldMap worldMap, LayerID layerID, int row, int col, byte code) {
        this(editor, worldMap, layerID, Vector2i.of(col, row), code);
    }

    @Override
    public Void execute() {
        boolean symmetric = editor.symmetricEditMode();
        switch (layerID) {
            case FOOD    -> setFoodTile(symmetric);
            case TERRAIN -> setTerrainTile(symmetric);
        }
        return null;
    }

    private void setTerrainTile(boolean symmetric) {
        worldMap.setContent(LayerID.TERRAIN, tile, code);
        worldMap.setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        if (symmetric) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            byte mirroredCode = mirroredTileCode(code);
            worldMap.setContent(LayerID.TERRAIN, mirroredTile, mirroredCode);
            worldMap.setContent(LayerID.FOOD, mirroredTile, FoodTile.EMPTY.code());
        }
        editor.setEdited(true);
        editor.setWorldMapChanged();
    }

    private void setFoodTile(boolean symmetric) {
        if (!canEditFoodAtTile(worldMap, tile)) return;
        worldMap.setContent(LayerID.FOOD, tile, code);
        if (symmetric) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            if (canEditFoodAtTile(worldMap, mirroredTile)) {
                worldMap.setContent(LayerID.FOOD, mirroredTile, code);
            }
        }
        editor.setEdited(true);
        editor.setFoodMapChanged();
    }
}