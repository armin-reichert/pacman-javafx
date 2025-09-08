/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

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
        var terrainProperties = editor.currentWorldMap().properties(LayerID.TERRAIN);
        if (terrainProperties.containsKey(propertyName) && terrainProperties.get(propertyName).equals(value)) {
            return null;
        }
        terrainProperties.put(propertyName, value);
        editor.setTerrainMapChanged();
        editor.setEdited(true);
        return null;
    }
}
