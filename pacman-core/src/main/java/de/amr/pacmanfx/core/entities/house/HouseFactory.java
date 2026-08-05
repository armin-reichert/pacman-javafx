/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.house;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.house.comp.HouseFloorplanComp;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import static de.amr.pacmanfx.core.model.world.map.TerrainTile.*;
import static java.util.Objects.requireNonNull;

public class HouseFactory {

    /**
     * Size of house in tiles (x=width, y=height).
     */
    public static final Vector2i ARCADE_HOUSE_SIZE_IN_TILES = WorldMap.tile(8, 5);

    public static final byte[][] ARCADE_HOUSE_TILES = {
        { ARC_NW.$, WALL_H.$, WALL_H.$, DOOR.$,   DOOR.$,   WALL_H.$, WALL_H.$, ARC_NE.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { WALL_V.$, EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  EMPTY.$,  WALL_V.$ },
        { ARC_SW.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, WALL_H.$, ARC_SE.$ }
    };

    public static House createArcadeHouse(Vector2i minTile) {
        final House house = new House();
        final HouseFloorplanComp floorplan = house.floorplan();

        floorplan.setContent(Ufx.copyOf(ARCADE_HOUSE_TILES));

        floorplan.setMinTile(requireNonNull(minTile));
        floorplan.setMaxTile(minTile.plus(ARCADE_HOUSE_SIZE_IN_TILES).minus(1, 1));
        floorplan.setLeftDoorTile(minTile.plus(3, 0));
        floorplan.setRightDoorTile(minTile.plus(4, 0));

        floorplan.setEntryPosition(
            floorplan.rightDoorTile().toVector2f()
            .scaled(WorldMap.TS)
            .minus(WorldMap.HTS, WorldMap.TS));

        floorplan.ghostRevivalTileMap().put(GhostPersonality.RED_GHOST_SHADOW,   minTile.plus(3, 2));
        floorplan.ghostRevivalTileMap().put(GhostPersonality.PINK_GHOST_SPEEDY,  minTile.plus(3, 2));
        floorplan.ghostRevivalTileMap().put(GhostPersonality.CYAN_GHOST_BASHFUL, minTile.plus(1, 2));
        floorplan.ghostRevivalTileMap().put(GhostPersonality.ORANGE_GHOST_POKEY, minTile.plus(5, 2));

        floorplan.ghostStartDirectionMap().put(GhostPersonality.RED_GHOST_SHADOW,   Direction.LEFT);
        floorplan.ghostStartDirectionMap().put(GhostPersonality.PINK_GHOST_SPEEDY,  Direction.DOWN);
        floorplan.ghostStartDirectionMap().put(GhostPersonality.CYAN_GHOST_BASHFUL, Direction.UP);
        floorplan.ghostStartDirectionMap().put(GhostPersonality.ORANGE_GHOST_POKEY, Direction.UP);

        return house;
    }
}
