/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model.actor;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingPhase;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;

public interface TengenMsPacMan_ActorFactory {

    static Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    static Pac createMsPacMan() {
        final var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    /**
     * In Arcade Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     * <p>
     * I use the same behavior here, however I do not know what the real Tengen implementation does.
     * </p>
     */
    static Ghost createGhost(byte personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> applyModifiedShadowBehavior(new RedGhostShadow("Blinky"));
            case PINK_GHOST_SPEEDY -> applyModifiedAmbushBehavior(new PinkGhostAmbusher("Pinky"));
            case CYAN_GHOST_BASHFUL -> new CyanGhostBashful("Inky");
            case ORANGE_GHOST_POKEY -> new OrangeGhostPokey("Sue");
            default -> throw new IllegalArgumentException();
        };
    }

    private static Ghost applyModifiedShadowBehavior(RedGhostShadow ghost) {
        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingTimer().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.isNewTileEntered() && terrain.isIntersection(ghost.tile());
            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, level);
                ghost.setSpeed(speed);
                ghost.tryMovingOrTeleporting(level);
            } else {
                // Normal behavior of red ghost
                final boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING || ghost.elroyState().enabled();
                final Vector2i targetTile = chase ? ghost.chasingTargetTile(level) : terrain.ghostScatterTile(ghost.personality());
                ghost.setSpeed(speed);
                ghost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return ghost;
    }

    private static Ghost applyModifiedAmbushBehavior(Ghost ghost) {
        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingTimer().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.isNewTileEntered() && terrain.isIntersection(ghost.tile());
            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, level);
                ghost.setSpeed(speed);
                ghost.tryMovingOrTeleporting(level);
            } else {
                final boolean chase = level.huntingTimer().phase() == HuntingPhase.CHASING;
                final Vector2i targetTile = chase ? ghost.chasingTargetTile(level) : terrain.ghostScatterTile(ghost.personality());
                ghost.setSpeed(speed);
                ghost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return ghost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameLevel level) {
        final Vector2i tile = ghost.tile();
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (teleporting) {
            return;
        }
        int dirsTried = 0;
        Direction dir = Direction.random();
        while (++dirsTried <= 4) {
            if (isAcceptableWishDir(level, ghost, dir)) {
                ghost.setWishDir(dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
            dir = dir.nextClockwise();
        }
    }

    private static boolean isAcceptableWishDir(GameLevel level, Ghost ghost, Direction dir) {
        final Vector2i neighborTile = ghost.tile().plus(dir.vector());
        return dir != ghost.moveDir().opposite() && ghost.canAccessTile(level, neighborTile);
    }
}
