package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_DeleteMapProperty extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;
    private final LayerID layerID;
    private final String propertyName;

    public Action_DeleteMapProperty(TileMapEditor editor, WorldMap worldMap, LayerID layerID, String propertyName) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
        this.layerID = requireNonNull(layerID);
        this.propertyName = requireNonNull(propertyName);
    }

    @Override
    public Void execute() {
        if (worldMap.properties(layerID).containsKey(propertyName)) {
            worldMap.properties(layerID).remove(propertyName);
            if (layerID == LayerID.FOOD) editor.setFoodMapChanged();
            if (layerID == LayerID.TERRAIN) editor.setTerrainMapChanged();
            editor.setEdited(true);
        }
        return null;
    }
}