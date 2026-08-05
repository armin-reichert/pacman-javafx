/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.house.comp;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.EnumMap;
import java.util.Map;

public class HouseFloorplanComp implements GameEntityComponent {

    private byte[][] content;

    private Vector2i minTile;

    private Vector2i maxTile;

    private Vector2i leftDoorTile;

    private Vector2i rightDoorTile;

    private Vector2f entryPosition;

    private final Map<GhostPersonality, Direction> ghostStartDirectionMap;

    private final Map<GhostPersonality, Vector2i> ghostRevivalTileMap;

    public HouseFloorplanComp() {
        ghostStartDirectionMap = new EnumMap<>(GhostPersonality.class);
        ghostRevivalTileMap = new EnumMap<>(GhostPersonality.class);
    }

    public byte[][] content() {
        return content;
    }

    public void setContent(byte[][] content) {
        this.content = content;
    }

    public Vector2i minTile() {
        return minTile;
    }

    public void setMinTile(Vector2i minTile) {
        this.minTile = minTile;
    }

    public Vector2i maxTile() {
        return maxTile;
    }

    public void setMaxTile(Vector2i maxTile) {
        this.maxTile = maxTile;
    }

    public Vector2i leftDoorTile() {
        return leftDoorTile;
    }

    public void setLeftDoorTile(Vector2i leftDoorTile) {
        this.leftDoorTile = leftDoorTile;
    }

    public Vector2i rightDoorTile() {
        return rightDoorTile;
    }

    public void setRightDoorTile(Vector2i rightDoorTile) {
        this.rightDoorTile = rightDoorTile;
    }

    public Vector2f entryPosition() {
        return entryPosition;
    }

    public void setEntryPosition(Vector2f entryPosition) {
        this.entryPosition = entryPosition;
    }

    public Map<GhostPersonality, Direction> ghostStartDirectionMap() {
        return ghostStartDirectionMap;
    }

    public Direction ghostStartDirection(GhostPersonality personality) {
        return  ghostStartDirectionMap.get(personality);
    }

    public Map<GhostPersonality, Vector2i> ghostRevivalTileMap() {
        return ghostRevivalTileMap;
    }

    public Vector2i ghostRevivalTile(GhostPersonality personality) {
        return  ghostRevivalTileMap.get(personality);
    }

    @Override
    public void reset() {
    }
}
