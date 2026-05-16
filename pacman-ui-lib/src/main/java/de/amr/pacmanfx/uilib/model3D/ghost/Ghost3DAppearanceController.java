/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
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
        final GhostAppearance appearance = computeAppearance(ghost3D, level);
        setGhostAppearance(ghost3D, appearance);
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
                    ? powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED
                    : GhostAppearance.NORMAL;
            }
            case FRIGHTENED -> {
                final boolean powerFading = pac.isPowerFading(level);
                yield powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
            }
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case EATEN -> GhostAppearance.EATEN;
            default -> GhostAppearance.NORMAL;
        };
    }

    private void setGhostAppearance(Ghost3D ghost3D, GhostAppearance ghostAppearance) {
        switch (ghostAppearance) {
            case NORMAL -> lookNormal(ghost3D);
            case FRIGHTENED -> lookFrightened(ghost3D);
            case FLASHING -> lookFlashing(ghost3D, numFlashes);
            case EYES -> lookEyesOnly(ghost3D);
            case EATEN -> lookEaten(ghost3D);
        }
    }

    private void brakeIfTunnelEntered(Ghost3D ghost3D) {
        final Ghost ghost = ghost3D.ghost();
        if (ghost.moveInfo().tunnelEntered) {
            ghost3D.animations().animation(Ghost3D.AnimationID.BRAKING.key(ghost)).playFromStart();
        }
    }

    private void lookNormal(Ghost3D ghost3D) {
        ghost3D.flashingDressAnimation().stop();
        ghost3D.normalDressAnimation().playOrContinue();
        ghost3D.dressMeshView().setVisible(true);
        selectMaterialSet(ghost3D, ghost3D.materials().normalMaterial());
        brakeIfTunnelEntered(ghost3D);
    }

    private void lookFlashing(Ghost3D ghost3D, int numFlashes) {
        if (numFlashes == 0) {
            lookFrightened(ghost3D);
            return;
        }
        selectMaterialSet(ghost3D, ghost3D.materials().flashingMaterial());
        ghost3D.dressMeshView().setVisible(true);

        ghost3D.normalDressAnimation().playOrContinue();
        ghost3D.flashingDressAnimation().setNumFlashes(numFlashes);
        ghost3D.flashingDressAnimation().playOrContinue();
    }

    private void lookFrightened(Ghost3D ghost3D) {
        ghost3D.flashingDressAnimation().stop();
        ghost3D.normalDressAnimation().playOrContinue();
        ghost3D.dressMeshView().setVisible(true);
        selectMaterialSet(ghost3D, ghost3D.materials().frightenedMaterial());
    }

    private void lookEyesOnly(Ghost3D ghost3D) {
        ghost3D.stopAllAnimations();
        ghost3D.dressMeshView().setVisible(false);
        selectMaterialSet(ghost3D, ghost3D.materials().normalMaterial());
    }

    private void lookEaten(Ghost3D ghost3D) {
        ghost3D.setVisible(false);
    }

    private void selectMaterialSet(Ghost3D ghost3D, GhostComponentMaterialSet materialSet) {
        ghost3D.dressMeshView().setMaterial(materialSet.dressMaterial());
        ghost3D.pupilsMeshView().setMaterial(materialSet.pupilsMaterial());
        ghost3D.eyeballsMeshView().setMaterial(materialSet.eyeballsMaterial());
    }
}
