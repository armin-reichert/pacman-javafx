/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;


import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.rules.CollisionStrategy;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ActorCollisionHandler {

    private final GamePlayStep step;
    private boolean doubleChecked;
    private CollisionStrategy strategy = CollisionStrategy.SAME_TILE;
    
    public ActorCollisionHandler(GamePlayStep step) {
        this.step = requireNonNull(step);
    }

    public GamePlayStep step() {
        return step;
    }

    public void setStrategy(CollisionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setDoubleChecked(boolean doubleChecked) {
        this.doubleChecked = doubleChecked;
    }

    public void detectCollisions(GameLevel level) {
        detectFoodCollision(level);
        detectEdibleBonusCollision(level);
        detectPacGhostCollision(level);
    }

    public void detectPacGhostCollision(GameLevel level) {
        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        step.ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(step.ghostsCollidingWithPac()::add);
    }

    public void detectEdibleBonusCollision(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Bonus bonus = level.entities().optBonus().orElse(null);
        step.setEdibleBonus(null);
        if (bonus != null && bonus.bonusState() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            step.setEdibleBonus(bonus);
        }
    }

    public void detectFoodCollision(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Vector2i pacTile = pac.pos().tile();
        if (level.food().hasFoodAtTile(pacTile)) {
            step.setFoodFoundTile(pacTile);
            step.setEnergizerFound(level.worldMap().foodLayer().isEnergizerTile(pacTile));
        }
    }
}
