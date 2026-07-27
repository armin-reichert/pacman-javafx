/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Pac;

public class PacDigestionSystem {

    public void update(Pac pac) {
        final long restingTicks = pac.digestion().restingTicks();
        if (restingTicks > 0) {
            pac.digestion().setRestingTicks(restingTicks - 1);
        }
    }

    public boolean isResting(Pac pac) {
        return pac.digestion().restingTicks() > 0;
    }

    public void starve(Pac pac) {
        pac.digestion().setStarvingTicks(pac.digestion().starvingTicks() + 1);
    }

    public void endStarving(Pac pac) {
        pac.digestion().setStarvingTicks(0);
    }

    public void onPacEatsEnergizer(GameContext gameContext, Pac pac) {
        pac.digestion().setRestingTicks(gameContext.model().rules().restingTicksForEnergizer());
    }

    public void onPacEatsPellet(GameContext gameContext, Pac pac) {
        pac.digestion().setRestingTicks(gameContext.model().rules().restingTicksForPellet());
    }
}
