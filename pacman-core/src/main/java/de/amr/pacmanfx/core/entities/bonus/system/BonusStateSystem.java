/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

//TODO implement navigation logic and movement changes elsewhere
public class BonusStateSystem {

    public BonusStateSystem() {}

    public void update(
        GameLevel level,
        Bonus bonus,
        GameEventManager eventManager,
        MovementSystem motor,
        BonusMoveAndJumpSystem moveAndJumpSystem, //TODO remove
        WorldNavigationSystem worldNavigationSystem
    ) {
        requireNonNull(level);
        requireNonNull(bonus);
        requireNonNull(eventManager);
        requireNonNull(motor);

        final BonusStateComp state = bonus.state();
        final BonusMoveAndJumpComp moveAndJumpComp = bonus.optMoveAndJump().orElse(null);

        state.timer().doTick();

        switch (state.bonusState()) {

            case EDIBLE -> {
                if (moveAndJumpComp != null) {
                    moveAndJumpSystem.update(motor, level, bonus);
                    state.setEdibleStateExpired(moveAndJumpComp.targetReached() || state.timer().hasExpired());
                }
                else {
                    // Fixed position bonus expires using timer. Animated bonus expires when entering portal.
                    state.setEdibleStateExpired(state.timer().hasExpired());
                }
                if (state.edibleStateExpired()) {
                    setInactive(bonus, moveAndJumpSystem, worldNavigationSystem);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                if (state.timer().hasExpired()) {
                    setInactive(bonus, moveAndJumpSystem, worldNavigationSystem);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case INACTIVE -> {}
        }
    }

    public void setInactive(
        Bonus bonus,
        BonusMoveAndJumpSystem moveAndJumpSystem, //TODO remove
        WorldNavigationSystem navigationSystem //TODO remove
    ) {
        bonus.hide();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.INACTIVE);
        state.timer().restartIndefinitely();

        if (bonus.optMovement().isPresent()) {
            navigationSystem.setSpeed(bonus, 0);
        }

        bonus.optMoveAndJump().ifPresent(moveAndJumpSystem::reset);
    }

    public void showEdibleForSeconds(
        Bonus bonus,
        float seconds,
        BonusMoveAndJumpSystem moveAndJumpSystem, //TODO remove
        WorldNavigationSystem navigationSystem //TODO remove
    ) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartSeconds(seconds);
    }

    public void showEdibleAndStartWandering(
        Bonus bonus,
        float speed,
        WorldNavigationSystem navigationSystem, //TODO remove
        BonusMoveAndJumpSystem moveAndJumpSystem //TODO remove
    ) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartIndefinitely();

        if (bonus.optMoveAndJump().isPresent()) {
            navigationSystem.setSpeed(bonus, speed);
        }
        navigationSystem.clearTargetTile(bonus);
        bonus.optMoveAndJump().ifPresent(moveAndJump -> {
            moveAndJumpSystem.start(moveAndJump);
            navigationSystem.setSpeed(bonus, speed);
        });
    }

    public void showEatenForSeconds(
        Bonus bonus,
        float seconds,
        WorldNavigationSystem navigationSystem //TODO remove
    ) {
        requireNonNull(bonus);

        bonus.show();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EATEN);
        state.timer().restartSeconds(seconds);

        if (bonus.optMovement().isPresent()) {
            navigationSystem.setSpeed(bonus, 0);
        }
    }
}
