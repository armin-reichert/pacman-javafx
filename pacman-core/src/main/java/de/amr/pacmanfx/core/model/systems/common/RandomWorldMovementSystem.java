package de.amr.pacmanfx.core.model.systems.common;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.*;
import static de.amr.basics.math.Direction.LEFT;
import static java.util.Objects.requireNonNull;

public class RandomWorldMovementSystem {

    /**
     * Lets the actor roam through the current level's world.<br>
         <cite>
             Roam if you want to, roam around the world!<br>
             Roam if you want to, without wings without wheels!<br>
             Roam if you want to, roam around the world!<br>
             Roam if you want to, without anything but the love we feel!
         </cite>
     */
    public void roam(GameContext gameContext, Actor actor, float speed) {
        requireNonNull(gameContext);
        requireNonNull(actor);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        final WorldMovementSystem navigator = gameContext.systems().navigator;
        final GameLevel level = gameContext.assertLevel();

        final Vector2i tile = WorldMovementSystem.computeTile(actor);
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(tile);

        final boolean stuck = !worldMovement.info.moved;
        if ((worldMovement.isNewTileEntered() || stuck) && !teleporting) {
            final Direction dir = computeRoamingDirection(gameContext, actor, tile);
            navigator.setWishDir(actor, dir);
            Logger.debug("Ghost {} takes random wish direction {}", actor.name(), dir);
        }
        navigator.setSpeed(actor, speed);
        navigator.tryMovingOrTeleporting(actor, gameContext);
    }

    // try a random direction towards an accessible tile, do not turn back unless there is no other way
    private Direction computeRoamingDirection(GameContext gameContext, Actor actor, Vector2i currentTile) {
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        final WorldMovementPolicy policy = actor.assertComponent(WorldMovementPolicy.class);

        final Direction oppositeDir = worldMovement.moveDir().opposite();
        Direction selectedDir = choosePseudoRandomDirection();
        int tries = 0;
        while (selectedDir == oppositeDir
            || !policy.canAccessTile(gameContext, actor, currentTile.plus(selectedDir.vector())))
        {
            selectedDir = selectedDir.nextClockwise();
            if (++tries > 4) {
                return oppositeDir;  // avoid endless loop
            }
        }
        return selectedDir;
    }

    private Direction choosePseudoRandomDirection() {
        final int rnd = RandomNumberSupport.randomInt(0, 1000);
        if (rnd < 163)             return UP;
        if (rnd < 163 + 252)       return RIGHT;
        if (rnd < 163 + 252 + 285) return DOWN;
        return LEFT;
    }
}
