/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import static de.amr.pacmanfx.core.model.world.WorldMap.halfTileRightOf;

public final class TengenMsPacMan_ActorFactory {

    public Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public Pac createMsPacMan() {
        final var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public Ghost createRedGhost() {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, "Blinky");
        registerCommonComponents(ghost);
        ghost.setComponent(Elroy.class, new Elroy());
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
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, "Sue");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public void initWorldPlacement(
        Ghost ghost,
        TerrainLayer terrain,
        House house,
        String startTileProperty)
    {
        ghost.worldPlacement().setHouse(house);
        ghost.worldPlacement().setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
    }

    private void registerCommonComponents(Ghost ghost) {
        ghost.setComponent(Movement.class, new Movement());
        ghost.setComponent(WorldNavigation.class, new WorldNavigation());
        ghost.setComponent(GhostStateComponent.class, new GhostStateComponent());
        ghost.setComponent(GhostWorldPlacement.class, new  GhostWorldPlacement());
        ghost.setComponent(SpriteAnim.class, new SpriteAnim());
        //TODO where does this belong?
        ghost.worldNavigation().corneringSpeedDelta = -1.25f;
    }
}
