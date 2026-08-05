/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.entities.pac.PacState;
import de.amr.pacmanfx.core.model.entities.pac.comp.PacStateComp;

import static java.util.Objects.requireNonNull;

public class PacStateSystem {

    public void setState(Pac pac, PacState pacState) {
        requireNonNull(pac);
        requireNonNull(pacState);
        pac.state().setState(pacState);
    }

    public void update(Pac pac) {
        final PacStateComp state = pac.state();
        switch (state.pacState()) {
            case ACTIVE -> state.setMoving(!isStandingStill(pac));
            case DEAD -> state.setMoving(false);
        }
    }

    public boolean isStandingStill(Pac pac) {
        return pac.movement().hasZeroVelocity() ||didNotMoveThroughWorld(pac);
    }

    private boolean didNotMoveThroughWorld(Pac pac) {
        final WorldNavigationComp worldNavigation = pac.worldNavigation();
        return !worldNavigation.info.moved;
    }
}
