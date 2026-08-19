/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;

import static java.util.Objects.requireNonNull;

public class BonusStateSystem {

    public BonusStateSystem() {}

    public void setBonusInactive(Bonus bonus) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();

        state.setBonusState(BonusState.INACTIVE);
        state.timer().restartIndefinitely();

        bonus.hide();
    }

    public void showEdibleForSeconds(Bonus bonus, float seconds) {
        showEdibleStateAndStartTimer(bonus, seconds);
    }

    public void showEdible(Bonus bonus) {
        showEdibleStateAndStartTimer(bonus, null);
    }

    private void showEdibleStateAndStartTimer(Bonus bonus, Float optionalSec) {
        requireNonNull(bonus);

        final BonusStateComp state = bonus.state();
        state.setBonusState(BonusState.EDIBLE);
        if (optionalSec == null) {
            state.timer().restartIndefinitely();
        } else {
            state.timer().restartSeconds(optionalSec);
        }

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
