package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.anim.GhostDressAnimation3D;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.anim.GhostFlashingAnimation3D;

public class Ghost3DAnimationComp implements GameEntityComponent {

    private ManagedAnimation flashing;
    private ManagedAnimation dressMovement;

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
        animationRegistry.register(this, flashing); //TODO needed?

        dressMovement = new GhostDressAnimation3D(ghost);
        animationRegistry.register(this, dressMovement);
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
