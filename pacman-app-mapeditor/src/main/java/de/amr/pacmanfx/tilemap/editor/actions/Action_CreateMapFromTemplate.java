package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BEFORE_MAZE;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.EMPTY_ROWS_BELOW_MAZE;

public class Action_CreateMapFromTemplate extends AbstractEditorAction<Void> {

    private final Image image;

    public Action_CreateMapFromTemplate(TileMapEditor editor, Image image) {
        super(editor);
        this.image = image;
    }

    @Override
    public Void execute() {
        int numRows = EMPTY_ROWS_BEFORE_MAZE + EMPTY_ROWS_BELOW_MAZE + (int) (image.getHeight() / TS);
        int numCols = (int) (image.getWidth() / TS);
        WorldMap emptyMap = new Action_CreateEmptyMap(editor, numRows, numCols).execute();
        emptyMap.properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_WALL_FILL);
        emptyMap.properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_WALL_STROKE);
        emptyMap.properties(LayerID.TERRAIN).remove(WorldMapProperty.COLOR_DOOR);
        emptyMap.properties(LayerID.FOOD).remove(WorldMapProperty.COLOR_FOOD);
        editor.setCurrentWorldMap(emptyMap);
        editor.setEdited(true);
        return null;
    }
}
