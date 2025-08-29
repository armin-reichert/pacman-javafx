package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.canEditFoodAtTile;

public class Action_FloodWithPellets extends AbstractEditorUIAction<Void> {

    private final Vector2i startTile;
    private final FoodTile foodTile;

    public Action_FloodWithPellets(TileMapEditorUI ui, Vector2i startTile, FoodTile foodTile) {
        super(ui);
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
            new Action_SetTileCode(ui, editor.currentWorldMap(), LayerID.FOOD, current, foodTile.code()).execute();
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if  (!visited.contains(neighborTile) && canEditFoodAtTile(editor.currentWorldMap(), neighborTile)) {
                    q.push(neighborTile);
                }
                visited.add(neighborTile);
            }
        }
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
