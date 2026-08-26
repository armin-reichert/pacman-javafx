package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.anim.GhostBrakeAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost.anim.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost.anim.GhostFlashingAnimation3D;

public class Ghost3DAnimationComp implements GameEntityComp {

    private ManagedAnimation flashing;
    private ManagedAnimation dressMovement;
    private ManagedAnimation braking;

    public Ghost3DAnimationComp() {
    }

    public ManagedAnimation flashing() {
        return flashing;
    }

    public void build(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostSettings settings,
        int numFlashes)
    {
        flashing = new GhostFlashingAnimation3D(ghost, settings, numFlashes);
        animationRegistry.register(flashing, flashing);

        dressMovement = new GhostDressAnimation3D(ghost);
        animationRegistry.register(dressMovement, dressMovement);

        braking = new GhostBrakeAnimation3D(ghost);
        animationRegistry.register(braking, braking);
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

    public ManagedAnimation braking() {
        return braking;
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
