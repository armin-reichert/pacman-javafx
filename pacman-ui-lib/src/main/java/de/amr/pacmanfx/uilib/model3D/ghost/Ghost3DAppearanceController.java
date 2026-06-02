/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.ghost;

import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;

public class Ghost3DAppearanceController {

    public Ghost3DAppearanceController() {}

    public void init(Ghost3D ghost3D) {
        ghost3D.stopAllAnimations();
        lookNormal(ghost3D);
    }

    public void update(Ghost3D ghost3D, GameLevel level) {
        final Pac pac = level.entities().pac();
        final Ghost ghost = ghost3D.ghost();

        final GhostAppearance appearance = switch (ghost.state()) {
            case LOCKED, LEAVING_HOUSE -> {
                //TODO maybe the (model) ghost should store the "frightened no more" state?
                final boolean powerActive = pac.powerTimer().isRunning();
                final boolean powerFading = pac.isPowerFading(level);
                final boolean killedDuringCurrentPhase = level.killedGhostsForCurrentEnergizer().contains(ghost);
                yield powerActive && !killedDuringCurrentPhase
                    ? powerFading ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED
                    : GhostAppearance.NORMAL;
            }
            case FRIGHTENED -> pac.isPowerFading(level) ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case EATEN -> GhostAppearance.EATEN;
            default -> GhostAppearance.NORMAL;
        };

        switch (appearance) {
            case NORMAL -> lookNormal(ghost3D);
            case FRIGHTENED -> lookFrightened(ghost3D);
            case FLASHING -> lookFlashing(ghost3D, level.numFlashes());
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
        ghost3D.dressMeshView().setVisible(true);
        selectMaterialSet(ghost3D, ghost3D.materials().normalMaterial());

        ghost3D.dressColorFlashingAnimation().ifPresent(ManagedAnimation::stop);
        ghost3D.dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
        brakeIfTunnelEntered(ghost3D);
    }

    private void lookFlashing(Ghost3D ghost3D, int numFlashes) {
        if (numFlashes == 0) {
            lookFrightened(ghost3D);
            return;
        }
        ghost3D.dressMeshView().setVisible(true);
        selectMaterialSet(ghost3D, ghost3D.materials().flashingMaterial());

        ghost3D.dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
        ghost3D.dressColorFlashingAnimation().ifPresent(flashing -> {
            flashing.setNumFlashes(numFlashes);
            flashing.playOrContinue();
        });
    }

    private void lookFrightened(Ghost3D ghost3D) {
        ghost3D.dressMeshView().setVisible(true);
        selectMaterialSet(ghost3D, ghost3D.materials().frightenedMaterial());

        ghost3D.dressColorFlashingAnimation().ifPresent(ManagedAnimation::stop);
        ghost3D.dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
    }

    private void lookEyesOnly(Ghost3D ghost3D) {
        ghost3D.dressMeshView().setVisible(false);
        selectMaterialSet(ghost3D, ghost3D.materials().normalMaterial());

        ghost3D.stopAllAnimations();
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
