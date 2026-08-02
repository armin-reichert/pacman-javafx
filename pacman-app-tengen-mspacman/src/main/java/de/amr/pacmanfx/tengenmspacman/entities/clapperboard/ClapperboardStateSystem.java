/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.clapperboard;


public class ClapperboardStateSystem {

    public static void startFlapAnimation(Clapperboard clapperboard) {
        clapperboard.state().setTick(0);
        clapperboard.state().setTextVisible(true);
        clapperboard.state().setState(ClapperboardState.CLOSED);
        clapperboard.state().setRunning(true);
    }

    public static void update(Clapperboard clapperboard) {
        final ClapperboardStateComp state = clapperboard.state();

        if (!state.running()) return;

        //TODO Verify exact tick values
        switch (state.tick()) {
            case 3 -> state.setState(ClapperboardState.OPEN);
            case 5 -> state.setState(ClapperboardState.WIDE_OPEN);
            case 65 -> {
                state.setState(ClapperboardState.CLOSED);
                state.setTextVisible(false);
            }
            case 69 -> state.setState(ClapperboardState.OPEN);
            case 71 -> state.setState(ClapperboardState.WIDE_OPEN);
            case 129 -> {
                state.setState(ClapperboardState.HIDDEN);
                state.setRunning(false);
            }
            default -> {
            }
        }
        state.setTick(state.tick() + 1);
    }
}
