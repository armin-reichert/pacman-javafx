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
    public <E extends GameEntity> void roam(GameLevel level, E gameEntity,
        WorldNavigationComp navigation,
        WorldMovementPolicy<E> worldMovementPolicy,
        MovementSystem motor, float speed)
    {
        final Vector2i tile = gameEntity.pos().tile();
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        final boolean stuck = !navigation.info().moved;
        if ((navigation.isNewTileEntered() || stuck) && !teleporting) {
            final Direction dir = computeRoamingDirection(level, gameEntity, worldMovementPolicy, tile);
            navigator.setWishDir(gameEntity, dir);
            Logger.debug("Ghost {} takes random wish direction {}", gameEntity.name(), dir);
        }
        navigator.setMoveDirSpeed(gameEntity, speed);
        navigator.tryMovingOrTeleporting(level, gameEntity, motor, worldMovementPolicy);
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private <E extends GameEntity> Direction computeRoamingDirection(
        GameLevel level, E gameEntity, WorldMovementPolicy<E> policy, Vector2i currentTile) {

        final WorldNavigationComp navigation = gameEntity.reqComp(WorldNavigationComp.class);

        final Direction oppositeDir = navigation.moveDir().opposite();
        Direction selectedDir = choosePseudoRandomDirection();
        int tries = 0;
        while (selectedDir == oppositeDir
            || !policy.canAccessTile(level, gameEntity, currentTile.plus(selectedDir.vector())))
        {
            selectedDir = selectedDir.nextClockwise();
            if (++tries > 4) {
                return oppositeDir;  // avoid endless loop
            }
        }
        return selectedDir;
    }

    private Direction choosePseudoRandomDirection() {
        final int rnd = RandomNumbers.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }
}
