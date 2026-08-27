/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacDigestionComp;
import de.amr.pacmanfx.core.rules.GameRules;

public class PacDigestionSystem {

    public void update(Pac pac) {
        final PacDigestionComp digestion = pac.digestion();
        final long restingTicks = digestion.restingTicks();
        if (restingTicks > 0) {
            digestion.setRestingTicks(restingTicks - 1);
        }
    }

    public void starve(Pac pac) {
        final PacDigestionComp digestion = pac.digestion();
        digestion.setStarvingTicks(digestion.starvingTicks() + 1);
    }

    public void endStarving(Pac pac) {
        pac.digestion().setStarvingTicks(0);
    }

    public void digestEnergizer(Pac pac, GameRules rules) {
        pac.digestion().setRestingTicks(rules.restingTicksForEnergizer());
    }

    public void digestPellet(Pac pac, GameRules rules) {
        pac.digestion().setRestingTicks(rules.restingTicksForPellet());
    }
}
