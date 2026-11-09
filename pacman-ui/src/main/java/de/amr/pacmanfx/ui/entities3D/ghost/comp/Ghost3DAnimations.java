package de.amr.pacmanfx.ui.entities3D.ghost.comp;

import de.amr.basics.Disposable;
import de.amr.basics.ecs.Resettable;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.Ghost3DBrakeAnimation;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.Ghost3DDressAnimation;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.Ghost3DFlashingAnimation;

public class Ghost3DAnimations implements Disposable, Resettable {

    private ManagedAnimation flashing;
    private ManagedAnimation dressMovement;
    private ManagedAnimation braking;

    public Ghost3DAnimations() {}

    @Override
    public void dispose() {
        if (flashing != null) {
            flashing.dispose();
            flashing = null;
        }
        if (dressMovement != null) {
            dressMovement.dispose();
            dressMovement = null;
        }
        if (braking != null) {
            braking.dispose();
            braking = null;
        }
    }

    public ManagedAnimation flashing() {
        return flashing;
    }

    public ManagedAnimation dressMovement() {
        return dressMovement;
    }

    public ManagedAnimation braking() {
        return braking;
    }


    public void createAnimations(
        Ghost ghost,
        GhostSettings settings,
        int numFlashes)
    {
        flashing = new Ghost3DFlashingAnimation(ghost, settings, numFlashes);
        dressMovement = new Ghost3DDressAnimation(ghost);
        braking = new Ghost3DBrakeAnimation(ghost);
    }

    @Override
    public void reset() {
        if (flashing != null) {
            flashing.stop();
        }
        if (dressMovement != null) {
            dressMovement.stop();
        }
    }

    public void lookNormal() {
        if (flashing != null) {
            flashing.stop();
        }
        if (dressMovement != null) {
            dressMovement.playOrContinue();
        }
//        brakeIfTunnelEntered(ghost3D);
    }

    public void lookFrightened() {
        if (flashing != null) {
            flashing.stop();
        }
        if (dressMovement != null) {
            dressMovement.playOrContinue();
        }
    }

    public void lookEyesOnly() {
        if (flashing != null) {
            flashing.stop();
        }
        if (dressMovement != null) {
            dressMovement.stop();
        }
    }
}
