/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.pac.PacCheatsComp;
import de.amr.pacmanfx.core.model.comp.pac.PacDigestionComp;
import de.amr.pacmanfx.core.model.comp.pac.PacPowerComp;
import de.amr.pacmanfx.core.model.comp.spriteanim.SpriteAnimComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.rules.ActorSpeedRules;
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

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(PacDigestionComp.class, new PacDigestionComp());
        setComponent(PacPowerComp.class, new PacPowerComp());
        setComponent(PacCheatsComp.class, new PacCheatsComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());

        state = State.ACTIVE;
    }

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

    @Override
    public String toString() {
        return "Pac{" +
            "name=" + name +
            ", state=" + state +
            ", visible=" + visibility() +
            ", position=" + pos() +
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
        requireComponent(SpriteAnimComp.class).delegate().select(ActorAnimationID.PAC_MUNCHING);
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

        if (state == State.DEAD || digestion().restingTicks() == PacDigestionComp.REST_FOREVER) {
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