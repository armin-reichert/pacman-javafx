/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.pac;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.entities.actor.SteeringComp;
import de.amr.pacmanfx.core.entities.world.WorldNavigationComp;

import static java.util.Objects.requireNonNull;

/**
 * Pac-Man / Ms. Pac-Man.
 */
public final class Pac extends GameEntity {

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name, boolean male) {
        this.name = requireNonNull(name);

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(SteeringComp.class, new SteeringComp<Pac>());
        setComponent(PacDigestionComp.class, new PacDigestionComp());
        setComponent(PacPowerComp.class, new PacPowerComp());
        setComponent(PacCheatsComp.class, new PacCheatsComp());
        setComponent(PacStateComp.class, new PacStateComp(male));
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());
        setComponent(PacAnimationComp.class, new PacAnimationComp());
    }

    public MovementComp movement() {
        return assertComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return assertComponent(WorldNavigationComp.class);
    }

    @SuppressWarnings("unchecked")
    public SteeringComp<Pac> autoSteering() {
        return (SteeringComp<Pac>) assertComponent(SteeringComp.class);
    }

    public PacDigestionComp digestion() {
        return assertComponent(PacDigestionComp.class);
    }

    public PacPowerComp power() {
        return assertComponent(PacPowerComp.class);
    }

    public PacCheatsComp cheats() {
        return assertComponent(PacCheatsComp.class);
    }

    public PacStateComp state() {
        return assertComponent(PacStateComp.class);
    }

    public PacAnimationComp animation() {
        return assertComponent(PacAnimationComp.class);
    }

    //TODO integrate with Pac animation comp
    public SpriteAnimationComp spriteAnim() {
        return assertComponent(SpriteAnimationComp.class);
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
        //spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.PAC_MOUTH_SHUT);
        spriteAnim().spriteAnimations().resetSelected();
    }
}