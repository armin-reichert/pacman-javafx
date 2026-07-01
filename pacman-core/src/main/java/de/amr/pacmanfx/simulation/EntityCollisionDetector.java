/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.FoodLayer;

import java.util.List;

public final class EntityCollisionDetector {

    private EntityCollisionDetector() {}

    public static void detectCollisions(GameContext context) {
        final HuntingStepResult result = context.huntingResult();
        final GameModel gameModel = context.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        final CollisionStrategy strategy = context.rules().getCollisionStrategy();

        detectFoodCollision(result, level, pac);
        detectEdibleBonusCollision(result, strategy, pac, bonus);
        detectPacGhostCollision(result, strategy, pac, ghosts);
    }

    private static void detectPacGhostCollision(HuntingStepResult result, CollisionStrategy strategy, Pac pac, List<Ghost> ghosts) {
        result.ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(result.ghostsCollidingWithPac()::add);
    }

    private static void detectEdibleBonusCollision(HuntingStepResult result, CollisionStrategy strategy, Pac pac, Bonus bonus) {
        result.setEdibleBonus(null);
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            result.setEdibleBonus(bonus);
        }
    }

    private static void detectFoodCollision(HuntingStepResult result, GameLevel level, Pac pac) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            result.setFoodFoundTile(pacTile);
            result.setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}