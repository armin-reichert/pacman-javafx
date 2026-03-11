/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.MsPacMan3D;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_Factory3D implements Factory3D {

    @Override
    public MsPacMan3D createPac3D(AssetMap assets, AnimationRegistry animationRegistry, Pac pac, double size) {
        requireNonNull(animationRegistry);
        requireNonNull(pac);
        final var msPacMan3D = new MsPacMan3D(
            animationRegistry,
            pac,
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs"));
        msPacMan3D.light().setColor(assets.color("pac.color.head").desaturate());
        return msPacMan3D;
    }

}
