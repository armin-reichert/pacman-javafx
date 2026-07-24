/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.HuntingPhaseStartedEvent;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.rules.HuntingPhase;
import de.amr.pacmanfx.core.rules.HuntingRules;
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
public class HuntingTimer extends TickTimer implements HuntingRules {

    private enum HuntingPhaseEvent { BEGINS, ENDS }

    private final int numPhases;

    private int phaseIndex = Integer.MIN_VALUE;

    private Consumer<Integer> phaseChangeCallback = index -> Logger.info("Hunting phase index is now {}", index);

    /**
     * @param name a readable name for this timer
     * @param numPhases the total number of scatter and chasing phases (4+4 in Arcade Pac-Man games)
     */
    public HuntingTimer(String name, int numPhases) {
        super(name);
        this.numPhases = requireNonNegativeInt(numPhases);
    }

    public void setPhaseIndex(int index) {
        requireValidPhaseIndex(index);
        if (phaseIndex != index) {
            phaseIndex = index;
            logPhase(phaseIndex, HuntingPhaseEvent.BEGINS);
            phaseChangeCallback.accept(phaseIndex);
        }
    }

    public void setPhaseChangeCallback(Consumer<Integer> callback) {
        this.phaseChangeCallback = requireNonNull(callback);
    }

    @Override
    public void startFirstPhase(GameContext gameContext, int levelNumber) {
        requireNonNull(gameContext);
        requireValidLevelNumber(levelNumber);

        startPhase(gameContext.model().rules(), levelNumber, 0);

        gameContext.eventManager().publishGameEvent(new HuntingPhaseStartedEvent(
            gameContext,
            phaseIndex,
            currentHuntingPhase())
        );
    }

    @Override
    public void update(GameRules rules, int levelNumber) {
        requireValidLevelNumber(levelNumber);
        if (hasExpired()) {
            logPhase(phaseIndex, HuntingPhaseEvent.ENDS);
            int nextPhaseIndex = requireValidPhaseIndex(phaseIndex + 1);
            startPhase(rules, levelNumber, nextPhaseIndex);
        } else {
            doTick();
        }
    }

    @Override
    public void reset() {
        stop();
        reset(TickTimer.INDEFINITE);
        phaseIndex = Integer.MIN_VALUE;
    }

    @Override
    public long remainingTicksOfCurrentPhase() {
        return remainingTicks();
    }

    @Override
    public int phaseIndex() { return phaseIndex; }

    @Override
    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex) ? Optional.of(phaseIndex / 2) : Optional.empty();
    }

    @Override
    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex) ? Optional.of(phaseIndex / 2) : Optional.empty();
    }

    @Override
    public HuntingPhase currentHuntingPhase() {
        return phase(phaseIndex);
    }

    private HuntingPhase phase(int phase) {
        return isEven(phase) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING;
    }

    @Override
    public boolean isChasing() {
        return currentHuntingPhase() == HuntingPhase.CHASING;
    }

    @Override
    public boolean isScattering() {
        return currentHuntingPhase() == HuntingPhase.SCATTERING;
    }

    // private

    private void logPhase(int index, HuntingPhaseEvent event) {
        final String eventText = switch (event) {
            case BEGINS -> "begins:";
            case ENDS   -> "ends:  ";
        };
        Logger.info("Hunting phase {} {} {}, {} ticks / {} seconds). {}",
            index,
            eventText,
            phase(index),
            durationTicks(),
            (float) durationTicks() / GameConstants.SIMULATION_FPS,
            this);
    }

    private void startPhase(GameRules rules, int levelNumber, int index) {
        final long duration = rules.huntingPhaseDuration(levelNumber, index);
        restartTicks(duration);
        setPhaseIndex(index);
    }

    private int requireValidPhaseIndex(int index) {
        if (index < 0 || index > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, index));
        }
        return index;
    }
}