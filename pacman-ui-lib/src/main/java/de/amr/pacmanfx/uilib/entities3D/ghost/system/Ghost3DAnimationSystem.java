package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;

public class Ghost3DAnimationSystem {

    public static void update(Ghost ghost) {
        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);
        checkForFlashingStart(ghost, view3D);
    }

    private static void checkForFlashingStart(Ghost ghost, Ghost3DViewComp view3D) {
        final GhostStateComp state = ghost.state();
        if (state.flashing()) {
            view3D.lookFlashing();
            final Ghost3DAnimationComp animation = ghost.requireComponent(Ghost3DAnimationComp.class);
            //dressAnimation().ifPresent(ManagedAnimation::playOrContinue);
            final var flashing = animation.flashingAnimation(ghost.personality());
            flashing.setNumFlashes(3); //TODO
            flashing.playOrContinue();
        }
    }

}
