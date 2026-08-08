/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.AutoSteeringComp;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.entities.pac.comp.*;

import static java.util.Objects.requireNonNull;

/**
 * Pac-Man / Ms. Pac-Man.
 */
public final class Pac extends GameEntity {

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(AutoSteeringComp.class, new AutoSteeringComp());
        setComponent(PacDigestionComp.class, new PacDigestionComp());
        setComponent(PacPowerComp.class, new PacPowerComp());
        setComponent(PacCheatsComp.class, new PacCheatsComp());
        setComponent(PacStateComp.class, new PacStateComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
        setComponent(PacAnimationComp.class, new PacAnimationComp());
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return requireComponent(WorldNavigationComp.class);
    }

    public AutoSteeringComp autoSteering() {
        return requireComponent(AutoSteeringComp.class);
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

    public PacStateComp state() {
        return requireComponent(PacStateComp.class);
    }

    public PacAnimationComp animation() {
        return requireComponent(PacAnimationComp.class);
    }

    //TODO integrate with Pac animation comp
    public SpriteAnimationComp spriteAnim() {
        return requireComponent(SpriteAnimationComp.class);
    }

    @Override
    public String toString() {
        return "Pac{" +
            "name=" + name +
            ", state=" + getPacState() +
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
        spriteAnim().animation().select(CommonSpriteAnimationID.PAC_MUNCHING);
    }

    public PacState getPacState() {
        return state().enumValue();
    }

}