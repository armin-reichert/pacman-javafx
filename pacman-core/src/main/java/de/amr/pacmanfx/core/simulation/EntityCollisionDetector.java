/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.FoodLayer;

import java.util.List;

public final class EntityCollisionDetector {

    private final HuntingStepResult huntingStepResult = new HuntingStepResult();

    public EntityCollisionDetector() {}

    public HuntingStepResult detectCollisions(GameLevel level) {
        detectFoodCollision(level);
        detectEdibleBonusCollision(level);
        detectPacGhostCollision(level);
        return huntingStepResult;
    }

    private void detectPacGhostCollision(GameLevel level) {
        final GameModel model = level.gameModel();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        huntingStepResult.ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(huntingStepResult.ghostsCollidingWithPac()::add);
    }

    private void detectEdibleBonusCollision(GameLevel level) {
        final GameModel model = level.gameModel();
        final CollisionStrategy strategy = model.rules().getCollisionStrategy();
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        huntingStepResult.setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            huntingStepResult.setEdibleBonus(bonus);
        }
    }

    private void detectFoodCollision(GameLevel level) {
        final Pac pac = level.entities().pac();
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            huntingStepResult.setFoodFoundTile(pacTile);
            huntingStepResult.setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}