/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import java.util.Set;

import static de.amr.pacmanfx.core.model.world.WorldMap.halfTileRightOf;

public class ArcadePacMan_ActorFactory {

    public static Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public static Ghost createGhost(
        byte personality,
        TerrainLayer terrain,
        House house,
        String startTileProperty,
        Set<Vector2i> specialTiles)
    {
        final Ghost ghost = switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> GhostFactory.createRedGhostShadow("Blinky");
            case GameModel.PINK_GHOST_SPEEDY  -> GhostFactory.createPinkGhostAmbusher("Pinky");
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Clyde");
            default -> throw new IllegalArgumentException("Unknown personality: " + personality);
        };
        ghost.setHome(house);
        ghost.setSpecialTerrainTiles(specialTiles);
        ghost.setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
        return ghost;
    }

}
