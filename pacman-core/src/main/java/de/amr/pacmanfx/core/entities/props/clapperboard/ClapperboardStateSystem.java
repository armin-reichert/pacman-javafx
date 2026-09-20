/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.props.clapperboard;

public class ClapperboardStateSystem {

    public void startFlapAnimation(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();
        state.setTick(0);
        state.setTextVisible(true);
        state.setEnumValue(ClapperboardState.WIDE_OPEN);
        state.setRunning(true);
        clapperboard.show();
    }

    public void update(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();

        if (!state.running()) return;

        //TODO Verify exact tick values
        switch (state.tick()) {
            case 48 -> state.setEnumValue(ClapperboardState.OPEN);
            case 54 -> state.setEnumValue(ClapperboardState.CLOSED);
            case 59 -> state.setEnumValue(ClapperboardState.WIDE_OPEN);
            case 88 -> {
                clapperboard.hide();
                state.setRunning(false);
            }
        }
        state.setTick(state.tick() + 1);
    }
}
