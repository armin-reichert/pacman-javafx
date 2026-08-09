/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d3;

import de.amr.pacmanfx.uilib.entities3D.world.Energizer3D;

public class GameLevel3DAnimationSystem {

    public static void startEnergizerPumping(GameLevel3DAnimations animations, Energizer3D energizer3D) {
        animations.startPumping(energizer3D);
    }

    public static void stopEnergizerPumping(GameLevel3DAnimations animations, Energizer3D energizer3D) {
        animations.stopPumping(energizer3D);
    }
}
