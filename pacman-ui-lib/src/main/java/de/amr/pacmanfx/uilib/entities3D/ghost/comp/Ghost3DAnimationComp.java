package de.amr.pacmanfx.uilib.entities3D.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.anim.GhostFlashingAnimation3D;

import java.util.EnumMap;
import java.util.Map;

public class Ghost3DAnimationComp implements GameEntityComponent {

    private final Map<GhostPersonality, GhostFlashingAnimation3D> flashingAnimations = new EnumMap<>(GhostPersonality.class);

    public Ghost3DAnimationComp(GhostSettings settings, GhostComponentMaterialSet flashingMaterialSet) {
        for (GhostPersonality gp : GhostPersonality.values()) {
            final var flashingAnimation = new GhostFlashingAnimation3D(gp, settings, flashingMaterialSet);
            flashingAnimations.put(gp, flashingAnimation);
        }
    }

    public GhostFlashingAnimation3D flashingAnimation(GhostPersonality gp) {
        return flashingAnimations.get(gp);
    }

    @Override
    public void reset() {
    }
}
