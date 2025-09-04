package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.EditorUI;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.canPlaceFoodAtTile;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.mirroredTileCode;
import static java.util.Objects.requireNonNull;

/**
 * This action should be used whenever a tile value has to be set and symmetric editing should be taken into account.
 */
public class Action_SetTileCode extends AbstractEditorUIAction<Void> {

    private final LayerID layerID;
    private final Vector2i tile;
    private final byte code;

    public Action_SetTileCode(EditorUI ui, LayerID layerID, Vector2i tile, byte code) {
        super(ui);
        this.layerID = requireNonNull(layerID);
        this.tile = requireNonNull(tile);
        this.code = code;
    }

    public Action_SetTileCode(EditorUI ui, LayerID layerID, int row, int col, byte code) {
        this(ui, layerID, Vector2i.of(col, row), code);
    }

    @Override
    public Void execute() {
        boolean symmetric = ui.symmetricEditMode();
        switch (layerID) {
            case FOOD    -> setFoodTile(editor.currentWorldMap(), symmetric);
            case TERRAIN -> setTerrainTile(editor.currentWorldMap(), symmetric);
        }
        return null;
    }

    private void setTerrainTile(WorldMap worldMap, boolean symmetric) {
        byte oldCode = worldMap.layer(LayerID.TERRAIN).get(tile);
        if (code == oldCode) return;
        worldMap.setContent(LayerID.TERRAIN, tile, code);
        worldMap.setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        if (symmetric) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            byte mirroredCode = mirroredTileCode(code);
            byte oldMirroredCode = worldMap.layer(LayerID.TERRAIN).get(mirroredTile);
            if (mirroredCode != oldMirroredCode) {
                worldMap.setContent(LayerID.TERRAIN, mirroredTile, mirroredCode);
                worldMap.setContent(LayerID.FOOD, mirroredTile, FoodTile.EMPTY.code());
            }
        }
        editor.setEdited(true);
        editor.setWorldMapChanged();
    }

    private void setFoodTile(WorldMap worldMap, boolean symmetric) {
        if (!canPlaceFoodAtTile(worldMap, tile)) return;
        worldMap.setContent(LayerID.FOOD, tile, code);
        if (symmetric) {
            Vector2i mirroredTile = worldMap.mirroredTile(tile);
            if (canPlaceFoodAtTile(worldMap, mirroredTile)) {
                worldMap.setContent(LayerID.FOOD, mirroredTile, code);
            }
        }
        editor.setEdited(true);
        editor.setFoodMapChanged();
    }
}