/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.GhostState;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class Ghost3DAppearanceController {

    private final Ghost3D ghost3D;
    private int numFlashes;

    public Ghost3DAppearanceController(Ghost3D ghost3D) {
        this.ghost3D = requireNonNull(ghost3D);
        numFlashes = 3;
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = numFlashes;
    }

    public void updateAppearance(GameLevel level) {
        final GhostState ghostState = ghost3D.ghost().state();

        // Let ghost shown as number alone
        if (ghostState == GhostState.EATEN) return;

        final boolean powerActive = level.pac().powerTimer().isRunning();
        final boolean powerFading = level.pac().isPowerFading(level);
        // ghosts that already got killed in the current power phase do not look frightened anymore
        final boolean killedAlready = level.energizerVictims().contains(ghost3D.ghost());

        setGhostAppearance(switch (ghostState) {
            case LOCKED, LEAVING_HOUSE -> powerActive && !killedAlready
                ? ghost3D.frightenedAppearance(powerFading)
                : GhostAppearance.NORMAL;
            case FRIGHTENED -> ghost3D.frightenedAppearance(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            default -> GhostAppearance.NORMAL;
        });
    }

    public void setGhostAppearance(GhostAppearance ghostAppearance) {
        switch (ghostAppearance) {
            case NORMAL -> ghost3D.setNormalLook();
            case FRIGHTENED -> ghost3D.setFrightenedLook();
            case EYES -> ghost3D.setEyesOnlyLook();
            case FLASHING -> ghost3D.setFlashingLook(numFlashes);
        }
        ghost3D.animateDress(true);

        Logger.debug("Ghost appearance for {} is now {}", ghost3D.ghost().name(), ghostAppearance);
    }
}
