/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.factory;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DMaterials;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.scene.shape.Mesh;

public class Ghost3DFactory {

    public static void createGhostView3D(
        Ghost ghost,
        GhostSettings settings,
        Ghost3DMaterials materialSet,
        Mesh dressMesh,
        Mesh pupilsMesh,
        Mesh eyeballsMesh)
    {
        ensureGhostHas3DView(ghost, settings, materialSet);
        ghost.requireComponent(Ghost3DViewComp.class).build(settings, dressMesh, pupilsMesh, eyeballsMesh);
    }

    private static void ensureGhostHas3DView(Ghost ghost, GhostSettings settings, Ghost3DMaterials materialSet) {
        if (!ghost.hasComponent(Ghost3DViewComp.class)) {
            ghost.setComponent(Ghost3DViewComp.class, new Ghost3DViewComp());
            ghost.setComponent(Ghost3DAnimationComp.class, new Ghost3DAnimationComp(settings, materialSet));
        }
    }
}
