package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.EditorUI;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.canEditFoodAtTile;

public class Action_FloodWithPellets extends AbstractEditorUIAction<Void> {

    private final Vector2i startTile;
    private final FoodTile foodTile;

    public Action_FloodWithPellets(EditorUI ui, Vector2i startTile, FoodTile foodTile) {
        super(ui);
        this.startTile = startTile;
        this.foodTile = foodTile;
    }

    @Override
    public Void execute() {
        final WorldMap worldMap = editor.currentWorldMap();
        if (!canEditFoodAtTile(worldMap, startTile)) {
            return null;
        }
        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.offer(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            new Action_SetTileCode(ui, worldMap, LayerID.FOOD, current, foodTile.code()).execute();
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if  (!visited.contains(neighborTile)
                    && canEditFoodAtTile(worldMap, neighborTile)
                    && worldMap.layer(LayerID.FOOD).get(neighborTile.y(), neighborTile.x()) == FoodTile.EMPTY.code()) {
                    q.offer(neighborTile);
                    visited.add(neighborTile);
                }
            }
        }
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
