package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_SetTerrainProperty extends AbstractEditorAction<Void> {

    private final String propertyName;
    private final String value;

    public Action_SetTerrainProperty(TileMapEditor editor, String propertyName, String value) {
        super(editor);
        this.propertyName = requireNonNull(propertyName);
        this.value = requireNonNull(value);
    }

    @Override
    public Void execute() {
        if (editor.currentWorldMap().properties(LayerID.TERRAIN).containsKey(propertyName)
            && editor.currentWorldMap().properties(LayerID.TERRAIN).get(propertyName).equals(value)) {
            return null;
        }
        editor.currentWorldMap().properties(LayerID.TERRAIN).put(propertyName, value);
        editor.changeManager().setTerrainMapChanged();
        editor.changeManager().setEdited(true);
        return null;
    }
}
