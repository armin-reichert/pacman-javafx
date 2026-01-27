/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public enum CollisionStrategy {

    SAME_TILE {
        @Override
        public boolean collide(Actor either, Actor other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            return either.tile().equals(other.tile());
        }
    },

    CENTER_DISTANCE {
        private static final float COLLISION_SENSITIVITY_PIXELS = 2;
        @Override
        public boolean collide(Actor either, Actor other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            float dist = either.center().euclideanDist(other.center());
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
