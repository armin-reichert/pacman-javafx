/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class EntityCollisionDetector {

    private final GameContext context;

    public EntityCollisionDetector(GameContext context) {
        this.context = requireNonNull(context);
        context.setHuntingStepResult(new HuntingStepResult());
    }

    public void detectCollisions(GameLevel level) {
        detectFoodCollision(level);
        detectEdibleBonusCollision(level);
        detectPacGhostCollision(level);
    }

    private void detectPacGhostCollision(GameLevel level) {
        final CollisionStrategy strategy = context.model().rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        context.huntingStepResult().ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(context.huntingStepResult().ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameLevel level) {
        final CollisionStrategy strategy = context.model().rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        context.huntingStepResult().setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            context.huntingStepResult().setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameLevel level) {
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            context.huntingStepResult().setFoodFoundTile(pacTile);
            context.huntingStepResult().setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}