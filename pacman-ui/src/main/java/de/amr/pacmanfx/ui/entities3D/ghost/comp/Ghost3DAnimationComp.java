package de.amr.pacmanfx.ui.entities3D.ghost.comp;

import de.amr.basics.Disposable;
import de.amr.basics.ecs.Resettable;
import de.amr.basics.ui.animation.AnimationRegistry;
import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.GhostBrakeAnimation3D;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.GhostDressAnimation3D;
import de.amr.pacmanfx.ui.entities3D.ghost.anim.GhostFlashingAnimation3D;

public class Ghost3DAnimationComp implements Disposable, Resettable {

    private ManagedAnimation flashing;
    private ManagedAnimation dressMovement;
    private ManagedAnimation braking;

    public Ghost3DAnimationComp() {}

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

    public void build(
        AnimationRegistry animationRegistry,
        Ghost ghost,
        GhostSettings settings,
        int numFlashes)
    {
        flashing = new GhostFlashingAnimation3D(ghost, settings, numFlashes);
        animationRegistry.register("Ghost.%s.flashing".formatted(ghost.name()), flashing);

        dressMovement = new GhostDressAnimation3D(ghost);
        animationRegistry.register("Ghost.%s.dressMovement".formatted(ghost.name()), dressMovement);

        braking = new GhostBrakeAnimation3D(ghost);
        animationRegistry.register("Ghost.%s.braking".formatted(ghost.name()), braking);
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
