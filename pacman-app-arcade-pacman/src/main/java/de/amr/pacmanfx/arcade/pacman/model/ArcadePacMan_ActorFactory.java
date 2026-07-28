/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import java.util.Set;

import static de.amr.pacmanfx.core.model.world.WorldMap.halfTileRightOf;

public class ArcadePacMan_ActorFactory {

    public Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public Ghost createRedGhost() {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, "Blinky");
        registerCommonComponents(ghost);
        ghost.registerComponent(Elroy.class, new Elroy());
        ghost.reset();
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, "Pinky");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createCyanGhost() {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, "Inky");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, "Clyde");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public void initWorldPlacement(
        Ghost ghost,
        TerrainLayer terrain,
        House house,
        String startTileProperty,
        Set<Vector2i> specialTiles)
    {
        ghost.worldPlacement().setHouse(house);
        ghost.worldPlacement().setSpecialTerrainTiles(specialTiles);
        ghost.worldPlacement().setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
    }

    private void registerCommonComponents(Ghost ghost) {
        ghost.registerComponent(Movement.class, new Movement());
        ghost.registerComponent(WorldNavigation.class, new WorldNavigation());
        ghost.registerComponent(GhostWorldPlacement.class, new GhostWorldPlacement());
        ghost.registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        ghost.registerComponent(GhostStateComponent.class, new GhostStateComponent());
        ghost.registerComponent(SpriteAnim.class, new SpriteAnim());
        //TODO where does this belong?
        ghost.worldNavigation().corneringSpeedDelta = -1.25f;
    }
}
