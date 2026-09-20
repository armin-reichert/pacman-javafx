/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.clapperboard;

import de.amr.pacmanfx.core.entities.props.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.entities.props.clapperboard.ClapperboardState;
import de.amr.pacmanfx.core.entities.props.clapperboard.ClapperboardStateComp;

import static java.util.Objects.requireNonNull;

public class ClapperboardStateSystem {

    public static void init(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        final ClapperboardStateComp state = clapperboard.state();

        state.setEnumValue(ClapperboardState.CLOSED);
        state.setTick(0);
        state.setTextVisible(true);
        state.setRunning(true);
    }

    public static void update(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        final ClapperboardStateComp state = clapperboard.state();

        if (!state.running()) return;

        //TODO Verify exact tick values
        switch (state.tick()) {
            case 3 -> state.setEnumValue(ClapperboardState.OPEN);
            case 5 -> state.setEnumValue(ClapperboardState.WIDE_OPEN);
            case 65 -> {
                state.setEnumValue(ClapperboardState.CLOSED);
                state.setTextVisible(false);
            }
            case 69 -> state.setEnumValue(ClapperboardState.OPEN);
            case 71 -> state.setEnumValue(ClapperboardState.WIDE_OPEN);
            case 129 -> {
                clapperboard.hide();
                state.setRunning(false);
            }
        }
        state.setTick(state.tick() + 1);
    }
}
