/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.*;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.PacDigestionSystem;
import de.amr.pacmanfx.core.model.systems.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.steering.Steering;

import static java.util.Objects.requireNonNull;

/**
 * Base class for Pac-Man / Ms. Pac-Man.
 */
public class Pac extends Actor implements UpdatableEntity {

    public enum State { WALKING, STUCK, DEAD }

    private State state;

    private Steering automaticSteering;

    /**
     * @param name a readable name. Any honest Pac-Man and Pac-Woman should have a name! Period.
     */
    public Pac(String name) {
        this.name = requireNonNull(name);

        registerComponent(Movement.class, new Movement());
        registerComponent(WorldMovement.class, new WorldMovement());
        registerComponent(WorldMovementPolicy.class, new PacManWorldMovementPolicy());
        registerComponent(PacDigestion.class, new PacDigestion());
        registerComponent(PacPower.class, new PacPower());
        registerComponent(PacCheats.class, new PacCheats());

        state = State.STUCK;
    }

    public WorldMovement worldMovement() {
        return assertComponent(WorldMovement.class);
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
            ", worldMovement=" + worldMovement() +
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

        state = State.STUCK;
        worldMovement().corneringSpeedDelta = 1.5f; // no real cornering implementation but better than nothing
        animations.select(CommonAnimationID.PAC_MUNCHING);
    }

    public State state() {
        return state;
    }

    public void setState(State state) {
        this.state = requireNonNull(state);
    }

    @Override
    public void update(GameContext gameContext) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final PacDigestionSystem digestionSystem = gameContext.systems().pacDigestionSystem;
        final PacPowerSystem powerSystem = gameContext.systems().pacPowerSystem;
        final ActorSpeedRules speedRules = gameContext.model().rules().actorSpeedRules();
        final GameLevel level = gameContext.assertLevel();

        if (state == State.DEAD || digestion().restingTicks() == PacDigestion.REST_FOREVER) {
            return;
        }

        digestionSystem.update(this);
        if (digestionSystem.isResting(this)) {
            return;
        }

        if (cheats().isUsingAutopilot()) {
            automaticSteering.steer(this, gameContext);
        }

        final float speed = powerSystem.isPowerActive(this)
            ? speedRules.pacSpeedWhenHasPower(level)
            : speedRules.pacSpeed(level);

        worldMovementSystem.setSpeed(this, speed);
        worldMovementSystem.tryMovingOrTeleporting(this, gameContext);

        if (worldMovement().info.moved) {
            animations.playSelected();
        }
        else {
            animations.stopSelected();
        }

        if (gotStuck()) {
            state = State.STUCK;
        } else {
            state = State.WALKING;
        }
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    private boolean gotStuck() {
        return (movement().velX == 0 && movement().velY == 0)
            || !worldMovement().info.moved
            || digestion().restingTicks() == PacDigestion.REST_FOREVER;
    }
}