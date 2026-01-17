/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.model.world.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.mapeditor.EditorUtil.canPlaceFoodAtTile;

public class Action_FloodWithPellets extends EditorAction<Void> {

    private final Vector2i startTile;

    public Action_FloodWithPellets(TileMapEditor editor, Vector2i startTile) {
        super(editor);
        this.startTile = startTile;
    }

    @Override
    public Void execute() {
        WorldMap worldMap = editor.currentWorldMap();

        if (!canPlacePelletAt(worldMap, startTile)) {
            return null;
        }

        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.offer(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            if (canPlacePelletAt(worldMap, current)) {
                new Action_SetFoodTileCode(editor, current, FoodTile.PELLET.$).execute();
            }
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if (!isAccessible(worldMap, neighborTile)) continue;
                if  (!visited.contains(neighborTile)) {
                    q.offer(neighborTile);
                    visited.add(neighborTile);
                }
            }
        }
        for (Vector2i tile : actorTiles(worldMap)) {
            clearBothTilesOccupiedByActor(worldMap, tile);
        }

        return null;
    }

    // The actor tile stored as a map property is the left one of a pair of tiles which are reserved for the actor
    // initially. Both tiles are not allowed to carry food. So we clear both tiles after flooding.
    private void clearBothTilesOccupiedByActor(WorldMap worldMap, Vector2i leftActorTile) {
        new Action_SetFoodTileCode(editor, leftActorTile, FoodTile.EMPTY.$).execute();
        Vector2i rightActorTile = leftActorTile.plus(Direction.RIGHT.vector());
        if (isAccessible(worldMap, rightActorTile)) {
            new Action_SetFoodTileCode(editor, rightActorTile, FoodTile.EMPTY.$).execute();
        }
    }

    private boolean isAccessible(WorldMap worldMap, Vector2i tile) {
        if (worldMap.terrainLayer().outOfBounds(tile)) return false;
        return worldMap.terrainLayer().content(tile) == TerrainTile.EMPTY.$;
    }

    private boolean canPlacePelletAt(WorldMap worldMap, Vector2i tile) {
        if (!canPlaceFoodAtTile(worldMap, tile)) return false;
        // Avoid overwriting energizers
        return worldMap.foodLayer().content(tile) != FoodTile.ENERGIZER.$;
    }

    private Set<Vector2i> actorTiles(WorldMap worldMap) {
        var actorTiles = new HashSet<Vector2i>();
        actorTile(worldMap, WorldMapPropertyName.POS_PAC).ifPresent(actorTiles::add);
        actorTile(worldMap, WorldMapPropertyName.POS_GHOST_1_RED).ifPresent(actorTiles::add);
        actorTile(worldMap, WorldMapPropertyName.POS_GHOST_2_PINK).ifPresent(actorTiles::add);
        actorTile(worldMap, WorldMapPropertyName.POS_GHOST_3_CYAN).ifPresent(actorTiles::add);
        actorTile(worldMap, WorldMapPropertyName.POS_GHOST_4_ORANGE).ifPresent(actorTiles::add);
        actorTile(worldMap, WorldMapPropertyName.POS_BONUS).ifPresent(actorTiles::add);
        return actorTiles;
    }

    private Optional<Vector2i> actorTile(WorldMap worldMap, String actorPosProperty) {
        var terrainProperties = worldMap.terrainLayer().propertyMap();
        String posString = terrainProperties.getOrDefault(actorPosProperty, null);
        return posString != null ? WorldMap.parseTile(posString) : Optional.empty();
    }
}