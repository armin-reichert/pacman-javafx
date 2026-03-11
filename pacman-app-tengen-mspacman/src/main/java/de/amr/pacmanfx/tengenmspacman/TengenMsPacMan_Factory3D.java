/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.config.ActorConfig;
import de.amr.pacmanfx.ui.config.Config3D;
import de.amr.pacmanfx.ui.config.PacConfig;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.model3D.*;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Factory3D implements Factory3D {

    @Override
    public MsPacMan3D createPac3D(
        Pac pac,
        AssetMap assets,
        ActorConfig actorConfig,
        AnimationRegistry animationRegistry)
    {
        requireNonNull(pac);
        requireNonNull(assets);
        requireNonNull(actorConfig);
        requireNonNull(animationRegistry);

        final PacConfig pacConfig = actorConfig.pacConfig();
        var pac3D = new MsPacMan3D(
            animationRegistry,
            pac,
            pacConfig.size3D(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor(),
            pacConfig.hairbowColor(),
            pacConfig.hairBowPearlsColor(),
            pacConfig.boobsColor()
        );

        pac3D.light().setColor(pacConfig.headColor().desaturate());
        return pac3D;
    }

    @Override
    public MutableGhost3D createMutableGhost3D(
        Ghost ghost,
        AssetMap assets,
        ActorConfig actorConfig,
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
            actorConfig.ghostSize(),
            numFlashings
        );
    }

    @Override
    public MsPacManBody createLivesCounterShape3D(AssetMap assets, Config3D config3D) {
        final PacConfig pacConfig = config3D.actor().pacConfig();
        return Models3D.PAC_MAN_MODEL.createMsPacManBody(
            config3D.livesCounter().shapeSize(),
            pacConfig.headColor(),
            pacConfig.eyesColor(),
            pacConfig.palateColor(),
            pacConfig.hairbowColor(),
            pacConfig.hairBowPearlsColor(),
            pacConfig.boobsColor()
        );
    }
}
