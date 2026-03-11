/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.PacBase3D;
import de.amr.pacmanfx.uilib.model3D.PacBody;
import de.amr.pacmanfx.uilib.model3D.PacMan3D;

public class PacManXXL_PacMan_Factory3D implements Factory3D {

    @Override
    public PacBase3D createPac3D(AssetMap assets, AnimationRegistry animationRegistry, Pac pac, double size) {
        var pacMan3D = new PacMan3D(
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
        pacMan3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pacMan3D;
    }

    @Override
    public PacBody createLivesCounterShape3D(AssetMap assets, double size) {
        return Models3D.PAC_MAN_MODEL.createPacBody(
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate")
        );
    }


}
