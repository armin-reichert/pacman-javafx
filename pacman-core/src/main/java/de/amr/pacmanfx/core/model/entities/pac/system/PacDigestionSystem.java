/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacDigestionComp;

public class PacDigestionSystem {

    public void update(GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        final long restingTicks = digestion.restingTicks();
        if (restingTicks > 0) {
            digestion.setRestingTicks(restingTicks - 1);
        }
    }

    public boolean mustRest(GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        return digestion.restingTicks() > 0;
    }

    public void starve(GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        digestion.setStarvingTicks(digestion.starvingTicks() + 1);
    }

    public void endStarving(GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        digestion.setStarvingTicks(0);
    }

    public void onPacEatsEnergizer(GameContext gameContext, GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        digestion.setRestingTicks(gameContext.model().rules().restingTicksForEnergizer());
    }

    public void onPacEatsPellet(GameContext gameContext, GameEntity pac) {
        final PacDigestionComp digestion = pac.requireComponent(PacDigestionComp.class);
        digestion.setRestingTicks(gameContext.model().rules().restingTicksForPellet());
    }
}
