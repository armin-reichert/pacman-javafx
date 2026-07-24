/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.rules;

import de.amr.pacmanfx.core.model.actors.CollisionStrategy;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ActorCollisionRules {

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private final ObjectProperty<CollisionStrategy> collisionStrategy = new SimpleObjectProperty<>(CollisionStrategy.SAME_TILE);

    public BooleanProperty collisionDoubleCheckedProperty() {
        return collisionDoubleChecked;
    }

    public boolean isCollisionDoubleChecked() {
        return collisionDoubleChecked.get();
    }

    public ObjectProperty<CollisionStrategy> collisionStrategyProperty() {
        return collisionStrategy;
    }

    public CollisionStrategy getCollisionStrategy() {
        return collisionStrategy.get();
    }
}
