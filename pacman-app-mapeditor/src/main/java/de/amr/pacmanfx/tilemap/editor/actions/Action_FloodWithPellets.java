package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.canEditFoodAtTile;

public class Action_FloodWithPellets extends AbstractEditorAction<Void> {

    private final Vector2i startTile;
    private final FoodTile foodTile;

    public Action_FloodWithPellets(TileMapEditor editor, Vector2i startTile, FoodTile foodTile) {
        super(editor);
        this.startTile = startTile;
        this.foodTile = foodTile;
    }

    @Override
    public Void execute() {
        if (!canEditFoodAtTile(editor.currentWorldMap(), startTile)) {
            return null;
        }
        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.push(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            // use this method such that symmetric editing etc. is taken into account:
            editor.setTileValueRespectingSymmetry(editor.currentWorldMap(), LayerID.FOOD, current, foodTile.code());
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if  (!visited.contains(neighborTile) && canEditFoodAtTile(editor.currentWorldMap(), neighborTile)) {
                    q.push(neighborTile);
                }
                visited.add(neighborTile);
            }
        }
        editor.changeManager().setFoodMapChanged();
        editor.changeManager().setEdited(true);
        return null;
    }
}
