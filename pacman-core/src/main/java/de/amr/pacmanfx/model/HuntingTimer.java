/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.HuntingPhaseStartedEvent;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.function.Consumer;

import static de.amr.basics.math.MathAdds.isEven;
import static de.amr.basics.math.MathAdds.isOdd;
import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public class HuntingTimer {

    private enum HuntingPhaseEvent { BEGINS, ENDS }

    private final TickTimer tickTimer;

    private final int numPhases;

    private int phaseIndex = Integer.MIN_VALUE;

    private Consumer<Integer> phaseChangeCallback = index -> Logger.info("Hunting phase index is now {}", index);

    /**
     * @param name a readable name for this timer
     * @param numPhases the total number of scatter and chasing phases (4+4 in Arcade Pac-Man games)
     */
    public HuntingTimer(String name, int numPhases) {
        this.tickTimer = new TickTimer(name);
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    public void setPhaseChangeCallback(Consumer<Integer> callback) {
        this.phaseChangeCallback = requireNonNull(callback);
    }

    /**
     * Advances the hunting timer and starts the timer for the next phase if the current phase is complete.
     *
     * @param rules the game rules
     * @param levelNumber the game level number (starts with 1)
     */
    public void update(GameRules rules, int levelNumber) {
        requireValidLevelNumber(levelNumber);
        if (tickTimer.hasExpired()) {
            logPhase(phaseIndex, HuntingPhaseEvent.ENDS);
            int nextPhaseIndex = requireValidPhaseIndex(phaseIndex + 1);
            startPhase(rules, levelNumber, nextPhaseIndex);
        } else {
            tickTimer.doTick();
        }
    }

    /**
     * @return The current timer tick count
     */
    public long tickCount() {
        return tickTimer.tickCount();
    }

    public long durationTicks() {
        return tickTimer.durationTicks();
    }

    /**
     * Stops and resets the hunting timer and the phase index to an undefined value.
     */
    public void reset() {
        tickTimer.stop();
        tickTimer.reset(TickTimer.INDEFINITE);
        phaseIndex = Integer.MIN_VALUE;
    }

    /**
     * Starts the hunting timer.
     */
    public void start() {
        tickTimer.start();
    }

    /**
     * Stops the hunting timer.
     */
    public void stop() {
        tickTimer.stop();
    }

    /**
     * @return {@code true} if the hunting timer is stopped
     */
    public boolean isStopped() {
        return tickTimer.isStopped();
    }

    /**
     * @return the number of ticks remaining for the current hunting phase
     */
    public long remainingTicksOfCurrentPhase() {
        return tickTimer.remainingTicks();
    }

    public void setPhaseIndex(int index) {
        requireValidPhaseIndex(index);
        if (phaseIndex != index) {
            phaseIndex = index;
            logPhase(phaseIndex, HuntingPhaseEvent.BEGINS);
            phaseChangeCallback.accept(phaseIndex);
        }
    }

    public int phaseIndex() { return phaseIndex; }

    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex) ? Optional.of(phaseIndex / 2) : Optional.empty();
    }

    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex) ? Optional.of(phaseIndex / 2) : Optional.empty();
    }

    public HuntingPhase currentHuntingPhase() {
        return phase(phaseIndex);
    }

    public HuntingPhase phase(int phase) {
        return isEven(phase) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING;
    }

    public boolean isChasing() {
        return currentHuntingPhase() == HuntingPhase.CHASING;
    }

    public boolean isScattering() {
        return currentHuntingPhase() == HuntingPhase.SCATTERING;
    }

    /**
     * Starts the first hunting phase for the given level and fires a game event.
     *
     * @param gameContext the game context
     * @param levelNumber the level number
     */
    public void startFirstPhase(GameContext gameContext, int levelNumber) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        startPhase(gameContext.rules(), levelNumber, 0);

        gameContext.flow().publishGameEvent(new HuntingPhaseStartedEvent(
            gameContext,
            phaseIndex,
            currentHuntingPhase())
        );
    }

    private void logPhase(int index, HuntingPhaseEvent event) {
        final String eventText = switch (event) {
            case BEGINS -> "begins:";
            case ENDS   -> "ends:  ";
        };
        Logger.info("Hunting phase {} {} {}, {} ticks / {} seconds). {}",
            index,
            eventText,
            phase(index),
            tickTimer.durationTicks(),
            (float) tickTimer.durationTicks() / GameClock.DEFAULT_TICKS_PER_SECOND,
            this);
    }

    private void startPhase(GameRules rules, int levelNumber, int index) {
        final long duration = rules.huntingPhaseDuration(levelNumber, index);
        tickTimer.restartTicks(duration);
        setPhaseIndex(index);
    }

    private int requireValidPhaseIndex(int index) {
        if (index < 0 || index > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, index));
        }
        return index;
    }
}