/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

public class ActorCollisionRules {

    private boolean collisionDoubleChecked = true;

    private CollisionStrategy collisionStrategy = CollisionStrategy.SAME_TILE;

    public boolean isCollisionDoubleChecked() {
        return collisionDoubleChecked;
    }

    public void setCollisionDoubleChecked(boolean collisionDoubleChecked) {
        this.collisionDoubleChecked = collisionDoubleChecked;
    }

    public CollisionStrategy getCollisionStrategy() {
        return collisionStrategy;
    }

    public void setCollisionStrategy(CollisionStrategy collisionStrategy) {
        this.collisionStrategy = collisionStrategy;
    }
}
