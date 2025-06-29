/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.actors.GhostState;

public interface GhostAppearanceSelector {

    static GhostAppearance selectAppearance(
            GhostState ghostState,
            boolean powerActive,
            boolean powerFading,
            boolean killedDuringCurrentPhase)
    {
        return switch (ghostState) {
            case LEAVING_HOUSE, LOCKED -> powerActive && !killedDuringCurrentPhase
                    ? frightenedOrFlashing(powerFading)
                    : GhostAppearance.NORMAL;
            case FRIGHTENED -> frightenedOrFlashing(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EATEN;
            case EATEN -> GhostAppearance.VALUE;
            default -> GhostAppearance.NORMAL;
        };
    }

    private static GhostAppearance frightenedOrFlashing(boolean powerFading) {
        return powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }
}
