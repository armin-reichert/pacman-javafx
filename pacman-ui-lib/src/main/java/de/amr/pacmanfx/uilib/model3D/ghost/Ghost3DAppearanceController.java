/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;

public class Ghost3DAppearanceController {

    private int numFlashes;

    public Ghost3DAppearanceController() {
        numFlashes = 3;
    }

    public void setNumFlashes(int numFlashes) {
        this.numFlashes = numFlashes;
    }

    public void init(Ghost3D ghost3D) {
        ghost3D.stopAllAnimations();
        setGhostAppearance(ghost3D, GhostAppearance.NORMAL);
    }

    public void update(Ghost3D ghost3D, GameLevel level) {
        // Eaten ghost is invisible and needs no update
        if (ghost3D.ghost().state() != GhostState.EATEN) {
            final GhostAppearance appearance = computeAppearance(ghost3D, level);
            setGhostAppearance(ghost3D, appearance);
        }
    }

    private GhostAppearance computeAppearance(Ghost3D ghost3D, GameLevel level) {
        final Pac pac = level.pac();
        final Ghost ghost = ghost3D.ghost();
        return switch (ghost.state()) {
            case LOCKED, LEAVING_HOUSE -> {
                final boolean powerActive = pac.powerTimer().isRunning();
                final boolean powerFading = pac.isPowerFading(level);
                final boolean killedDuringCurrentPhase = level.energizerVictims().contains(ghost);
                yield powerActive && !killedDuringCurrentPhase
                    ? ghost3D.frightenedAppearance(powerFading)
                    : GhostAppearance.NORMAL;
            }
            case FRIGHTENED -> {
                final boolean powerFading = pac.isPowerFading(level);
                yield ghost3D.frightenedAppearance(powerFading);
            }
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            default -> GhostAppearance.NORMAL;
        };
    }

    private void setGhostAppearance(Ghost3D ghost3D, GhostAppearance ghostAppearance) {
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
    }

    private void brakeIfTunnelEntered(Ghost3D ghost3D) {
        final Ghost ghost = ghost3D.ghost();
        if (ghost.moveInfo().tunnelEntered) {
            ghost3D.animations().animation(Ghost3D.AnimationID.GHOST_BRAKING.forGhost(ghost)).playFromStart();
        }
    }
}
