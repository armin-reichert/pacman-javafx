/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.entities;

import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.pac.*;
import de.amr.pacmanfx.core.model.comp.spriteanim.SpriteAnimComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends GameEntity {

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(PacDigestionComp.class, new PacDigestionComp());
        setComponent(PacPowerComp.class, new PacPowerComp());
        setComponent(PacCheatsComp.class, new PacCheatsComp());
        setComponent(PacStateComp.class, new PacStateComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());
    }

    //TODO state entity component

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return requireComponent(WorldNavigationComp.class);
    }

    public PacDigestionComp digestion() {
        return requireComponent(PacDigestionComp.class);
    }

    public PacPowerComp power() {
        return requireComponent(PacPowerComp.class);
    }

    public PacCheatsComp cheats() {
        return requireComponent(PacCheatsComp.class);
    }

    public PacStateComp stateComp() {
        return requireComponent(PacStateComp.class);
    }

    @Override
    public String toString() {
        return "Pac{" +
            "name=" + name +
            ", state=" + state() +
            ", visible=" + visibility() +
            ", position=" + pos() +
            ", movement=" + movement() +
            ", worldNavigation=" + worldNavigation() +
            ", digestion=" + digestion() +
            ", power=" + power() +
            ", cheats=" + cheats() +
            '}';
    }

    @Override
    public void reset() {
        super.reset();
        worldNavigation().corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing
        //TODO check this
        requireComponent(SpriteAnimComp.class).delegate().select(ActorAnimationID.PAC_MUNCHING);
    }

    public PacState state() {
        return stateComp().state();
    }

}