/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay;


import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.bonus.BonusState;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.gamestate.FrameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.CollisionStrategy;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ActorCollisionHandler {

    private final FrameState frameState;
    private boolean doubleChecked;
    private CollisionStrategy strategy = CollisionStrategy.SAME_TILE;
    
    public ActorCollisionHandler(FrameState frameState) {
        this.frameState = requireNonNull(frameState);
    }

    public FrameState step() {
        return frameState;
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
        final Pac pac = level.entitySet().pac();
        final List<Ghost> ghosts = level.entitySet().ghosts().toList();
        frameState.ghostsCollidingWithPac().clear();
        ghosts.stream()
            .filter(ghost -> strategy.collide(pac, ghost))
            .forEach(frameState.ghostsCollidingWithPac()::add);
    }

    public void detectEdibleBonusCollision(GameLevel level) {
        final Pac pac = level.entitySet().pac();
        final Bonus bonus = level.entitySet().entities().anyOfTypeOrNull(Bonus.class);
        frameState.setEdibleBonus(null);
        if (bonus != null && bonus.state().enumValue() == BonusState.EDIBLE && strategy.collide(pac, bonus)) {
            frameState.setEdibleBonus(bonus);
        }
    }

    public void detectFoodCollision(GameLevel level) {
        final Pac pac = level.entitySet().pac();
        final Vector2i pacTile = pac.pos().tile();
        if (level.food().hasFoodAtTile(pacTile)) {
            frameState.setFoodFoundTile(pacTile);
            frameState.setEnergizerFound(level.worldMap().foodLayer().isEnergizerTile(pacTile));
        }
    }
}
