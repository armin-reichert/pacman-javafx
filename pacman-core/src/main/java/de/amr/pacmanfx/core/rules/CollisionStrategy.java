/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.rules;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public enum CollisionStrategy {

    SAME_TILE {
        @Override
        public boolean collide(Actor either, Actor other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            final Vector2i eitherTile = GameContext.SYSTEMS.worldMovementSystem.computeTile(either);
            final Vector2i otherTile = GameContext.SYSTEMS.worldMovementSystem.computeTile(other);
            return eitherTile.equals(otherTile);
        }
    },

    CENTER_DISTANCE {
        private static final float COLLISION_SENSITIVITY_PIXELS = 2;
        @Override
        public boolean collide(Actor either, Actor other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            final Vector2f eitherCenter = GameContext.SYSTEMS.worldMovementSystem.computeCenter(either);
            final Vector2f otherCenter = GameContext.SYSTEMS.worldMovementSystem.computeCenter(other);
            float dist = eitherCenter.euclideanDist(otherCenter);
            if (dist < COLLISION_SENSITIVITY_PIXELS) {
                Logger.info("Collision detected (dist={}): {} collides with {}", dist, either, other);
                return true;
            }
            return false;
        }
    };

    /**
     * @param either some actor
     * @param other some actor
     * @return <code>true</code> if both actors are colliding according to this strategy
     */
    public abstract boolean collide(Actor either, Actor other);
}
