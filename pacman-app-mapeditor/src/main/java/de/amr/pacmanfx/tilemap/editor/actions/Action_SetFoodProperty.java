package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_SetFoodProperty extends AbstractEditorAction<Void> {

    private final String propertyName;
    private final String value;

    public Action_SetFoodProperty(TileMapEditor editor, String propertyName, String value) {
        super(editor);
        this.propertyName = requireNonNull(propertyName);
        this.value = requireNonNull(value);
    }

    @Override
    public Void execute() {
        var foodProperties = editor.currentWorldMap().properties(LayerID.FOOD);
        if (foodProperties.containsKey(propertyName) && foodProperties.get(propertyName).equals(value)) {
            return null;
        }
        foodProperties.put(propertyName, value);
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
