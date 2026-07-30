/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.pac.PacCheatsComponent;
import de.amr.pacmanfx.core.model.component.pac.PacDigestionComponent;
import de.amr.pacmanfx.core.model.component.pac.PacPowerComponent;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnimComponent;
import de.amr.pacmanfx.core.model.component.world.WorldNavigationComponent;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.steering.Steering;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends GameEntity implements UpdatableEntity {

    public enum State {ACTIVE, DEAD }

    private State state;

    private Steering<Pac> automaticSteering;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);

        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(WorldNavigationComponent.class, new WorldNavigationComponent());
        setComponent(PacDigestionComponent.class, new PacDigestionComponent());
        setComponent(PacPowerComponent.class, new PacPowerComponent());
        setComponent(PacCheatsComponent.class, new PacCheatsComponent());
        setComponent(SpriteAnimComponent.class, new SpriteAnimComponent());

        state = State.ACTIVE;
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }

    public WorldNavigationComponent worldNavigation() {
        return requireComponent(WorldNavigationComponent.class);
    }

    public PacDigestionComponent digestion() {
        return requireComponent(PacDigestionComponent.class);
    }

    public PacPowerComponent power() {
        return requireComponent(PacPowerComponent.class);
    }

    public PacCheatsComponent cheats() {
        return requireComponent(PacCheatsComponent.class);
    }

    @Override
    public String toString() {
        return "Pac{" +
            "name=" + name +
            ", state=" + state +
            ", visible=" + visibility() +
            ", position=" + position() +
            ", movement=" + movement() +
            ", worldNavigation=" + worldNavigation() +
            ", digestion=" + digestion() +
            ", power=" + power() +
            ", cheats=" + cheats() +
            '}';
    }

    public void setAutomaticSteering(Steering<Pac> steering) {
        automaticSteering = requireNonNull(steering);
    }

    @Override
    public void reset() {
        super.reset();

        state = State.ACTIVE;
        worldNavigation().corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing

        //TODO check this
        requireComponent(SpriteAnimComponent.class).delegate().select(ActorAnimationID.PAC_MUNCHING);
    }

    public State state() {
        return state;
    }

    public void setState(State state) {
        this.state = requireNonNull(state);
    }

    @Override
    public void update(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();
        final GameLevel level = gameContext.assertLevel();

        if (state == State.DEAD || digestion().restingTicks() == PacDigestionComponent.REST_FOREVER) {
            return;
        }

        sys.pacDigestion().update(this);
        if (sys.pacDigestion().isResting(this)) {
            return;
        }

        if (cheats().isUsingAutopilot()) {
            automaticSteering.steer(this, level);
        }

        final float speed = sys.pacPower().isPowerActive(this)
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level);

        sys.navigator().setSpeed(this, speed);
        sys.navigator().tryMovingOrTeleporting(this, level, sys.pacWorldMovementPolicy());

        if (worldNavigation().info.moved) {
            sys.spriteAnim().playSelected(this);
        } else {
            sys.spriteAnim().stopSelected(this);
        }
    }

    public boolean isBlocked() {
        return movement().hasZeroSpeed() || didNotMoveThroughWorld();
    }

    private boolean didNotMoveThroughWorld() {
        return !worldNavigation().info.moved;
    }
}