/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.FoodLayer;

import java.util.List;

public final class EntityCollisionDetector {

    public void detectCollisions(GameContext context) {
        final GameLevel level = context.level();
        detectFoodCollision(context);
        detectEdibleBonusCollision(context);
        detectPacGhostCollision(context);
    }

    private void detectPacGhostCollision(GameContext context) {
        final GameLevel level = context.level();
        final GameModel model = context.model();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        context.thisFrame().huntingStepResult().ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(context.thisFrame().huntingStepResult().ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameContext context) {
        final GameLevel level = context.level();
        final GameModel model = context.model();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        context.thisFrame().huntingStepResult().setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            context.thisFrame().huntingStepResult().setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameContext context) {
        final GameLevel level = context.level();
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            context.thisFrame().huntingStepResult().setFoodFoundTile(pacTile);
            context.thisFrame().huntingStepResult().setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}