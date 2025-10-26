/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Pac;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.ParallelCamera;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;

class PlayScene2DCamera extends ParallelCamera {

    record Range(double min, double max) {}

    enum State { INTRO, TRACKING, MANUAL }

    private final DoubleProperty scaling = new SimpleDoubleProperty(1);

    //TODO determine exakt values in NES emulator
    private static final int INTRO_MOVEMENT_START_TICK = 60;
    private static final int INTRO_MOVEMENT_DURATION_TICKS = 120;

    private Range range;
    private State state;
    private int introTick;
    private double targetY;

    public PlayScene2DCamera() {
        range = new Range(Double.MIN_VALUE, Double.MAX_VALUE);
        state = State.MANUAL;
        introTick = 0;
        targetY = 0;
    }

    // This is "alchemy", not science :-)
    public void updateRange(GameLevel gameLevel) {
        final int numRows = gameLevel.worldMap().terrainLayer().numRows();
        final int span = numRows - 26;
        final int min = switch (numRows) {
            case 30 -> -3; // MINI
            case 35, 36 -> -6; // STRANGE, ARCADE
            case 42 -> -9; // BIG
            default -> {
                Logger.warn("Unexpected number of rows: {}", numRows);
                yield 0;
            }
        };
        range = new Range(scaledTiles(min), scaledTiles(min + span));
    }

    public DoubleProperty scalingProperty() {
        return scaling;
    }

    public void update(GameLevel gameLevel) {
        switch (state) {
            case INTRO -> updateIntroMode();
            case TRACKING -> updateTrackingMode(gameLevel);
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
        setToTop();
        state = State.INTRO;
        introTick = 0;
        Logger.info("Camera intro sequence started");
    }

    private void updateIntroMode() {
        ++introTick;
        if (introTick < INTRO_MOVEMENT_START_TICK) {
            return;
        }
        if (introTick == INTRO_MOVEMENT_START_TICK) {
            setTargetToBottom();
        }
        else if (introTick == INTRO_MOVEMENT_START_TICK + INTRO_MOVEMENT_DURATION_TICKS) {
            enterTrackingMode();
            return;
        }
        move();
        Logger.debug("Intro tick={} y={} maxY={}", introTick, getTranslateY(), range.max());
    }

    public void enterTrackingMode() {
        state = State.TRACKING;
    }

    private void updateTrackingMode(GameLevel gameLevel) {
        Pac pac = gameLevel.pac();
        double relY = pac.y() / TS(gameLevel.worldMap().terrainLayer().numRows());
        if (relY < 0.5 || relY < 0.6 && pac.moveDir() == Direction.UP) {
            setTargetToTop();
        } else if (relY > 0.5 || relY > 0.4 && pac.moveDir() == Direction.DOWN) {
            setTargetToBottom();
        }
        move();
    }

    public void enterManualMode() {
        state = State.MANUAL;
    }

    public void setToTop() {
        setToY(range.min());
    }

    public void setToBottom() {
        setToY(range.max());
    }

    public void setToY(double y) {
        switch (state) {
            case INTRO -> Logger.error("Cannot set camera to y-position {} while intro is running", y);
            case TRACKING -> Logger.error("Cannot set camera to y-position {} while tracking", y);
            case MANUAL -> setTranslateY(y);
        }
    }

    public void setTargetToTop() {
        targetY = range.min();
    }

    public void setTargetToBottom() {
        targetY = range.max();
    }

    private void move() {
        setTranslateY(lerp(getTranslateY(), targetY, 0.015));
    }

    private double scaledTiles(int n) {
        return scaling.get() * TS(n);
    }
}