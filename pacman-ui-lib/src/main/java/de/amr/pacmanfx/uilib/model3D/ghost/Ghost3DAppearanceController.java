/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import org.tinylog.Logger;

public class Ghost3DAppearanceController {

    private int numFlashes;

    public Ghost3DAppearanceController() {
        numFlashes = 3;
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = numFlashes;
    }

    public void init(Ghost3D ghost3D, GameLevel level) {
        ghost3D.stopAllAnimations();
        setGhostAppearance(ghost3D, GhostAppearance.NORMAL);
    }

    public void update(Ghost3D ghost3D, GameLevel level) {
        final GhostState ghostState = ghost3D.ghost().state();

        // Let ghost shown as number alone
        if (ghostState == GhostState.EATEN) return;

        final boolean powerActive = level.pac().powerTimer().isRunning();
        final boolean powerFading = level.pac().isPowerFading(level);
        // ghosts that already got killed in the current power phase do not look frightened anymore
        final boolean killedAlready = level.energizerVictims().contains(ghost3D.ghost());

        setGhostAppearance(ghost3D, switch (ghostState) {
            case LOCKED, LEAVING_HOUSE -> powerActive && !killedAlready
                ? ghost3D.frightenedAppearance(powerFading)
                : GhostAppearance.NORMAL;
            case FRIGHTENED -> ghost3D.frightenedAppearance(powerFading);
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            default -> GhostAppearance.NORMAL;
        });
    }

    public void setGhostAppearance(Ghost3D ghost3D, GhostAppearance ghostAppearance) {
        switch (ghostAppearance) {
            case NORMAL -> {
                ghost3D.setNormalLook();
                ghost3D.animateDress(ghost3D.isVisible());
                brakeIfTunnelEntered(ghost3D);
            }
            case FRIGHTENED -> {
                ghost3D.setFrightenedLook();
                ghost3D.animateDress(ghost3D.isVisible());
            }
            case FLASHING -> {
                ghost3D.setFlashingLook(numFlashes);
                ghost3D.animateDress(ghost3D.isVisible());
            }
            case EYES -> {
                ghost3D.setEyesOnlyLook();
                ghost3D.animateDress(false);
            }
        }
        Logger.debug("Ghost appearance for {} is now {}", ghost3D.ghost().name(), ghostAppearance);
    }

    private void brakeIfTunnelEntered(Ghost3D ghost3D) {
        final Ghost ghost = ghost3D.ghost();
        if (ghost.moveInfo().tunnelEntered) {
            ghost3D.animations().animation(Ghost3D.AnimationID.GHOST_BRAKING.forGhost(ghost)).playFromStart();
        }
    }
}
