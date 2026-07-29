/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.pac.PacCheats;
import de.amr.pacmanfx.core.model.component.pac.PacDigestion;
import de.amr.pacmanfx.core.model.component.pac.PacPower;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.steering.Steering;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends Actor implements UpdatableEntity {

    public enum State {ACTIVE, DEAD }

    private State state;

    private Steering automaticSteering;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);

        setComponent(Movement.class, new Movement());
        setComponent(WorldNavigation.class, new WorldNavigation());
        setComponent(PacDigestion.class, new PacDigestion());
        setComponent(PacPower.class, new PacPower());
        setComponent(PacCheats.class, new PacCheats());
        setComponent(SpriteAnim.class, new SpriteAnim());

        state = State.ACTIVE;
    }

    public Movement movement() {
        return assertComponent(Movement.class);
    }

    public WorldNavigation worldNavigation() {
        return assertComponent(WorldNavigation.class);
    }

    public PacDigestion digestion() {
        return assertComponent(PacDigestion.class);
    }

    public PacPower power() {
        return assertComponent(PacPower.class);
    }

    public PacCheats cheats() {
        return assertComponent(PacCheats.class);
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

    public void setAutomaticSteering(Steering steering) {
        automaticSteering = requireNonNull(steering);
    }

    @Override
    public void reset() {
        super.reset();

        state = State.ACTIVE;
        worldNavigation().corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing

        //TODO check this
        assertComponent(SpriteAnim.class).delegate().select(CommonAnimationID.PAC_MUNCHING);
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

        if (state == State.DEAD || digestion().restingTicks() == PacDigestion.REST_FOREVER) {
            return;
        }

        sys.pacDigestion().update(this);
        if (sys.pacDigestion().isResting(this)) {
            return;
        }

        if (cheats().isUsingAutopilot()) {
            automaticSteering.steer(this, gameContext);
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