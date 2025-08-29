package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

/**
 * Ignores symmetric edit mode!
 */
public class Action_ClearFoodTile extends AbstractEditorAction<Void> {

    private final Vector2i tile;

    public Action_ClearFoodTile(TileMapEditor editor, Vector2i tile) {
        super(editor);
        this.tile = requireNonNull(tile);
    }

    @Override
    public Void execute() {
        editor.currentWorldMap().setContent(LayerID.FOOD, tile, FoodTile.EMPTY.code());
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
