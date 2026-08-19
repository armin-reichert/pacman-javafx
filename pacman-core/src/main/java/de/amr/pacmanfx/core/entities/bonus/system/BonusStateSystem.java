/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
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
                boolean timedOut = state.timer().hasExpired();
                boolean targetReached = bonus.optMoveAndJump().map(BonusMoveAndJumpComp::targetReached).orElse(true);
                if (timedOut || targetReached) {
                    state.setEdibleStateExpired(timedOut);
                    setBonusInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                final boolean timedOut = state.timer().hasExpired();
                if (timedOut) {
                    setBonusInactive(bonus);
                    eventManager.publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }
        }
    }

    public void setBonusInactive(Bonus bonus) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();

        state.setBonusState(BonusState.INACTIVE);
        state.timer().restartIndefinitely();

        bonus.hide();
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartSeconds(seconds);

        bonus.show();
    }

    public void showEdible(Bonus bonus) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        state.timer().restartIndefinitely();

        bonus.show();
    }

    public void showEatenForSeconds(Bonus bonus, float seconds) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EATEN);
        state.timer().restartSeconds(seconds);

        bonus.show();
    }
}
