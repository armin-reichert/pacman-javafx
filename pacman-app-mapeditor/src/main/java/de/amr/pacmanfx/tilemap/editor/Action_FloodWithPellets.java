package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.canEditFoodAtTile;

public class Action_FloodWithPellets extends AbstractEditorAction {

    public void setStartTile(Vector2i tile) {
        setArg("startTile", tile);
    }

    public void setPelletValue(byte value) {
        setArg("pelletValue", value);
    }

    @Override
    public Object execute(TileMapEditor editor) {
        Vector2i startTile = getArg("startTile", Vector2i.class);
        Byte pelletValue = getArg("pelletValue", Byte.class);
        if (!canEditFoodAtTile(editor.editedWorldMap(), startTile)) {
            return null;
        }
        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.push(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            // use this method such that symmmetric editing etc. is taken into account:
            editor.setTileValueRespectingSymmetry(editor.editedWorldMap(), LayerID.FOOD, current, pelletValue);
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if  (!visited.contains(neighborTile) && canEditFoodAtTile(editor.editedWorldMap(), neighborTile)) {
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
