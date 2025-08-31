package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditorUI;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.canPlaceFoodAtTile;
import static java.util.Objects.requireNonNull;

public class Action_FloodWithPellets extends AbstractEditorUIAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i startTile;

    public Action_FloodWithPellets(EditorUI ui, WorldMap worldMap, Vector2i startTile) {
        super(ui);
        this.worldMap = requireNonNull(worldMap);
        this.startTile = startTile;
    }

    @Override
    public Void execute() {
        if (!canPlacePelletAt(startTile)) {
            return null;
        }
        var q = new ArrayDeque<Vector2i>();
        Set<Vector2i> visited = new HashSet<>();
        q.offer(startTile);
        visited.add(startTile);
        while (!q.isEmpty()) {
            Vector2i current = q.poll();
            if (canPlacePelletAt(current)) {
                new Action_SetTileCode(ui, worldMap, LayerID.FOOD, current, FoodTile.PELLET.code()).execute();
            }
            for (Direction dir : Direction.values()) {
                Vector2i neighborTile = current.plus(dir.vector());
                if (!isAccessible(neighborTile)) continue;
                if  (!visited.contains(neighborTile)) {
                    q.offer(neighborTile);
                    visited.add(neighborTile);
                }
            }
        }
        for (Vector2i tile : actorTiles()) {
            clearBothTilesOccupiedByActor(tile);
        }

        return null;
    }

    // The actor tile stored as a map property is the left one of a pair of tiles which are reserved for the actor
    // initially. Both tiles are not allowed to carry food. So we clear both tiles after flooding.
    private void clearBothTilesOccupiedByActor(Vector2i leftActorTile) {
        new Action_SetTileCode(ui, worldMap, LayerID.FOOD, leftActorTile, FoodTile.EMPTY.code()).execute();
        Vector2i rightActorTile = leftActorTile.plus(Direction.RIGHT.vector());
        if (isAccessible(rightActorTile)) {
            new Action_SetTileCode(ui, worldMap, LayerID.FOOD, rightActorTile, FoodTile.EMPTY.code()).execute();
        }
    }

    private boolean isAccessible(Vector2i tile) {
        if (worldMap.outOfWorld(tile)) return false;
        return worldMap.layer(LayerID.TERRAIN).get(tile) == TerrainTile.EMPTY.code();
    }

    private boolean canPlacePelletAt(Vector2i tile) {
        if (!canPlaceFoodAtTile(worldMap, tile)) return false;
        // Avoid overwriting energizers
        return worldMap.layer(LayerID.FOOD).get(tile) != FoodTile.ENERGIZER.code();
    }

    private Set<Vector2i> actorTiles() {
        var actorTiles = new HashSet<Vector2i>();
        actorTile(WorldMapProperty.POS_PAC).ifPresent(actorTiles::add);
        actorTile(WorldMapProperty.POS_RED_GHOST).ifPresent(actorTiles::add);
        actorTile(WorldMapProperty.POS_PINK_GHOST).ifPresent(actorTiles::add);
        actorTile(WorldMapProperty.POS_CYAN_GHOST).ifPresent(actorTiles::add);
        actorTile(WorldMapProperty.POS_ORANGE_GHOST).ifPresent(actorTiles::add);
        actorTile(WorldMapProperty.POS_BONUS).ifPresent(actorTiles::add);
        return actorTiles;
    }

    private Optional<Vector2i> actorTile(String actorPosProperty) {
        var terrainProperties = worldMap.properties(LayerID.TERRAIN);
        String posString = terrainProperties.getOrDefault(actorPosProperty, null);
        return posString != null ? WorldMapParser.parseTile(posString) : Optional.empty();
    }
}