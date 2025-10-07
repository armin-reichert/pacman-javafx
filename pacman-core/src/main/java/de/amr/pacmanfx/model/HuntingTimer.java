/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Validations.requireNonNegativeInt;
import static de.amr.pacmanfx.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.lib.UsefulFunctions.isEven;
import static de.amr.pacmanfx.lib.UsefulFunctions.isOdd;
import static java.util.Objects.requireNonNull;

/**
 * Controls the timing of the hunting phases (alternating scattering and chasing).
 */
public abstract class HuntingTimer {

    private GameLevel gameLevel;
    private final TickTimer timer;
    private final int numPhases;
    private final IntegerProperty phaseIndex = new SimpleIntegerProperty();

    protected HuntingTimer(String name, int numPhases) {
        this.numPhases = requireNonNegativeInt(numPhases);
        timer = new TickTimer(requireNonNull(name));
        phaseIndex.addListener((py, ov, nv) -> logPhase());
    }

    public void setGameLevel(GameLevel gameLevel) {
        this.gameLevel = requireNonNull(gameLevel);
        phaseIndexProperty().addListener((py, ov, nv) -> {
            if (nv.intValue() > 0) {
                gameLevel.ghosts(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE)
                    .forEach(Ghost::requestTurnBack);
            }
        });
        timer.stop();
        timer.reset(TickTimer.INDEFINITE);
        phaseIndex.set(0);
    }

    public abstract long huntingTicks(int levelNumber, int phaseIndex);

    public void logPhase() {
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds). {}",
            phaseIndex(), phase(), timer.durationTicks(), (float) timer.durationTicks() / Globals.NUM_TICKS_PER_SEC, this);
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }
    public boolean isStopped() { return timer.isStopped(); }
    public long tickCount() { return timer.tickCount(); }
    public long remainingTicks() { return timer.remainingTicks(); }

    public IntegerProperty phaseIndexProperty() { return phaseIndex; }
    public int phaseIndex() { return phaseIndex.get(); }
    public Optional<Integer> currentScatterPhaseIndex() {
        return isEven(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }
    public Optional<Integer> currentChasingPhaseIndex() {
        return isOdd(phaseIndex()) ? Optional.of(phaseIndex() / 2) : Optional.empty();
    }

    public HuntingPhase phase() { return isEven(phaseIndex()) ? HuntingPhase.SCATTERING : HuntingPhase.CHASING; }

    private int requireValidPhaseIndex(int phaseIndex) {
        if (phaseIndex < 0 || phaseIndex > numPhases - 1) {
            throw new IllegalArgumentException("Hunting phase index must be 0..%d, but is %d".formatted(numPhases, phaseIndex));
        }
        return phaseIndex;
    }

    public void update() {
        if (gameLevel == null) {
            Logger.error("Cannot update hunting timer, no game level assigned");
            return;
        }
        if (timer.hasExpired()) {
            Logger.info("Hunting phase {} ({}) ends, tick={}", phaseIndex(), phase(), timer.tickCount());
            startNextPhase();
        } else {
            timer.doTick();
        }
    }

    public void startFirstHuntingPhase() {
        startPhase(0);
        logPhase(); // no change event!
    }

    private void startNextPhase() {
        int nextPhaseIndex = requireValidPhaseIndex(phaseIndex() + 1);
        startPhase(nextPhaseIndex);
    }

    private void startPhase(int phaseIndex) {
        long duration = huntingTicks(gameLevel.number(), phaseIndex);
        timer.reset(duration);
        timer.start();
        this.phaseIndex.set(phaseIndex);
    }
}