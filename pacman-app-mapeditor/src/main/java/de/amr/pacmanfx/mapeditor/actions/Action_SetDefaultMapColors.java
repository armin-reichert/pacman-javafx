/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static de.amr.pacmanfx.mapeditor.EditorUtil.formatColor;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class Action_SetDefaultMapColors extends AbstractEditorAction<Void> {

    private final WorldMap worldMap;

    public Action_SetDefaultMapColors(TileMapEditor editor) {
        this(editor, editor.currentWorldMap());
    }

    public Action_SetDefaultMapColors(TileMapEditor editor, WorldMap worldMap) {
        super(editor);
        this.worldMap = requireNonNull(worldMap);
    }

    @Override
    public Void execute() {
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_STROKE, formatColor(MS_PACMAN_COLOR_WALL_STROKE));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_WALL_FILL, formatColor(MS_PACMAN_COLOR_WALL_FILL));
        worldMap.properties(LayerID.TERRAIN).put(WorldMapProperty.COLOR_DOOR, formatColor(MS_PACMAN_COLOR_DOOR));
        worldMap.properties(LayerID.FOOD).put(WorldMapProperty.COLOR_FOOD, formatColor(MS_PACMAN_COLOR_FOOD));

        editor.setTerrainMapPropertyChanged();
        editor.setFoodMapPropertyChanged();
        editor.setEdited(true);
        return null;
    }
}