/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.MovingActor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.ParallelCamera;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

class PlayScene2DCamera extends ParallelCamera {

    private record RangeY(double topPosition, double bottomPosition) {}

    private enum State { INTRO, TRACKING, MANUAL }

    //TODO determine exakt values in NES emulator
    private static final int INTRO_TILT_START_TICK = 60;
    private static final int INTRO_TILT_DURATION_TICKS = 120;

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    private RangeY rangeY;
    private State state;
    private int introTick;
    private double targetY;

    public PlayScene2DCamera() {
        rangeY = new RangeY(Double.MIN_VALUE, Double.MAX_VALUE);
        state = State.MANUAL;
    }

    // This is "alchemy", not science :-)
    public void updateRange(int mapHeightTiles) {
        final int spannedTiles = mapHeightTiles - 26;
        final int topPosition = switch (mapHeightTiles) {
            case 30 -> -3;     // all MINI maps
            case 35, 36 -> -6; // one STRANGE, all ARCADE maps
            case 42 -> -9;     // all BIG maps
            default -> {
                Logger.warn("Unexpected map height (tiles): {}", mapHeightTiles);
                yield 0;
            }
        };
        rangeY = new RangeY(scaling.get() * TS(topPosition), scaling.get() * TS(topPosition + spannedTiles));
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void update(double mapHeightPixels, MovingActor movingActor) {
        switch (state) {
            case INTRO -> updateIntroMode();
            case TRACKING -> updateTrackingMode(mapHeightPixels, movingActor);
        }
    }

    /**
     * Intro: Show top of maze, wait some time, move to bottom of maze, finally start tracking Pac-Man.
     */
    public void enterIntroMode() {
        if (state == State.INTRO) {
            Logger.warn("Camera intro sequence is already running");
            return;
        }
        enterManualMode();
        setToTopPosition();
        state = State.INTRO;
        introTick = 0;
        Logger.info("Camera intro sequence started");
    }

    private void updateIntroMode() {
        ++introTick;
        if (introTick < INTRO_TILT_START_TICK) {
            return;
        }
        if (introTick == INTRO_TILT_START_TICK) {
            setTargetToBottom();
        }
        else if (introTick == INTRO_TILT_START_TICK + INTRO_TILT_DURATION_TICKS) {
            enterTrackingMode();
            return;
        }
        move();
        Logger.debug("Intro tick={} y={} maxY={}", introTick, getTranslateY(), rangeY.bottomPosition());
    }

    public void enterTrackingMode() {
        state = State.TRACKING;
    }

    private void updateTrackingMode(double mapHeightPixels, MovingActor movingActor) {
        double relY = movingActor.y() / mapHeightPixels;
        if (relY < 0.5 || relY < 0.6 && movingActor.moveDir() == Direction.UP) {
            setTargetToTop();
        } else if (relY > 0.5 || relY > 0.4 && movingActor.moveDir() == Direction.DOWN) {
            setTargetToBottom();
        }
        move();
    }

    public void enterManualMode() {
        state = State.MANUAL;
    }

    public void setToTopPosition() {
        setTranslateY(rangeY.topPosition());
    }

    public void setTargetToTop() {
        targetY = rangeY.topPosition();
    }

    public void setTargetToBottom() {
        targetY = rangeY.bottomPosition();
    }

    private void move() {
        setTranslateY(lerp(getTranslateY(), targetY, 0.015));
    }
}