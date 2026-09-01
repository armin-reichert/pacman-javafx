/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.event.bonus.BonusExpiredEvent;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    public BonusStateSystem() {}

    public void update(GameContext game, Bonus bonus) {
        requireNonNull(game);
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.timer().doTick();

        switch (state.enumValue()) {
            case INACTIVE -> {}

            case EDIBLE -> {
                boolean expired = state.timer().hasExpired();
                boolean tourComplete = bonus.optMoveAndJump()
                    .map(BonusMoveAndJumpComp::targetReached)
                    .orElse(false);

                if (expired || tourComplete) {
                    state.setEdibleStateExpired(expired);
                    setInactive(bonus);
                    game.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }

            case EATEN -> {
                final boolean expired = state.timer().hasExpired();
                if (expired) {
                    setInactive(bonus);
                    game.eventManager().publishGameEvent(new BonusExpiredEvent(bonus));
                }
            }
        }
    }

    public void setInactive(Bonus bonus) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setEnumValue(BonusState.INACTIVE);
        state.timer().restartIndefinitely();

        bonus.hide();
    }

    public void setEdible(Bonus bonus) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setEnumValue(BonusState.EDIBLE);
        bonus.show();
    }
}
