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

public class BonusStateSystem {

    public BonusStateSystem() {}

    public void update(GameLevel level, Bonus bonus, GameEventManager eventManager, MovementSystem motor) {
        requireNonNull(level);
        requireNonNull(bonus);
        requireNonNull(eventManager);
        requireNonNull(motor);

        final BonusStateComp state = bonus.state();

        state.timer().doTick();

        switch (state.bonusState()) {
            case INACTIVE -> {}

            case EDIBLE -> {
                boolean edibleStateOver = state.timer().hasExpired();
                final BonusMoveAndJumpComp moveAndJump = bonus.optMoveAndJump().orElse(null);
                if (moveAndJump != null) {
                    edibleStateOver = edibleStateOver || moveAndJump.targetReached();
                }
                if (edibleStateOver) {
                    state.setEdibleStateExpired(edibleStateOver);
                    setBonusInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                boolean eatenStateOver = state.timer().hasExpired();
                if (eatenStateOver) {
                    setBonusInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }
        }
    }

    public void setBonusInactive(Bonus bonus) {
        bonus.hide();

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.INACTIVE);
        state.timer().restartIndefinitely();
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
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
