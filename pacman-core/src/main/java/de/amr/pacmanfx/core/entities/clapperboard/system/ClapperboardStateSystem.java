/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.clapperboard.system;

import de.amr.pacmanfx.core.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.entities.clapperboard.ClapperboardState;
import de.amr.pacmanfx.core.entities.clapperboard.comp.ClapperboardStateComp;

public class ClapperboardStateSystem {

    public static void startFlapAnimation(Clapperboard clapperboard) {
        clapperboard.show();
        clapperboard.state().setTick(0);
        clapperboard.state().setTextVisible(true);
        clapperboard.state().setState(ClapperboardState.WIDE_OPEN);
        clapperboard.state().setRunning(true);
    }

    public static void update(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();

        if (!state.running()) return;

        //TODO Verify exact tick values
        switch (state.tick()) {
            case 48 -> state.setState(ClapperboardState.OPEN);
            case 54 -> state.setState(ClapperboardState.CLOSED);
            case 59 -> state.setState(ClapperboardState.WIDE_OPEN);
            case 88 -> {
                clapperboard.hide();
                state.setRunning(false);
            }
        }
        state.setTick(state.tick() + 1);
    }
}
