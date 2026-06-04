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
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public final class Hunting {

    private Hunting() {}

    public static List<String> createReport(HuntingStepResult result) {
        var messages = new ArrayList<String>();
        for (Ghost ghost : result.ghostsCollidingWithPac()) {
            messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.computeTile(), ghost.state()));
        }
        if (result.energizerFound()) {
            messages.add("Energizer found at " + result.foodFoundTile());
        }
        if (result.bonusIndex() != -1) {
            messages.add("Bonus score reached, index=" + result.bonusIndex());
        }
        if (result.edibleBonus() != null) {
            messages.add("Bonus eaten: %s".formatted(result.edibleBonus()));
        }
        if (result.pacGotPower()) {
            messages.add("Pac gained power");
        }
        if (result.pacStartsLosingPower()) {
            messages.add("Pac starts losing power");
        }
        if (result.pacLostPower()) {
            messages.add("Pac lost power");
        }
        for (Ghost ghost : result.ghostsKilled()) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
        }
        return messages;
    }

    public static void detectCollisions(GameContext context) {
        final HuntingStepResult result = context.huntingResult();
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        final CollisionStrategy collisionStrategy = game.collisionStrategy();

        detectFood(result, level, pac);
        detectEdibleBonus(result, collisionStrategy, pac, bonus);
        detectPacGhostCollisions(result, collisionStrategy, pac, ghosts);
    }

    private static void detectPacGhostCollisions(HuntingStepResult result, CollisionStrategy strategy, Pac pac, List<Ghost> ghosts) {
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(result.ghostsCollidingWithPac()::add);
    }

    private static void detectEdibleBonus(HuntingStepResult result, CollisionStrategy strategy, Pac pac, Bonus bonus) {
        if (bonus != null && bonus.state() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            result.setEdibleBonus(bonus);
        }
    }

    private static void detectFood(HuntingStepResult result, GameLevel level, Pac pac) {
        final FoodLayer foodLayer = level.worldMap().foodLayer();
        final Vector2i pacTile = pac.computeTile();
        if (foodLayer.hasFoodAtTile(pacTile)) {
            result.setFoodFoundTile(pacTile);
            result.setEnergizerFound(foodLayer.isEnergizerTile(pacTile));
        }
    }
}