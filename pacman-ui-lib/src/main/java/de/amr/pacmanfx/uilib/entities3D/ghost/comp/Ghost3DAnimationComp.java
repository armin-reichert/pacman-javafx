package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.anim.GhostFlashingAnimation3D;

public class Ghost3DAnimationComp implements GameEntityComponent {

    private GhostFlashingAnimation3D flashing;

    public Ghost3DAnimationComp() {
    }

    public GhostFlashingAnimation3D flashing() {
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
    }

    @Override
    public void reset() {
        if (flashing != null) {
            flashing.stop();
        }
    }

    public void lookNormal() {
        if (flashing != null) {
            flashing.stop();
        }
//        dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
//        brakeIfTunnelEntered(ghost3D);
    }

    public void lookFrightened() {
        if (flashing != null) {
            flashing.stop();
        }
//        dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
    }

    public void lookEyesOnly() {
        if (flashing != null) {
            flashing.stop();
        }
    }

}
