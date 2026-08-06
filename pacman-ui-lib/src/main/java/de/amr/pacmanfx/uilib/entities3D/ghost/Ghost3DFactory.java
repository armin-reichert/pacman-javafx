/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.scene.shape.Mesh;

public class Ghost3DFactory {

    public static void createGhostView3D(Ghost ghost, GhostSettings settings, Mesh dressMesh, Mesh pupilsMesh, Mesh eyeballsMesh) {
        ensureGhostHas3DView(ghost);
        ghost.requireComponent(Ghost3DViewComp.class)
            .build(settings, dressMesh, pupilsMesh, eyeballsMesh);
    }

    private static void ensureGhostHas3DView(Ghost ghost) {
        if (!ghost.hasComponent(Ghost3DViewComp.class)) {
            ghost.setComponent(Ghost3DViewComp.class, new Ghost3DViewComp());
            //TODO other 3D related components
        }
    }
}
