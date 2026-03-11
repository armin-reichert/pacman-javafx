/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.config.ActorConfig3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.*;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D implements Factory3D {

    @Override
    public MsPacMan3D createPac3D(
        Pac pac,
        AssetMap assets,
        ActorConfig3D actorConfig3D,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(assets);
        requireNonNull(actorConfig3D);
        requireNonNull(animationRegistry);

        var pac3D = new MsPacMan3D(
            animationRegistry,
            pac,
            actorConfig3D.pacSize(),
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );

        pac3D.light().setColor(assets.color("pac.color.head").desaturate());
        return pac3D;
    }

    @Override
    public MutableGhost3D createMutableGhost3D(
        Ghost ghost,
        AssetMap assets,
        ActorConfig3D actorConfig3D,
        GhostColorSet colorSet,
        AnimationRegistry animationRegistry,
        int numFlashings)
    {
        return new MutableGhost3D(
            animationRegistry,
            ghost,
            colorSet,
            Models3D.GHOST_MODEL.dressMesh(),
            Models3D.GHOST_MODEL.pupilsMesh(),
            Models3D.GHOST_MODEL.eyeballsMesh(),
            actorConfig3D.ghostSize(),
            numFlashings
        );
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(AssetMap assets, double size) {
        return Models3D.PAC_MAN_MODEL.createMsPacManBody(
            size,
            assets.color("pac.color.head"),
            assets.color("pac.color.eyes"),
            assets.color("pac.color.palate"),
            assets.color("pac.color.hairbow"),
            assets.color("pac.color.hairbow.pearls"),
            assets.color("pac.color.boobs")
        );
    }
}
