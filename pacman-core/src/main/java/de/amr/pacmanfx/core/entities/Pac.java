/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SteeringComp;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
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

        setComp(MovementComp.class, new MovementComp());
        setComp(WorldNavigationComp.class, new WorldNavigationComp());
        setComp(SteeringComp.class, new SteeringComp<Pac>());
        setComp(PacDigestionComp.class, new PacDigestionComp());
        setComp(PacPowerComp.class, new PacPowerComp());
        setComp(PacCheatsComp.class, new PacCheatsComp());
        setComp(PacStateComp.class, new PacStateComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
        setComp(PacAnimationComp.class, new PacAnimationComp());
    }

    public MovementComp movement() {
        return reqComp(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return reqComp(WorldNavigationComp.class);
    }

    @SuppressWarnings("unchecked")
    public SteeringComp<Pac> autoSteering() {
        return (SteeringComp<Pac>) reqComp(SteeringComp.class);
    }

    public PacDigestionComp digestion() {
        return reqComp(PacDigestionComp.class);
    }

    public PacPowerComp power() {
        return reqComp(PacPowerComp.class);
    }

    public PacCheatsComp cheats() {
        return reqComp(PacCheatsComp.class);
    }

    public PacStateComp state() {
        return reqComp(PacStateComp.class);
    }

    public PacAnimationComp animation() {
        return reqComp(PacAnimationComp.class);
    }

    //TODO integrate with Pac animation comp
    public SpriteAnimationComp spriteAnim() {
        return reqComp(SpriteAnimationComp.class);
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
        spriteAnim().animation().select(CommonSpriteAnimationID.PAC_FULL);
        spriteAnim().animation().resetSelected();
    }

    public PacState getPacState() {
        return state().enumValue();
    }

}