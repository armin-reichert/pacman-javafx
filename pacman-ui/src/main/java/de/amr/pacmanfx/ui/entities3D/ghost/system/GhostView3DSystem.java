package de.amr.pacmanfx.ui.entities3D.ghost.system;

import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostStateComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.Ghost3DMaterialSet;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostView3D;
import de.amr.pacmanfx.ui.entities3D.ghost.comp.GhostAppearance;

public class GhostView3DSystem {

    public GhostView3DSystem() {}

    public void update(Ghost ghost) {
        final GhostStateComp state = ghost.state();

        final GhostAppearance appearance = switch (state.enumValue()) {
            case LOCKED -> state.hasPacPower() ? appearFrightenedOrFlashing(state) : GhostAppearance.NORMAL;
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case FRIGHTENED -> appearFrightenedOrFlashing(state);
            case HUNTING_PAC, LEAVING_HOUSE -> GhostAppearance.NORMAL;
        };
        setAppearance(ghost, appearance);
    }

    private void lookNormal(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(true);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().normal());
    }

    private void lookFrightened(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(true);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().frightened());
    }

    private void lookEyesOnly(GhostView3D ghostView3D) {
        ghostView3D.dressMeshView()   .setVisible(false);
        ghostView3D.eyeballsMeshView().setVisible(true);
        ghostView3D.pupilsMeshView()  .setVisible(true);

        applyMaterials(ghostView3D, ghostView3D.appearanceMaterialSet().normal());
    }

    private void applyMaterials(GhostView3D ghostView3D, Ghost3DMaterialSet materials) {
        ghostView3D.dressMeshView()   .setMaterial(materials.dress());
        ghostView3D.pupilsMeshView()  .setMaterial(materials.pupils());
        ghostView3D.eyeballsMeshView().setMaterial(materials.eyeballs());
    }

    private GhostAppearance appearFrightenedOrFlashing(GhostStateComp state) {
        return state.isPacPowerFading() ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    private void setAppearance(Ghost ghost, GhostAppearance appearance) {
        final GhostView3D view3D = ghost.assertComponent(GhostView3D.class);
        final Ghost3DAnimationComp animation3D = ghost.assertComponent(Ghost3DAnimationComp.class);

        view3D.setAppearance(appearance);
        switch (appearance) {
            case EATEN, EYES -> {
                lookEyesOnly(view3D);
                animation3D.lookEyesOnly();
            }
            case FLASHING -> {
                lookFrightened(view3D);
                ensureFlashingPlays(animation3D.flashing());
            }
            case FRIGHTENED -> {
                lookFrightened(view3D);
                animation3D.lookFrightened();
            }
            case NORMAL -> {
                lookNormal(view3D);
                animation3D.lookNormal();
            }
        }
    }

    private void ensureFlashingPlays(ManagedAnimation flashing) {
        if (!flashing.isRunning()) {
            flashing.playOrContinue();
        }
    }
}