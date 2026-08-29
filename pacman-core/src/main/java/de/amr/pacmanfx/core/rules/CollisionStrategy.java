/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.ecs.systems.PositionSystem.boundingBox;
import static java.util.Objects.requireNonNull;

public enum CollisionStrategy {

    SAME_TILE {
        @Override
        public boolean collide(GameEntity either, GameEntity other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            return either.pos().tile().equals(other.pos().tile());
        }

        @Override
        public String toString() {
            return "Same Tile";
        }
    },

    BOX_INTERSECTION {
        @Override
        public boolean collide(GameEntity either, GameEntity other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            return boundingBox(either.pos().asVector2f()).intersects(boundingBox(other.pos().asVector2f()));
        }

        @Override
        public String toString() {
            return "Box Intersection";
        }
    },

    CENTER_DISTANCE {
        private static final float COLLISION_SENSITIVITY_PIXELS = 2;
        @Override
        public boolean collide(GameEntity either, GameEntity other) {
            requireNonNull(either, "Actor to check for collision must not be null");
            requireNonNull(other, "Actor to check for collision must not be null");
            final Vector2f eitherCenter = either.pos().bodyCenter();
            final Vector2f otherCenter = other.pos().bodyCenter();
            float dist = eitherCenter.euclideanDist(otherCenter);
            if (dist < COLLISION_SENSITIVITY_PIXELS) {
                Logger.debug("Collision detected (dist={}): {} collides with {}", dist, either, other);
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Center Distance under threshold";
        }
    };

    /**
     * @param either some actor
     * @param other some actor
     * @return <code>true</code> if both actors are colliding according to this strategy
     */
    public abstract boolean collide(GameEntity either, GameEntity other);
}
