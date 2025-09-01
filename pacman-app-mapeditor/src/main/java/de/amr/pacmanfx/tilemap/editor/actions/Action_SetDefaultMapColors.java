package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;

import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class Action_SetDefaultMapColors extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;

    public Action_SetDefaultMapColors(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_STROKE, MS_PACMAN_COLOR_WALL_STROKE);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_FILL, MS_PACMAN_COLOR_WALL_FILL);
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_DOOR, MS_PACMAN_COLOR_DOOR);
        worldMap.properties(LayerID.FOOD).put(WorldMapProperty.COLOR_FOOD, MS_PACMAN_COLOR_FOOD);
        editor.setTerrainMapChanged();
        editor.setFoodMapChanged();
        editor.setEdited(true);
        return null;
    }
}
