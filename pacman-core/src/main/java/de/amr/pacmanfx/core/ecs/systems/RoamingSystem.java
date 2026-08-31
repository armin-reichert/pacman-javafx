/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RandomNumbers;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.*;
import static java.util.Objects.requireNonNull;

public class RoamingSystem {

    private final WorldNavigationSystem navigator;

    public RoamingSystem(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    /**
     * Lets the actor roam through the current level's world.<br>
         <cite>
             Roam if you want to, roam around the world!<br>
             Roam if you want to, without wings without wheels!<br>
             Roam if you want to, roam around the world!<br>
             Roam if you want to, without anything but the love we feel!
         </cite>
     */
    public <E extends GameEntity> void roam(
        GameLevel level,
        E actor,
        WorldNavigationComp navigation,
        WorldMovementPolicy<E> worldMovementPolicy,
        float speed)
    {
        final Vector2i tile = actor.pos().tile();
        final boolean inPortalSpace = level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        // Compute a random direction whenever a new tile has been entered or in a dead-end.
        // While teleporting, keep direction.
        final boolean stuck = !navigation.info().moved;
        if ((navigation.isNewTileEntered() || stuck) && !inPortalSpace) {
            final Direction dir = computeNextDirection(level, actor, worldMovementPolicy, tile);
            navigator.setWishDir(actor, dir);
            Logger.debug("Ghost {} takes random wish direction {}", actor.name(), dir);
        }

        navigator.setMoveDirSpeed(actor, speed);
        navigator.tryMovingOrTeleporting(level, actor, worldMovementPolicy);
    }

    // Try a random accessible direction, do not turn back unless there is no other way
    private <E extends GameEntity> Direction computeNextDirection(
        GameLevel level,
        E actor,
        WorldMovementPolicy<E> policy,
        Vector2i currentTile)
    {
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        final Direction oppositeDir = navigation.moveDir().opposite();

        Direction nextDir = choosePseudoRandomDirection();
        for (int i = 0; i < 4; ++i) {
            final Vector2i nextTile = currentTile.plus(nextDir.vector());
            final boolean accessible = policy.canAccessTile(level, actor, nextTile);
            if (accessible && nextDir != oppositeDir) {
                return nextDir;
            }
            nextDir = nextDir.nextClockwise();
        }
        return oppositeDir;
    }

    // Saw this on YouTube :-)
    private Direction choosePseudoRandomDirection() {
        final int rnd = RandomNumbers.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }
}
