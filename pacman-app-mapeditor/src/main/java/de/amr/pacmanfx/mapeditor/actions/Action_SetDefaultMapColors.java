/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapPropertyName;

import static de.amr.pacmanfx.mapeditor.EditorUtil.formatRGBA;
import static de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites.*;
import static java.util.Objects.requireNonNull;

public class Action_SetDefaultMapColors extends EditorAction<Void> {

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
        worldMap.terrainLayer().propertyMap().put(WorldMapPropertyName.COLOR_WALL_STROKE, formatRGBA(MS_PACMAN_COLOR_WALL_STROKE));
        worldMap.terrainLayer().propertyMap().put(WorldMapPropertyName.COLOR_WALL_FILL, formatRGBA(MS_PACMAN_COLOR_WALL_FILL));
        worldMap.terrainLayer().propertyMap().put(WorldMapPropertyName.COLOR_DOOR, formatRGBA(MS_PACMAN_COLOR_DOOR));
        worldMap.foodLayer()   .propertyMap().put(WorldMapPropertyName.COLOR_FOOD, formatRGBA(MS_PACMAN_COLOR_FOOD));

        editor.setTerrainMapPropertyChanged();
        editor.setFoodMapPropertyChanged();
        editor.setEdited(true);
        return null;
    }
}